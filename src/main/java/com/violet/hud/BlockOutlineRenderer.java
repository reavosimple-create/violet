package com.violet.hud;

import com.violet.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

/**
 * Screen-space block outline: projects the targeted block's real edges (stairs, slabs etc. included)
 * onto the HUD with your chosen color and thickness. Uses no world-render internals, so it survives
 * version changes. Vanilla's thin black outline is still drawn underneath it.
 */
public final class BlockOutlineRenderer {
    private record Cam(double x, double y, double z, double rx, double ry, double rz,
                       double ux, double uy, double uz, double fx, double fy, double fz,
                       double k, int w, int h) {}

    public static void render(DrawContext ctx, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Modules.blockOutline.enabled || mc.world == null || mc.player == null) return;
        if (!mc.options.getPerspective().isFirstPerson()) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();
        VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return;

        Vec3d eye = mc.player.getCameraPosVec(delta);
        double yaw = Math.toRadians(mc.player.getYaw(delta)), pitch = Math.toRadians(mc.player.getPitch(delta));
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
        double fx = -sy * cp, fy = -sp, fz = cy * cp;     // forward
        double rx = -cy, ry = 0, rz = -sy;                // right
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx; // up = right x forward
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        double fov = Math.toRadians(mc.options.getFov().getValue() + Modules.outlineFov.value);
        Cam c = new Cam(eye.x, eye.y, eye.z, rx, ry, rz, ux, uy, uz, fx, fy, fz, (h / 2.0) / Math.tan(fov / 2), w, h);

        Box bb = shape.getBoundingBox();
        double cx0 = pos.getX() + (bb.minX + bb.maxX) / 2, cy0 = pos.getY() + (bb.minY + bb.maxY) / 2, cz0 = pos.getZ() + (bb.minZ + bb.maxZ) / 2;
        double hx = (bb.maxX - bb.minX) / 2, hy = (bb.maxY - bb.minY) / 2, hz = (bb.maxZ - bb.minZ) / 2;
        int col = Modules.outlineColor.get();
        int t = Modules.outlineThick.i();
        boolean cull = Modules.outlineHideBack.value;

        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            double ax = pos.getX() + x1, ay = pos.getY() + y1, az = pos.getZ() + z1;
            double bx = pos.getX() + x2, by = pos.getY() + y2, bz = pos.getZ() + z2;
            if (cull) {
                double mx = (ax + bx) / 2, my = (ay + by) / 2, mz = (az + bz) / 2;
                double ox = mx - cx0, oy = my - cy0, oz = mz - cz0;
                boolean onFace = false, front = false;
                if (hx > 1e-6 && Math.abs(Math.abs(ox) - hx) < 1e-4) { onFace = true; front |= Math.signum(ox) * (c.x() - (cx0 + ox)) > 0; }
                if (hy > 1e-6 && Math.abs(Math.abs(oy) - hy) < 1e-4) { onFace = true; front |= Math.signum(oy) * (c.y() - (cy0 + oy)) > 0; }
                if (hz > 1e-6 && Math.abs(Math.abs(oz) - hz) < 1e-4) { onFace = true; front |= Math.signum(oz) * (c.z() - (cz0 + oz)) > 0; }
                if (onFace && !front) return;
            }
            edge(ctx, c, ax, ay, az, bx, by, bz, col, t);
        });
    }

    private static double[] toCam(Cam c, double x, double y, double z) {
        double dx = x - c.x(), dy = y - c.y(), dz = z - c.z();
        return new double[]{
                dx * c.rx() + dy * c.ry() + dz * c.rz(),
                dx * c.ux() + dy * c.uy() + dz * c.uz(),
                dx * c.fx() + dy * c.fy() + dz * c.fz()};
    }

    private static void edge(DrawContext ctx, Cam c, double ax, double ay, double az,
                             double bx, double by, double bz, int col, int t) {
        double[] a = toCam(c, ax, ay, az), b = toCam(c, bx, by, bz);
        double near = 0.05;
        if (a[2] < near && b[2] < near) return;
        if (a[2] < near) a = clip(a, b, near);
        else if (b[2] < near) b = clip(b, a, near);
        double x1 = c.w() / 2.0 + a[0] / a[2] * c.k(), y1 = c.h() / 2.0 - a[1] / a[2] * c.k();
        double x2 = c.w() / 2.0 + b[0] / b[2] * c.k(), y2 = c.h() / 2.0 - b[1] / b[2] * c.k();
        int steps = (int) Math.min(3000, Math.max(1, Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)))));
        int half = t / 2;
        for (int i = 0; i <= steps; i++) {
            int x = (int) Math.round(x1 + (x2 - x1) * i / steps), y = (int) Math.round(y1 + (y2 - y1) * i / steps);
            ctx.fill(x - half, y - half, x - half + t, y - half + t, col);
        }
    }

    private static double[] clip(double[] behind, double[] front, double near) {
        double s = (near - behind[2]) / (front[2] - behind[2]);
        return new double[]{behind[0] + (front[0] - behind[0]) * s, behind[1] + (front[1] - behind[1]) * s, near};
    }
}
