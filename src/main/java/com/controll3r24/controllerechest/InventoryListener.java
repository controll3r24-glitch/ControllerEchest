package com.controll3r24.controllerechest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public class InventoryListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String title = ControllerEchest.getInstance().getConfigManager().getMenuTitle().replace("%player%", player.getName());
        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType().name().contains("STAINED_GLASS_PANE")) {
                MenuGUI.handleClick(player, event.getSlot());
            }
            return;
        }
        // Allow normal clicks in enderchest inventories
        if (inv.getHolder() instanceof EnderChestHolder) return;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof EnderChestHolder) {
            EnderChestHolder chestHolder = (EnderChestHolder) holder;
            UUID owner = chestHolder.getOwner();
            int number = chestHolder.getNumber();
            ControllerEchest.getInstance().getDataManager().loadPlayerData(owner).thenAccept(data -> {
                PlayerData.EnderChestData chestData = data.getChest(number - 1);
                if (chestData == null) return;
                chestData.setItems(inv.getContents());
                boolean isStaff = ControllerEchest.getInstance().getDataManager().isStaff(owner);
                int expectedSize = isStaff ? ControllerEchest.getInstance().getConfigManager().getStaffChestSize() : ControllerEchest.getInstance().getConfigManager().getDefaultChestSize();
                if (chestData.getSize() != expectedSize) chestData.setSize(expectedSize);
                ControllerEchest.getInstance().getDataManager().savePlayerData(owner, data);
            });
        }
    }
}
