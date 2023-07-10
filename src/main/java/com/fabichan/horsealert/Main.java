package com.fabichan.horsealert;


import com.fabichan.horsealert.Commands.HorseAlertCommandExecutor;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("horsealert").setExecutor(new HorseAlertCommandExecutor(this, cooldowns));
    }


    public void moveHorseTowardsPlayer(Horse horse, Player player, double speed) {
        Vector horseLocation = horse.getLocation().toVector();
        Vector playerLocation = player.getLocation().toVector();
        Vector direction = playerLocation.subtract(horseLocation).normalize();
        Vector velocity = direction.multiply(speed);
        horse.setVelocity(velocity);
    }

    public void startHorseMovementTask(Horse horse, Player player, double speed, long delay, long period) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (horse.getLocation().distance(player.getLocation()) <= 2.0) {
                    player.sendMessage("You alerted the horse.");
                    player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
                    this.cancel();
                } else {
                    moveHorseTowardsPlayer(horse, player, speed);
                }
            }
        }.runTaskTimer(this, delay, period);
    }
    @Override
    public void onDisable() {
        getLogger().info("HorseAlert has been disabled!");

    }}
