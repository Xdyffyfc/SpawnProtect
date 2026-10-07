package com.spawnprotect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnProtect extends JavaPlugin implements Listener {

    private String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private final String bypassPerm = "spawnprotect.bypass";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();

        worldName = cfg.getString("region.world");
        minX = cfg.getInt("region.min.x");
        minY = cfg.getInt("region.min.y");
        minZ = cfg.getInt("region.min.z");
        maxX = cfg.getInt("region.max.x");
        maxY = cfg.getInt("region.max.y");
        maxZ = cfg.getInt("region.max.z");

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SpawnProtect 已加载！保护区域已启用");
    }

    /** 判断位置是否在保护区域 */
    private boolean inProtectRegion(Location loc) {
        World w = loc.getWorld();
        if (w == null || !w.getName().equals(worldName)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    // 玩家破坏方块
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        if (inProtectRegion(loc) && !e.getPlayer().hasPermission(bypassPerm)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§c出生保护区不能破坏方块！");
        }
    }

    // 玩家放置方块
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Location loc = e.getBlock().getLocation();
        if (inProtectRegion(loc) && !e.getPlayer().hasPermission(bypassPerm)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§c出生保护区不能放置方块！");
        }
    }

    // 爆炸保护（苦力怕、TNT等）
    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> inProtectRegion(block.getLocation()));
    }

    // 火焰烧毁方块
    @EventHandler
    public void onBurn(BlockBurnEvent e) {
        if (inProtectRegion(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}