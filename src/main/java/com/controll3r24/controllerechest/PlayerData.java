package com.controll3r24.controllerechest;

import org.bukkit.inventory.ItemStack;
import java.util.Arrays;

public class PlayerData {
    private final EnderChestData[] chests;

    public PlayerData() {
        chests = new EnderChestData[54];
        for (int i = 0; i < 54; i++) {
            chests[i] = new EnderChestData();
        }
    }

    public EnderChestData getChest(int index) {
        if (index < 0 || index >= chests.length) return null;
        return chests[index];
    }

    public void setChest(int index, EnderChestData data) {
        if (index >= 0 && index < chests.length) chests[index] = data;
    }

    public static class EnderChestData {
        private ItemStack[] items;
        private int size;

        public EnderChestData() {
            this.size = ControllerEchest.getInstance().getConfigManager().getDefaultChestSize();
            this.items = new ItemStack[size];
        }

        public EnderChestData(int size) {
            this.size = Math.max(9, Math.min(54, size));
            this.items = new ItemStack[this.size];
        }

        public ItemStack[] getItems() { return items.clone(); }
        public void setItems(ItemStack[] items) {
            if (items == null) return;
            this.items = Arrays.copyOf(items, size);
        }

        public int getSize() { return size; }

        public void setSize(int size) {
            size = Math.max(9, Math.min(54, size));
            if (this.size == size) return;
            ItemStack[] newItems = new ItemStack[size];
            System.arraycopy(items, 0, newItems, 0, Math.min(items.length, size));
            this.items = newItems;
            this.size = size;
        }

        public int getOccupiedCount() {
            int count = 0;
            for (ItemStack item : items) {
                if (item != null && !item.getType().isAir()) count++;
            }
            return count;
        }

        public boolean isEmpty() { return getOccupiedCount() == 0; }
        public boolean isFull() { return getOccupiedCount() == size; }
    }
}
