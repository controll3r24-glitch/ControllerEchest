package com.controll3r24.controllerechest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuGUI {
    public static Inventory createMenu(Player player) {
        ConfigManager cfg = ControllerEchest.getInstance().getConfigManager();
        String title = cfg.getMenuTitle().replace("%player%", player.getName());
        Inventory inv = Bukkit.createInventory(null, 54, title);
        ControllerEchest.getInstance().getDataManager().loadPlayerData(player.getUniqueId()).thenAccept(data -> {
            for (int i = 0; i < 54; i++) {
                PlayerData.EnderChestData chest = data.getChest(i);
                if (chest == null) continue;
                int occupied = chest.getOccupiedCount();
                boolean isStaffOwner = ControllerEchest.getInstance().getDataManager().isStaff(player.getUniqueId());
                int maxSlots = isStaffOwner ? cfg.getStaffChestSize() : cfg.getDefaultChestSize();
                boolean empty = occupied == 0;
                boolean full = occupied >= maxSlots;
                Material glass;
                if (empty) glass = cfg.getEmptyGlass();
                else if (full) glass = cfg.getFullGlass();
                else glass = cfg.getHalfGlass();
                if (glass == null) glass = Material.LIME_STAINED_GLASS_PANE;
                ItemStack item = new ItemStack(glass);
                ItemMeta meta = item.getItemMeta();
                String displayName = cfg.getGlassName()
                        .replace("%number%", String.valueOf(i+1))
                        .replace("%occupied%", String.valueOf(occupied))
                        .replace("%max%", String.valueOf(maxSlots));
                meta.setDisplayName(displayName);
                item.setItemMeta(meta);
                final int slot = i;
                Bukkit.getScheduler().runTask(ControllerEchest.getInstance(), () -> inv.setItem(slot, item));
            }
        });
        return inv;
    }

    public static void handleClick(Player player, int slot) {
        if (slot < 0 || slot >= 54) return;
        int chestNum = slot + 1;
        if (!player.hasPermission("controlenderchest.slot." + chestNum) && !player.hasPermission("controlenderchest.admin")) {
            boolean hasAnySlot = false;
            for (int i = 1; i <= 54; i++) {
                if (player.hasPermission("controlenderchest.slot." + i)) {
                    hasAnySlot = true;
                    break;
                }
            }
            if (hasAnySlot) {
                player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.cannot-open"));
                return;
            }
        }
        new CommandEC().onCommand(player, null, "ec", new String[]{String.valueOf(chestNum)});
    }
}
