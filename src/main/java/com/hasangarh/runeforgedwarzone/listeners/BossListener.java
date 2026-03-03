package com.hasangarh.runeforgedwarzone.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import com.hasangarh.runeforgedwarzone.managers.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class BossListener implements Listener {

    private final MobManager mobManager;
    private final Random random = new Random();

    public BossListener(MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if the dead entity is our Boss
        if (mobManager.isBoss(entity)) {

            // 1. CRITICAL: Tell the manager the boss is dead so the timer can spawn a new one later
            mobManager.setBossDead();

            // 2. Clear default Wither Skeleton drops (bones/coal)
            event.getDrops().clear();
            event.setDroppedExp(5000); // Massive XP drop

            // 3. Announce to Server
            Player killer = entity.getKiller();
            String killerName = (killer != null) ? killer.getName() : "Unknown";

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&6&lWARZONE &8» &c&lThe Rune Guardian has been slain!"));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7Killer: &e" + killerName));
            Bukkit.broadcastMessage("");

            // 4. Drop a GUARANTEED Random Rune
            // We access RuneForgedRunes to create the item properly
            Plugin rawPlugin = Bukkit.getPluginManager().getPlugin("RuneForgedRunes");
            if (rawPlugin instanceof RuneForgedRunes) {
                RuneForgedRunes runePlugin = (RuneForgedRunes) rawPlugin;

                // Pick random rune type
                RuneType[] allRunes = RuneType.values();
                RuneType selected = allRunes[random.nextInt(allRunes.length)];

                // Create the physical item and add to drops
                ItemStack runeItem = runePlugin.getRuneManager().createRune(selected);
                event.getDrops().add(runeItem);
            }

            // 5. Bonus Loot (Diamonds/Gold)
            event.getDrops().add(new ItemStack(Material.DIAMOND, 5 + random.nextInt(5))); // 5-10 Diamonds
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 2));

            // 6. Sound Effect for everyone online
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1f);
            }
        }
    }
}