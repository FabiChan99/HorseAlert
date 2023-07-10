package com.fabichan.horsealert.Commands;


import com.fabichan.horsealert.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class HorseAlertCommandExecutor implements CommandExecutor {
    private final Main plugin;
    private final HashMap<UUID, Long> cooldowns;

    public HorseAlertCommandExecutor(Main plugin, HashMap<UUID, Long> cooldowns) {
        this.plugin = plugin;
        this.cooldowns = cooldowns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!(player.hasPermission("horsealert.use"))) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        long cooldownTime = 6 * 60 * 60 * 1000;
        if (!player.hasPermission("horsealert.bypasscooldown")){
            long remainingTime = checkCooldown(playerUUID, cooldownTime);

            if (remainingTime > 0) {
                long remainingSeconds = remainingTime / 1000 % 60;
                long remainingMinutes = remainingTime / (60 * 1000) % 60;
                long remainingHours = remainingTime / (60 * 60 * 1000) % 24;

                player.sendMessage(ChatColor.RED + "The /horsealert command is on cooldown. You can use it again in " +
                        remainingHours + " hours, " + remainingMinutes + " minutes, " + remainingSeconds + " seconds.");
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "Alerting the Horse....");
        spawnHorse(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1, 1);
        return true;
    }

    private void spawnHorse(Player player) {
        Location horseSpawnLocation = player.getLocation().clone();
        horseSpawnLocation.setZ(horseSpawnLocation.getZ() + 20);

        Horse horse = (Horse) player.getWorld().spawnEntity(horseSpawnLocation, EntityType.HORSE);
        horse.setOwner(player);


        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location playerLocation = player.getLocation();
            Location horseLocation = horse.getLocation();

            Vector direction = playerLocation.subtract(horseLocation).toVector();
            direction.normalize();

            double speed = 0.2;
            Vector velocity = direction.multiply(speed);

            plugin.startHorseMovementTask(horse, player, 0.6, 40L, 5L);
        }, 40L);
    }

    private long checkCooldown(UUID playerId, long cooldownTime) {
        long currentTime = System.currentTimeMillis();
        long remainingTime = 0;


        if (cooldowns.containsKey(playerId)) {

            long lastUsageTime = cooldowns.get(playerId);
            remainingTime = cooldownTime - (currentTime - lastUsageTime);


            if (remainingTime <= 0) {
                cooldowns.put(playerId, currentTime);
                remainingTime = 0;
            }
        } else {
            cooldowns.put(playerId, currentTime);
        }

        return remainingTime;
    }
}