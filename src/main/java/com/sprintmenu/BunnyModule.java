package com.sprintmenu;

import com.sprintmenu.mixin.RabbitEntityAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/** Функция "Bunnies": клиентские кролики бегают и прыгают по блокам вокруг игрока. */
public final class BunnyModule {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sprintmenu-bunnies.properties");
    private static final double SPEED = 0.12;
    private static final int MAX_COUNT = 30;
    private static final Random RNG = new Random();
    private static boolean optimized = true;

    private static final List<RabbitEntity.Variant> VARIANTS = new ArrayList<>();

    static {
        for (RabbitEntity.Variant v : RabbitEntity.Variant.values()) {
            if (!v.name().equals("EVIL")) {
                VARIANTS.add(v);
            }
        }
    }

    private static boolean enabled = false;
    private static int count = 5;
    private static int colorIndex = 0;

    private static final List<Bunny> bunnies = new ArrayList<>();
    private static ClientWorld lastWorld;
    private static int nextId = -50000;

    private static final class Bunny {
        final RabbitEntity entity;
        double x, z, groundY;
        float yaw;
        int hop, age, turnTimer;

        Bunny(RabbitEntity entity, double x, double groundY, double z, float yaw) {
            this.entity = entity;
            this.x = x;
            this.groundY = groundY;
            this.z = z;
            this.yaw = yaw;
            this.turnTimer = 20 + RNG.nextInt(60);
            this.hop = RNG.nextInt(10);
        }
    }

    private BunnyModule() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static int getCount() {
        return count;
    }

    public static boolean isOptimized() {
        return optimized;
    }

    public static void setOptimized(boolean value) {
        optimized = value;
        save();
    }

    public static void setCount(int value) {
        count = MathHelper.clamp(value, 1, MAX_COUNT);
        save();
    }

    public static void nextColor() {
        if (VARIANTS.isEmpty()) return;
        colorIndex = (colorIndex + 1) % VARIANTS.size();
        for (Bunny b : bunnies) {
            applyColor(b.entity);
        }
        save();
    }

    public static String colorName() {
        if (VARIANTS.isEmpty()) return "-";
        String name = VARIANTS.get(colorIndex % VARIANTS.size()).name();
        return switch (name) {
            case "BROWN" -> "Коричневый";
            case "WHITE" -> "Белый";
            case "BLACK" -> "Чёрный";
            case "GOLD" -> "Золотой";
            case "SALT" -> "Соль-перец";
            case "WHITE_SPLOTCHED" -> "Пятнистый";
            default -> name;
        };
    }

    public static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        if (world != lastWorld) {
            bunnies.clear();
            lastWorld = world;
        }
        if (world == null || player == null) {
            bunnies.clear();
            return;
        }
        if (!enabled) {
            if (!bunnies.isEmpty()) clearAll(world);
            return;
        }

        if (optimized && (player.age & 1) != 0) {
            return;
        }

        Iterator<Bunny> it = bunnies.iterator();
        while (it.hasNext()) {
            Bunny b = it.next();
            b.age++;
            boolean tooFar = b.entity.squaredDistanceTo(player) > 28.0 * 28.0;
            if (b.age > 1200 || tooFar || b.entity.isRemoved()) {
                remove(world, b);
                it.remove();
                continue;
            }
            move(world, b);
        }

        while (bunnies.size() > count) {
            remove(world, bunnies.remove(bunnies.size() - 1));
        }

        if (bunnies.size() < count && player.age % 4 == 0) {
            spawn(world, player);
        }
    }

    private static void spawn(ClientWorld world, ClientPlayerEntity player) {
        double ang = RNG.nextDouble() * Math.PI * 2;
        double dist = 3 + RNG.nextDouble() * 9;
        double x = player.getX() + Math.cos(ang) * dist;
        double z = player.getZ() + Math.sin(ang) * dist;

        Double gy = groundY(world, x, player.getY(), z);
        if (gy == null) return;

        RabbitEntity rabbit = new RabbitEntity(EntityType.RABBIT, world);
        rabbit.setId(nextId--);
        if (nextId < -2_000_000_000) nextId = -50000;

        applyColor(rabbit);
        rabbit.setPosition(x, gy, z);
        rabbit.setNoGravity(true);
        rabbit.setInvulnerable(true);
        rabbit.setSilent(true);
        world.addEntity(rabbit);

        bunnies.add(new Bunny(rabbit, x, gy, z, RNG.nextFloat() * 360f));
    }

    private static void move(ClientWorld world, Bunny b) {
        if (--b.turnTimer <= 0) {
            b.yaw += RNG.nextInt(121) - 60;
            b.turnTimer = 30 + RNG.nextInt(60);
        }

        double rad = Math.toRadians(b.yaw);
        double nx = b.x - Math.sin(rad) * SPEED;
        double nz = b.z + Math.cos(rad) * SPEED;

        Double gy = groundY(world, nx, b.groundY, nz);
        if (gy == null || gy - b.groundY > 1.1 || b.groundY - gy > 3.5) {
            b.yaw += 120 + RNG.nextInt(120);
            b.turnTimer = 30 + RNG.nextInt(60);
        } else {
            b.x = nx;
            b.z = nz;
            b.groundY = gy;
        }

        if (++b.hop >= 10) {
            b.hop = 0;
            b.entity.handleStatus((byte) 1);
        }

        b.entity.setPosition(b.x, b.groundY, b.z);
        b.entity.setYaw(b.yaw);
        b.entity.setHeadYaw(b.yaw);
        b.entity.setBodyYaw(b.yaw);
    }

    private static Double groundY(ClientWorld world, double x, double refY, double z) {
        int bx = MathHelper.floor(x);
        int bz = MathHelper.floor(z);
        int startY = MathHelper.floor(refY) + 2;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int y = startY; y >= startY - 6; y--) {
            pos.set(bx, y, bz);
            VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (shape.isEmpty()) continue;

            double top = y + shape.getMax(Direction.Axis.Y);
            pos.set(bx, y + 1, bz);
            boolean freeAbove = world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                    && world.getFluidState(pos).isEmpty();
            if (freeAbove) return top;
        }
        return null;
    }

    private static void applyColor(RabbitEntity rabbit) {
        if (VARIANTS.isEmpty()) return;
        ((RabbitEntityAccessor) rabbit).sprintmenu$setVariant(VARIANTS.get(colorIndex % VARIANTS.size()));
    }

    private static void remove(ClientWorld world, Bunny b) {
        world.removeEntity(b.entity.getId(), Entity.RemovalReason.DISCARDED);
    }

    private static void clearAll(ClientWorld world) {
        for (Bunny b : bunnies) {
            remove(world, b);
        }
        bunnies.clear();
    }

    public static void load() {
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "false"));
            count = MathHelper.clamp(Integer.parseInt(props.getProperty("count", "5")), 1, MAX_COUNT);
            colorIndex = Math.max(0, Integer.parseInt(props.getProperty("color", "0")));
            optimized = Boolean.parseBoolean(props.getProperty("optimized", "true"));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    private static void save() {
        try (OutputStream out = Files.newOutputStream(FILE)) {
            Properties props = new Properties();
            props.setProperty("enabled", Boolean.toString(enabled));
            props.setProperty("count", Integer.toString(count));
            props.setProperty("color", Integer.toString(colorIndex));
            props.setProperty("optimized", Boolean.toString(optimized));
            props.store(out, "Bunnies");
        } catch (IOException ignored) {
        }
    }
}
