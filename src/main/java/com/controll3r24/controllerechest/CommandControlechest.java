package com.controll3r24.controllerechest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.UUID;

public class CommandControlechest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("controlenderchest.admin")) {
            sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("admin.usage"));
            return true;
        }
        if (args[0].equalsIgnoreCase("open") && args.length == 3) {
            String targetName = args[1];
            int number;
            try {
                number = Integer.parseInt(args[2]);
                if (number < 1 || number > 54) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.invalid-number"));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.player-not-found"));
                return true;
            }
            final UUID targetUUID = target.getUniqueId();
            final int chestNum = number;
            ControllerEchest.getInstance().getDataManager().loadPlayerData(targetUUID).thenAccept(data -> {
                PlayerData.EnderChestData chestData = data.getChest(chestNum - 1);
                if (chestData == null) return;
                boolean isStaff = ControllerEchest.getInstance().getDataManager().isStaff(targetUUID);
                int size = isStaff ? ControllerEchest.getInstance().getConfigManager().getStaffChestSize() : ControllerEchest.getInstance().getConfigManager().getDefaultChestSize();
                if (chestData.getSize() != size) chestData.setSize(size);
                String title = ControllerEchest.getInstance().getConfigManager().getAdminChestTitle().replace("%number%", String.valueOf(chestNum)).replace("%player%", targetName);
                Inventory inv = Bukkit.createInventory(new EnderChestHolder(targetUUID, chestNum), size, title);
                inv.setContents(chestData.getItems());
                if (sender instanceof Player) {
                    Player viewer = (Player) sender;
                    Bukkit.getScheduler().runTask(ControllerEchest.getInstance(), () -> viewer.openInventory(inv));
                } else {
                    sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("errors.console-cannot-open"));
                }
            });
            return true;
        }
        sender.sendMessage(ControllerEchest.getInstance().getConfigManager().getMessage("admin.usage"));
        return true;
    }
}
