package com.caellab.randomspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class RandomSpawnPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!getConfig().getBoolean("enabled", true) || player.hasPlayedBefore()) {
            return;
        }
        getServer().getScheduler().runTask(this, () -> place(player, false));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("randomspawn.admin")) {
            sender.sendMessage("§c你没有权限使用 /randomspawn");
            return true;
        }
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayerExact(args[0]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("§c控制台请指定玩家：/randomspawn <玩家>");
            return true;
        }
        if (target == null) {
            sender.sendMessage("§c玩家不在线");
            return true;
        }
        place(target, true);
        return true;
    }

    private void place(Player player, boolean forced) {
        World world = player.getWorld();
        if (!getConfig().getStringList("worlds").contains(world.getName())) {
            if (forced) {
                player.sendMessage("§c当前世界不在随机出生点的生效范围内");
            }
            return;
        }
        SafeSpawnFinder finder = new SafeSpawnFinder(world,
                getConfig().getInt("min-distance", 1000),
                getConfig().getInt("max-distance", 3000),
                getConfig().getInt("max-chunk-attempts", 10),
                getConfig().getInt("min-y", 63));
        finder.findAsync(this, spot -> {
            if (!player.isOnline()) {
                return;
            }
            if (spot == null) {
                getLogger().warning("没找到安全的随机落点，保持原位置：" + player.getName());
                if (forced) {
                    player.sendMessage("§c没找到合适的随机落点，稍后再试");
                }
                return;
            }
            player.teleport(spot);
            if (getConfig().getBoolean("set-respawn-point", false)) {
                player.setBedSpawnLocation(spot, true);
            }
            String message = getConfig().getString("message", "");
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message
                        .replace("%x%", String.valueOf(spot.getBlockX()))
                        .replace("%z%", String.valueOf(spot.getBlockZ()))
                        .replace('&', '§'));
            }
            getLogger().info("随机投放 " + player.getName() + " 到 " + spot.getBlockX() + ", "
                    + spot.getBlockY() + ", " + spot.getBlockZ());
        });
    }
}
