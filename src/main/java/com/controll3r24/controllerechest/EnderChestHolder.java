package com.controll3r24.controllerechest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public class EnderChestHolder implements InventoryHolder {
    private final UUID owner;
    private final int number;
    private Inventory inventory;

    public EnderChestHolder(UUID owner, int number) {
        this.owner = owner;
        this.number = number;
    }

    @Override
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inv) { this.inventory = inv; }
    public UUID getOwner() { return owner; }
    public int getNumber() { return number; }
}
