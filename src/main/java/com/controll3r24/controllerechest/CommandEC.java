package com.controll3r24.controllerechest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CommandEC implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.player-only"));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("controlenderchest.open")) {
            player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.no-permission"));
            return true;
        }
        int specificSlot = -1;
        for (int i = 1; i <= 54; i++) {
            if (player.hasPermission("controlenderchest.slot." + i)) {
                if (specificSlot == -1) specificSlot = i;
                else { specificSlot = -2; break; }
            }
        }
        if (args.length == 0) {
            if (specificSlot == -1) {
                player.openInventory(MenuGUI.createMenu(player));
            } else if (specificSlot > 0) {
                openChest(player, player, specificSlot);
            } else {
                player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.no-permission"));
            }
            return true;
        }
        try {
            int number = Integer.parseInt(args[0]);
            if (number < 1 || number > 54) {
                player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.invalid-number"));
                return true;
            }
            if (specificSlot == -1 || specificSlot == number || player.hasPermission("controlenderchest.admin")) {
                openChest(player, player, number);
            } else {
                player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.cannot-open"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.invalid-number"));
        }
        return true;
    }

    private void openChest(Player viewer, Player owner, int number) {
        ControllerEchest.getInstance().getDataManager().loadPlayerData(owner.getUniqueId()).thenAccept(data -> {
            PlayerData.EnderChestData chestData = data.getChest(number - 1);
            if (chestData == null) return;
            boolean isStaff = ControllerEchest.getInstance().getDataManager().isStaff(owner.getUniqueId());
            int size = isStaff ? ControllerEchest.getInstance().getConfigManager().getStaffChestSize() : ControllerEchest.getInstance().getConfigManager().getDefaultChestSize();
            if (chestData.getSize() != size) chestData.setSize(size);
            String title = ControllerEchest.getInstance().getConfigManager().getChestTitle().replace("%number%", String.valueOf(number));
            Inventory inv = Bukkit.createInventory(new EnderChestHolder(owner.getUniqueId(), number), size, title);
            inv.setContents(chestData.getItems());
            Bukkit.getScheduler().runTask(ControllerEchest.getInstance(), () -> viewer.openInventory(inv));
        });
    }
}
