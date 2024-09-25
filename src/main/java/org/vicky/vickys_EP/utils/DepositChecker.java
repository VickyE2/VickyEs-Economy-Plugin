package org.vicky.vickys_EP.utils;

import dev.lone.itemsadder.api.CustomStack;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.util.*;

public class DepositChecker {
    private final JavaPlugin plugin;
    private int goldAmount = 0;
    private int silverAmount = 0;
    private int copperAmount = 0;
    private int tinAmount = 0;

    private final Set<Player> cooldownPlayers = new HashSet<>();
    private final List<ItemStack> itemsToCheck = new ArrayList<>();

    public DepositChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    Economy economy = VickysEconomyPlugin.getEconomy();

    private boolean isValidCoin(ItemStack item, boolean isCustom) {
        if (item == null || item.getType().isAir()) {
            plugin.getLogger().warning("Item is null");
            return false;
        }

        if (isCustom) {
            CustomStack customItem = CustomStack.byItemStack(item);
            if (customItem == null) {
                plugin.getLogger().warning("Custom item is null");
                return false;
            }

            plugin.getLogger().info("size of items in coin check: " + itemsToCheck.size());
            boolean isGold = customItem.getNamespacedID().equals("vickyes_ep:gold_coin");
            boolean isSilver = customItem.getNamespacedID().equals("vickyes_ep:silver_coin");
            boolean isCopper = customItem.getNamespacedID().equals("vickyes_ep:copper_coin");
            boolean isTin = customItem.getNamespacedID().equals("vickyes_ep:tin_coin");

            if (isGold) {
                plugin.getLogger().info("Coin is a gold coin");
                return true;
            } else if (isSilver) {
                plugin.getLogger().info("Coin is a silver coin");
                return true;
            } else if (isCopper) {
                plugin.getLogger().info("Coin is a copper coin");
                return true;
            } else if (isTin) {
                plugin.getLogger().info("Coin is a tin coin");
                return true;
            }
        }

        return false;
    }

    private void addPlayerToCooldown(Player player) {
        cooldownPlayers.add(player);
    }

    private void removePlayerFromCooldown(Player player) {
        cooldownPlayers.remove(player);
    }

    public boolean isPlayerInCooldown(Player player) {
        return cooldownPlayers.contains(player);
    }

    public void depositCoins(Player player, List<Integer> coinSlots, int buttonSlot, boolean isCustom) {
        goldAmount = 0;
        silverAmount = 0;
        copperAmount = 0;
        tinAmount = 0;
        itemsToCheck.clear();

        Inventory inventory = player.getOpenInventory().getTopInventory();

        if (isPlayerInCooldown(player)) {
            player.sendMessage(ChatColor.RED + "You can't perform another transaction until the current one is finished.");
            return;
        }

        plugin.getLogger().info("Checking coins in specified slots: " + coinSlots);

        // Add player to cooldown
        addPlayerToCooldown(player);

        // Iterate through each specified coin slot and check if it contains valid coins
        for (int slot : coinSlots) {
            ItemStack item = inventory.getItem(slot);
            checkAndAddItem(item, slot, isCustom);
        }

        boolean isEmpty = itemsToCheck.isEmpty();

        // Process valid items
        for (ItemStack item : itemsToCheck) {
            if (item.isSimilar(CustomStack.getInstance("vickyes_ep:gold_coin").getItemStack())) {
                goldAmount += item.getAmount();
            } else if (item.isSimilar(CustomStack.getInstance("vickyes_ep:silver_coin").getItemStack())) {
                silverAmount += item.getAmount();
            } else if (item.isSimilar(CustomStack.getInstance("vickyes_ep:copper_coin").getItemStack())) {
                copperAmount += item.getAmount();
            } else if (item.isSimilar(CustomStack.getInstance("vickyes_ep:tin_coin").getItemStack())) {
                tinAmount += item.getAmount();
            }
        }

        // Perform deposit if there are valid coins
        if (!isEmpty) {
            double totalMoney = goldAmount * 1000 + silverAmount * 100 + copperAmount + tinAmount * 0.01;
            economy.depositPlayer(player, "Through Bank Deposit", totalMoney);

            ItemStack button = inventory.getItem(buttonSlot);
            modifyItemLore(button, ChatColor.GREEN + "   - Transaction Successful, Deposited ﾶ" + totalMoney, null);

            // Clear the slots after deposit
            for (int slot : coinSlots) {
                inventory.setItem(slot, null);
            }

            // Disable button after transaction
            disableTransactionButton(inventory, buttonSlot, totalMoney, player);
        } else {
            // Handle failure case
            handleTransactionFailure(inventory, buttonSlot, player);
        }
    }

