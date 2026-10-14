package com.caellab.randomspawn;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

final class SafeSpawnFinder {

    private static final Set<Material> BAD_GROUND = Set.of(
            Material.CACTUS, Material.MAGMA_BLOCK, Material.POWDER_SNOW,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.SWEET_BERRY_BUSH,
            Material.FIRE, Material.SOUL_FIRE, Material.COBWEB, Material.SCAFFOLDING,
            Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN);

    private final World world;
    private final int minDistance;
    private final int maxDistance;
    private final int maxChunkAttempts;
    private final int minY;
    private final Random random = new Random();

    SafeSpawnFinder(World world, int minDistance, int maxDistance, int maxChunkAttempts, int minY) {
        this.world = world;
        this.minDistance = Math.max(0, minDistance);
        this.maxDistance = Math.max(this.minDistance, maxDistance);
        this.maxChunkAttempts = Math.max(1, maxChunkAttempts);
        this.minY = minY;
    }

    void findAsync(Plugin plugin, Consumer<Location> callback) {
        attemptAsync(plugin, 0, callback);
    }

    private void attemptAsync(Plugin plugin, int attempt, Consumer<Location> callback) {
        if (attempt >= maxChunkAttempts) {
            callback.accept(null);
            return;
        }
        int x = offset();
        int z = offset();
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Location spot = scan(chunkX, chunkZ);
                    if (spot != null) {
                        callback.accept(spot);
                    } else {
                        attemptAsync(plugin, attempt + 1, callback);
                    }
                }));
    }

    private int offset() {
        int value = minDistance + random.nextInt(maxDistance - minDistance + 1);
        return random.nextBoolean() ? value : -value;
    }

    private Location scan(int chunkX, int chunkZ) {
        List<Location> candidates = new ArrayList<>();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                Location safe = check(baseX + dx, baseZ + dz);
                if (safe != null) {
                    candidates.add(safe);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private Location check(int x, int z) {
        int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if (y < minY || y + 2 >= world.getMaxHeight()) {
            return null;
        }
        Block ground = world.getBlockAt(x, y, z);
        Material type = ground.getType();
        if (!type.isSolid() || ground.isLiquid()) {
            return null;
        }
        if (BAD_GROUND.contains(type) || Tag.LEAVES.isTagged(type) || Tag.LOGS.isTagged(type)) {
            return null;
        }
        if (!world.getBlockAt(x, y + 1, z).isPassable() || !world.getBlockAt(x, y + 2, z).isPassable()) {
            return null;
        }
        String biome = world.getBiome(x, y, z).getKey().getKey();
        if (biome.contains("ocean") || biome.contains("river")) {
            return null;
        }
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }
}
