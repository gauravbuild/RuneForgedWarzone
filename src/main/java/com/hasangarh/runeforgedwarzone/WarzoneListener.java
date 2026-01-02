package com.hasangarh.runeforgedwarzone;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WarzoneListener implements Listener {

    private final RuneForgedWarzone plugin;
    private final Set<UUID> fallDamageImmunity = new HashSet<>();

    public WarzoneListener(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    // --- 1. The WorldGuard Teleport Logic ---
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("void-teleport.enabled")) return;

        // Optimization: Only run if block changed
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        String triggerRegionName = plugin.getConfig().getString("void-teleport.trigger-region");

        if (isInRegion(player, triggerRegionName)) {
            triggerWarzoneDrop(player);
        }
    }

    private boolean isInRegion(Player player, String regionName) {
        Location loc = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));

        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }
        return false;
    }

    private void triggerWarzoneDrop(Player player) {
        // 1. Reset Fall Distance
        player.setFallDistance(0);

        // 2. Add immunity
        if (plugin.getConfig().getBoolean("void-teleport.prevent-fall-damage")) {
            fallDamageImmunity.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> fallDamageImmunity.remove(player.getUniqueId()), 100L); // 5 seconds
        }

        // 3. Play Sound
        String soundName = plugin.getConfig().getString("void-teleport.sound");
        try {
            if (soundName != null && !soundName.isEmpty()) {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), 1f, 1f);
            }
        } catch (Exception ignored) {}

        // 4. Run Command
        String cmd = plugin.getConfig().getString("void-teleport.command").replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    // --- 2. Damage & Protection Logic ---

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && fallDamageImmunity.contains(player.getUniqueId())) {
                event.setCancelled(true);
                fallDamageImmunity.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (plugin.getConfig().getBoolean("void-teleport.prevent-fall-damage")) {
            event.getPlayer().setFallDistance(0);
        }
    }

    // --- 3. Warzone Mechanics (Manual Protections) ---
    // Note: WorldGuard flags can also handle this, but this gives us custom deny messages.

    private boolean isWarzone(Player player) {
        return player.getWorld().getName().equals(plugin.getConfig().getString("warzone-rules.world-name"));
    }

    private void denyAction(Player player) {
        String msg = plugin.getConfig().getString("warzone-rules.deny-message");
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("runeforgedwarzone.bypass")) return;
        if (isWarzone(event.getPlayer()) && !plugin.getConfig().getBoolean("warzone-rules.allow-block-break")) {
            event.setCancelled(true);
            denyAction(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("runeforgedwarzone.bypass")) return;
        if (isWarzone(event.getPlayer()) && !plugin.getConfig().getBoolean("warzone-rules.allow-block-place")) {
            event.setCancelled(true);
            denyAction(event.getPlayer());
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().hasPermission("runeforgedwarzone.bypass")) return;
        if (isWarzone(event.getPlayer()) && !plugin.getConfig().getBoolean("warzone-rules.allow-buckets")) {
            event.setCancelled(true);
            denyAction(event.getPlayer());
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (event.getPlayer().hasPermission("runeforgedwarzone.bypass")) return;
        if (isWarzone(event.getPlayer()) && !plugin.getConfig().getBoolean("warzone-rules.allow-buckets")) {
            event.setCancelled(true);
            denyAction(event.getPlayer());
        }
    }

    @EventHandler
    public void onTrample(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().toString().contains("FARMLAND")) {
                if (isWarzone(event.getPlayer()) && !plugin.getConfig().getBoolean("warzone-rules.allow-crop-trample")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}