    // Helper method to disable the button after a transaction
    private void disableTransactionButton(Inventory inventory, int buttonSlot, double totalMoney, Player player) {
        CustomStack disabled = CustomStack.getInstance("vicky_utls:red_failed_x");
        ItemMeta meta = disabled.getItemStack().getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Cannot Perform a Transaction Now");
        List<String> loreMessages = new ArrayList<>();
        loreMessages.add(ChatColor.GREEN + "   - Transaction Successful, Deposited ﾶ" + totalMoney);
        meta.setLore(loreMessages);
        disabled.getItemStack().setItemMeta(meta);
        inventory.setItem(buttonSlot, disabled.getItemStack());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removePlayerFromCooldown(player);
            CustomStack enabled = CustomStack.getInstance("vicky_utls:green_arrow_right");
            ItemMeta metaE = enabled.getItemStack().getItemMeta();
            metaE.setDisplayName(ChatColor.DARK_GREEN + "ᴅᴇᴘᴏsɪᴛ");
            enabled.getItemStack().setItemMeta(metaE);
            inventory.setItem(buttonSlot, enabled.getItemStack());
        }, 80);
    }

    // Handle transaction failure
    private void handleTransactionFailure(Inventory inventory, int buttonSlot, Player player) {
        CustomStack disabled = CustomStack.getInstance("vicky_utls:red_failed_x");
        ItemMeta meta = disabled.getItemStack().getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Cannot Perform a Transaction Now");
        List<String> loreMessages = new ArrayList<>();
        loreMessages.add(ChatColor.RED + "Transaction Failed: Invalid items.");
        meta.setLore(loreMessages);
        disabled.getItemStack().setItemMeta(meta);
        inventory.setItem(buttonSlot, disabled.getItemStack());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removePlayerFromCooldown(player);
            CustomStack enabled = CustomStack.getInstance("vicky_utls:green_arrow_right");
            ItemMeta metaE = enabled.getItemStack().getItemMeta();
            metaE.setDisplayName(ChatColor.DARK_GREEN + "ᴅᴇᴘᴏsɪᴛ");
            enabled.getItemStack().setItemMeta(metaE);
            inventory.setItem(buttonSlot, enabled.getItemStack());
        }, 80);
    }

    // Helper method to check and add valid coins from a specific slot
    private void checkAndAddItem(ItemStack item, int slot, boolean isCustom) {
        if (item != null && !item.getType().isAir()) {
            if (isValidCoin(item, isCustom)) {
                plugin.getLogger().info("Valid coin found in slot: " + slot);
                itemsToCheck.add(item);
            } else {
                plugin.getLogger().info("Invalid item found in slot: " + slot);
            }
        } else {
            plugin.getLogger().info("No valid item in slot: " + slot);
        }
    }

    private void modifyItemLore(ItemStack item, String lore, List<String> loreCList) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (lore != null) {
                List<String> loreList = new ArrayList<>();
                loreList.add(lore);
                meta.setLore(loreList);
            } else if (loreCList != null) {
                meta.setLore(loreCList);
            } else {
                meta.setLore(null); // Clear lore
            }
            item.setItemMeta(meta);
        }
    }
}
