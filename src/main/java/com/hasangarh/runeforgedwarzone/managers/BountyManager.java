package com.hasangarh.runeforgedwarzone.managers;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyManager {

    private final RuneForgedWarzone plugin;
    private final Map<UUID, Double> activeBounties = new HashMap<>();
    private final Map<UUID, Integer> killStreaks = new HashMap<>();
    private final Map<UUID, UUID> trackers = new HashMap<>();

    public BountyManager(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    public void addKill(Player killer) {
        UUID id = killer.getUniqueId();
        int streak = killStreaks.getOrDefault(id, 0) + 1;
        killStreaks.put(id, streak);

        FileConfiguration config = plugin.getBountyConfig();
        String path = "killstreaks." + streak;

        if (config != null && config.contains(path)) {
            double amount = config.getDouble(path + ".bounty-add");
            String msg = config.getString(path + ".message").replace("%player%", killer.getName());
            activeBounties.put(id, activeBounties.getOrDefault(id, 0.0) + amount);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    public void handleDeath(Player victim, Player killer) {
        killStreaks.remove(victim.getUniqueId());
        trackers.values().removeIf(target -> target.equals(victim.getUniqueId()));

        if (activeBounties.containsKey(victim.getUniqueId())) {
            double amount = activeBounties.remove(victim.getUniqueId());
            if (killer != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + killer.getName() + " " + amount);
            }
        }
    }

    public void startTracking(Player hunter, Player target) {
        trackers.put(hunter.getUniqueId(), target.getUniqueId());
    }

    public UUID getTrackingTarget(UUID hunter) { return trackers.get(hunter); }
    public void stopTracking(UUID hunter) { trackers.remove(hunter); }
    public Map<UUID, Double> getActiveBounties() { return activeBounties; }

    public boolean addManualBounty(Player player, Player target, double amount) {
        return false;
    }
}