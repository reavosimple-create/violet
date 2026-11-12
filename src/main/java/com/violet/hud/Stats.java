package com.violet.hud;

import com.violet.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

public final class Stats {
    public static int combo;
    public static long lastHit, hitMs;
    public static Entity target;
    public static double reach, speed;
    public static final long sessionStart = System.currentTimeMillis();

    public static double dmg;
    public static long dmgTime;
    private static float lastHp = -1;
    private static final int[] fpsBuf = new int[200];
    private static int fpsIdx, fpsCount;
    public static int fpsAvg, fpsMin;

    private static double px, pz;
    private static boolean hasPos;
    private static final List<ArrayDeque<Long>> clicks = List.of(new ArrayDeque<>(), new ArrayDeque<>());
    private static final boolean[] was = new boolean[2];
    private static final Random rng = new Random();

    public static void pollClicks(MinecraftClient mc) {
        if (mc.currentScreen != null) return;
        long h = mc.getWindow().getHandle(), now = System.currentTimeMillis();
        for (int b = 0; b < 2; b++) {
            boolean d = GLFW.glfwGetMouseButton(h, b) == GLFW.GLFW_PRESS;
            if (d && !was[b]) clicks.get(b).add(now);
            was[b] = d;
            while (!clicks.get(b).isEmpty() && now - clicks.get(b).peekFirst() > 1000) clicks.get(b).pollFirst();
        }
    }

    public static int cps(int b) { return clicks.get(b).size(); }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        if (hasPos) speed = Math.hypot(p.getX() - px, p.getZ() - pz) * 20.0;
        px = p.getX(); pz = p.getZ(); hasPos = true;
        long now = System.currentTimeMillis();
        if (p.hurtTime > 0 || now - lastHit > 3000) combo = 0;

        fpsBuf[fpsIdx] = mc.getCurrentFps();
        fpsIdx = (fpsIdx + 1) % fpsBuf.length;
        fpsCount = Math.min(fpsCount + 1, fpsBuf.length);
        long sum = 0; int mn = Integer.MAX_VALUE;
        for (int i = 0; i < fpsCount; i++) { sum += fpsBuf[i]; mn = Math.min(mn, fpsBuf[i]); }
        fpsAvg = (int) (sum / Math.max(1, fpsCount));
        fpsMin = mn == Integer.MAX_VALUE ? 0 : mn;

        if (target instanceof LivingEntity le && le.isAlive()) {
            float hp = le.getHealth();
            if (lastHp >= 0 && hp < lastHp - 0.01f) { dmg = lastHp - hp; dmgTime = now; }
            lastHp = hp;
        } else lastHp = -1;
    }

    public static void onHit(Entity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        if (e != target) lastHp = -1;
        combo++; lastHit = now; hitMs = now; target = e;
        Vec3d eye = mc.player.getEyePos();
        Box bb = e.getBoundingBox();
        Vec3d c = new Vec3d(MathHelper.clamp(eye.x, bb.minX, bb.maxX),
                MathHelper.clamp(eye.y, bb.minY, bb.maxY), MathHelper.clamp(eye.z, bb.minZ, bb.maxZ));
        reach = eye.distanceTo(c);

        if (Modules.hitSound.enabled) {
            SoundEvent se = switch (Modules.hitSoundType.get()) {
                case "Arrow" -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
                case "Level Up" -> SoundEvents.ENTITY_PLAYER_LEVELUP;
                case "Pop" -> SoundEvents.ENTITY_ITEM_PICKUP;
                case "Glass" -> SoundEvents.BLOCK_GLASS_BREAK;
                default -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            };
            mc.getSoundManager().play(PositionedSoundInstance.master(se, (float) Modules.hitPitch.value, (float) Modules.hitVolume.value));
        }

        if (Modules.hitParticles.enabled) {
            ParticleEffect p = switch (Modules.particleType.get()) {
                case "Heart" -> ParticleTypes.HEART;
                case "Enchant" -> ParticleTypes.ENCHANT;
                case "Flame" -> ParticleTypes.FLAME;
                case "Witch" -> ParticleTypes.WITCH;
                case "End Rod" -> ParticleTypes.END_ROD;
                default -> ParticleTypes.CRIT;
            };
            for (int i = 0; i < Modules.particleCount.i(); i++) {
                mc.particleManager.addParticle(p,
                        e.getX() + (rng.nextDouble() - 0.5) * e.getWidth(),
                        e.getBodyY(0.2 + rng.nextDouble() * 0.7),
                        e.getZ() + (rng.nextDouble() - 0.5) * e.getWidth(),
                        (rng.nextDouble() - 0.5) * 0.3, 0.15, (rng.nextDouble() - 0.5) * 0.3);
            }
        }
    }
}
