package com.hasangarh.runeforgedwarzone.managers;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class MobManager {

    private final RuneForgedWarzone plugin;
    public static final String BOSS_NAME = ChatColor.translateAlternateColorCodes('&', "&4&l☠ &c&lRune Guardian &4&l☠");

    // Tracking the active boss
    private UUID activeBossId = null;

    public MobManager(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    // --- SCHEDULER START ---
    public void startScheduler() {
        int interval = plugin.getConfig().getInt("boss.interval", 3600) * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnGuardianAtAltar();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void spawnGuardianAtAltar() {
        // 1. Check if Boss is already alive
        if (isBossAlive()) {
            Bukkit.broadcastMessage(ChatColor.RED + "The Rune Guardian is still alive! No new boss spawned.");
            return;
        }

        // 2. Get Location from Config
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("boss.location.world")) {
            plugin.getLogger().warning("Boss Altar location not set! Use /warzone setaltar");
            return;
        }

        World world = Bukkit.getWorld(config.getString("boss.location.world"));
        double x = config.getDouble("boss.location.x");
        double y = config.getDouble("boss.location.y");
        double z = config.getDouble("boss.location.z");
        Location loc = new Location(world, x, y, z);

        // 3. Spawn
        spawnGuardian(loc);

        // 4. Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l☠ &c&lBOSS EVENT &4&l☠"));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7The &cRune Guardian &7has spawned at the Altar!"));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&eWarzone: &f" + x + ", " + y + ", " + z));
        Bukkit.broadcastMessage("");

        // Sound for everyone
        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
        }
    }

    public void spawnGuardian(Location loc) {
        // Pre-Clear area to be safe (optional)
        // loc.getWorld().getNearbyEntities(loc, 2, 2, 2).forEach(e -> { if(isBoss((LivingEntity)e)) e.remove(); });

        WitherSkeleton boss = (WitherSkeleton) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);

        boss.setCustomName(BOSS_NAME);
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setPersistent(true);

        // Gear
        boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));

        boss.getEquipment().setHelmetDropChance(0f);
        boss.getEquipment().setChestplateDropChance(0f);
        boss.getEquipment().setItemInMainHandDropChance(0f);

        // Stats
        if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
            boss.setHealth(500.0);
        }
        if (boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
        }

        // Track ID
        this.activeBossId = boss.getUniqueId();
    }

    public boolean isBoss(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().equals(BOSS_NAME);
    }

    public boolean isBossAlive() {
        if (activeBossId == null) return false;
        org.bukkit.entity.Entity entity = Bukkit.getEntity(activeBossId);
        return entity != null && !entity.isDead() && entity.isValid();
    }

    public void setBossDead() {
        this.activeBossId = null;
    }
}