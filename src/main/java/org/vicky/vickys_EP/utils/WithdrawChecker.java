package org.vicky.vickys_EP.utils;

import dev.lone.itemsadder.api.CustomStack;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.guiparent.ButtonAction;
import org.vicky.vickys_EP.Listeners.MainDepositAndWithdrawListener;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class WithdrawChecker {

    private final Economy economy = VickysEconomyPlugin.getEconomy();
    private final Map<Integer, String> coinSlots = new HashMap<>(); // Slot to Coin Type mapping
    private final Map<String, Integer> additionButtonSlots = new HashMap<>(); // Maps coin slots to button slots
    private final Map<String, Integer> subtractionButtonSlots = new HashMap<>(); // Maps coin slots to subtract button slots
    private final List<Integer> withdrawalButtonSlots = new ArrayList<>();

    private final JavaPlugin plugin;
    private final Set<Player> cooldownPlayers = new HashSet<>();

    // Constructor
    public WithdrawChecker(JavaPlugin plugin) {
        this.plugin = plugin;
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

    // Method to withdraw coins from specified slots and deduct from player's balance
    public void withdrawCoins(Inventory inventory, Player player, int buttonSlot) {

        if (isPlayerInCooldown(player)) {
            player.sendMessage(ChatColor.DARK_RED + "You cannot perform a Transaction now");
            return;
        }

        addPlayerToCooldown(player);

        double totalValue = calculateTotalCoinValue(inventory);

        // Check if the player has sufficient balance to deduct the total value
        if (economy.getBalance(player) >= totalValue) {
            // Withdraw the coins from the inventory and deduct the balance
            for (Map.Entry<Integer, String> entry : coinSlots.entrySet()) {
                Integer slot = entry.getKey();
                String coinType = entry.getValue();

                ItemStack item = inventory.getItem(slot);
                if (item != null && !item.getType().isAir()) {
                    int amount = item.getAmount();

                    // Remove the item from the inventory
                    inventory.setItem(slot, null);

                    // Create a new coin stack and add to the player's inventory
                    ItemStack coinStack = CustomStack.getInstance("vickyes_ep:" + coinType + "_coin").getItemStack();
                    coinStack.setAmount(amount);

                    // Use the player's inventory addItem method which may trigger events
                    Map<Integer, ItemStack> leftover = player.getInventory().addItem(coinStack);

                    // Handle any leftover items if the inventory is full
                    if (!leftover.isEmpty()) {
                        for (ItemStack leftOverItem : leftover.values()) {
                            player.getWorld().dropItem(player.getLocation(), leftOverItem); // Drop excess coins on the ground
                        }
                    }
                }
            }

            // Deduct the total amount from the player's economy
            economy.withdrawPlayer(player, totalValue);
            disableTransactionButton(inventory, buttonSlot, totalValue, player, ChatColor.GREEN + "  - Transaction Successful, Withdrew ﾶ" + totalValue);
        } else {
            player.sendMessage(ChatColor.RED + "Insufficient balance to withdraw these coins.");
            disableTransactionButton(inventory, buttonSlot, totalValue, player, ChatColor.RED + "  - Insufficient Funds");
        }
    }


    private void disableTransactionButton(Inventory inventory, int buttonSlot, double totalMoney, Player player, String reason) {
        CustomStack disabled = CustomStack.getInstance("vicky_utls:red_failed_x");
        ItemMeta meta = disabled.getItemStack().getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Cannot Perform a Transaction Now");
        List<String> loreMessages = new ArrayList<>();
        loreMessages.add(reason);
        meta.setLore(loreMessages);
        disabled.getItemStack().setItemMeta(meta);
        inventory.setItem(buttonSlot, disabled.getItemStack());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removePlayerFromCooldown(player);
            CustomStack enabled = CustomStack.getInstance("vicky_utls:green_arrow_right");
            ItemMeta metaE = enabled.getItemStack().getItemMeta();
            metaE.setDisplayName(ChatColor.DARK_GREEN + "ᴡɪᴛʜᴅʀᴀᴡ");
            enabled.getItemStack().setItemMeta(metaE);
            inventory.setItem(buttonSlot, enabled.getItemStack());
        }, 80);
    }

    // Register a button for either adding, subtracting, or withdrawing coins
    public void setButton(int slot, boolean isAddition, boolean isWithdraw, String coinType, Player player, MainDepositAndWithdrawListener listener, Map<String, List<Integer>> coinSlotMapping) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            plugin.getLogger().info("Current inventory size after delay: " + inventory.getSize());

            // Assign the slots for each coin type based on the provided mapping
            assignCoinSlots(coinSlotMapping);

            ButtonAction buttonAction;
            if (isAddition && !isWithdraw) {
                buttonAction = new ButtonAction(playerA -> addCoinToSlot(inventory, coinType, playerA), false);
                listener.registerADSBButton(buttonAction, slot);
                additionButtonSlots.put(coinType, slot);
            } else if (!isAddition && !isWithdraw){
                buttonAction = new ButtonAction(playerS -> subtractCoinFromSlot(inventory, coinType, playerS), false);
                listener.registerADSBButton(buttonAction, slot);
                subtractionButtonSlots.put(coinType, slot);
            }

            // Adding a withdraw button action
            if (isWithdraw) {
                withdrawalButtonSlots.add(slot);

                ButtonAction withdrawButtonAction = new ButtonAction(playerA -> withdrawCoins(inventory, playerA, slot), false);
                listener.registerADSBButton(withdrawButtonAction, slot); // Assuming the next slot is for withdraw
            }

            plugin.getLogger().info(
                    "GoldSlots: " + coinSlotsForType("gold") +
                            ". SilverSlots: " + coinSlotsForType("silver") +
                            ". CopperSlots: " + coinSlotsForType("copper") +
                            ". TinSlots: " + coinSlotsForType("tin") + ".");
        }, 2);
    }

    // Method to add a coin to a specified slot
    private void addCoinToSlot(Inventory inventory, String coinType, Player player) {
        double playerBalance = economy.getBalance(player);
        plugin.getLogger().info("Function to add coin to slot has been called");
        boolean addedCoin = false; // Track if we successfully added a coin
        boolean sufficientFund = false;

        List<Integer> sortedKeys = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : coinSlots.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(coinType)) {
                sortedKeys.add(entry.getKey());
            }
        }

        // Sort the filtered keys in ascending order
        Collections.sort(sortedKeys);
        // Iterate through sorted slots and attempt to add coins
        for (int slot : sortedKeys) {
            if (slot < 0 || slot >= inventory.getSize()) continue; // Skip if slot is invalid

            ItemStack existingItem = inventory.getItem(slot);
            int existingNumberItems = (existingItem != null) ? existingItem.getAmount() : 0;
            double newTotalValue = calculateTotalCoinValue(inventory) + getCoinValue(coinType);

            // Check if the slot is empty, air, or contains a similar coin type and that the stack is not full
            if (newTotalValue <= playerBalance) {
                // Calculate the total value of coins if we add one more
                sufficientFund = true;
                // Check if we have enough funds to add this coin
                if (existingItem == null || existingItem.getType().isAir() || existingItem.isSimilar(CustomStack.getInstance("vickyes_ep:" + coinType + "_coin").getItemStack()) && existingNumberItems < 64) {
                    // If sufficient funds, proceed to add the coin
                    int spaceLeft = 64 - existingNumberItems; // Maximum stack size is 64
                    int coinsToAdd = Math.min(1, spaceLeft); // Add 1 coin or as much as the space left allows

                    // Create the coin stack with the correct amount
                    ItemStack coinStack = CustomStack.getInstance("vickyes_ep:" + coinType + "_coin").getItemStack();
                    coinStack.setAmount(existingNumberItems + coinsToAdd);
                    inventory.setItem(slot, coinStack);

                    plugin.getLogger().info("Added " + coinsToAdd + " coins to slot: " + slot);
                    addedCoin = true; // Successfully added a coin
                     // Indicate sufficient funds for the operation

                    break; // Exit the loop after successfully adding coins to a slot
                }
            }
        }

// Log if no coin was added after checking all slots
        if (!addedCoin) {
            if (!sufficientFund) {
                changeButtonToMaxedItem(inventory, coinType, ChatColor.DARK_RED + "Insufficient Funds to add coin");
                plugin.getLogger().info("No available funds to add coins for " + coinType);
            } else {
                changeButtonToMaxedItem(inventory, coinType, ChatColor.DARK_RED + "Max Stack Reached");
                plugin.getLogger().info("No available slots to add coins for " + coinType);
            }
        }

    }


    // Method to subtract a coin from a specified slot
    private void subtractCoinFromSlot(Inventory inventory, String coinType, Player player) {
        double playerBalance = economy.getBalance(player);

        // Find all slots for this coinType and sort by the slot number (descending)
        List<Integer> coinTypeSlots = coinSlots.entrySet().stream()
                .filter(entry -> entry.getValue().equals(coinType))
                .map(Map.Entry::getKey)
                .sorted(Comparator.reverseOrder()) // Sort slots in descending order
                .toList();

        for (Integer slot : coinTypeSlots) {
            ItemStack existingItem = inventory.getItem(slot);
            if (existingItem != null && !existingItem.getType().isAir()) {
                int existingNumberItems = existingItem.getAmount();

                if (existingNumberItems > 0) {
                    existingItem.setAmount(existingNumberItems - 1);
                    if (existingItem.getAmount() > 0) {
                        inventory.setItem(slot, existingItem); // Update the slot with the reduced amount
                    } else {
                        inventory.setItem(slot, null); // Clear the slot if no coins are left
                    }

                    // After subtraction, check if the button can be reverted from maxed
                    revertButtonFromMaxed(inventory, playerBalance);
                    break; // Exit once a coin has been subtracted
                }
            }
        }
    }

    // Calculate the total value of coins in the defined slots
    private double calculateTotalCoinValue(Inventory inventory) {
        double totalValue = 0;

        for (Map.Entry<Integer, String> entry : coinSlots.entrySet()) {
            ItemStack item = inventory.getItem(entry.getKey());
            if (item != null && !item.getType().isAir()) {
                totalValue += item.getAmount() * getCoinValue(entry.getValue());
            }
        }

        return totalValue;
    }

    // Placeholder for getting the coin value based on coin type
    private double getCoinValue(String coinType) {
        switch (coinType.toLowerCase()) {
            case "gold":
                return 1000;
            case "silver":
                return 100;
            case "copper":
                return 1;
            case "tin":
                return 0.01;
            default:
                return 0;
        }
    }

    // Placeholder method to assign coin slots
    private void assignCoinSlots(Map<String, List<Integer>> coinSlotMapping) {
        coinSlotMapping.forEach((coinType, slots) -> {
            for (Integer slot : slots) {
                coinSlots.put(slot, coinType);
            }
        });
    }

    // Placeholder method to get the coin slots for a specific type
    private List<Integer> coinSlotsForType(String type) {
        return coinSlots.entrySet().stream()
                .filter(entry -> entry.getValue().equals(type))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Placeholder method to change button to a maxed item
    private void changeButtonToMaxedItem(Inventory inventory, String coinType, String reason) {
        // Get the button slot for this coin type from the map
        Integer buttonSlot = additionButtonSlots.get(coinType);

        if (buttonSlot != null) {
            ItemStack maxedItem = CustomStack.getInstance("vicky_utls:red_failed_x").getItemStack();
            ItemMeta meta = maxedItem.getItemMeta();

            meta.setDisplayName(ChatColor.RED + reason);
            List<String> loreMessages = new ArrayList<>();
            loreMessages.add(ChatColor.GRAY + "  - You cannot add more of this coin: " + coinType);
            meta.setLore(loreMessages);
            maxedItem.setItemMeta(meta);

            // Set the maxed out item in the correct button slot
            inventory.setItem(buttonSlot, maxedItem);
        } else {
            plugin.getLogger().warning("Button slot for coin type " + coinType + " not found in map.");
        }
    }

    // Placeholder method to revert the button from a maxed item
    private void revertButtonFromMaxed(Inventory inventory, double playerBalance) {
        for (Map.Entry<String, Integer> entry : additionButtonSlots.entrySet()) {
            String coinType = entry.getKey();
            int buttonSlot = entry.getValue(); // Get the button slot for this coin type

            // Iterate over all coin slots for this type to check values
            for (Map.Entry<Integer, String> coinEntry : coinSlots.entrySet()) {
                if (coinEntry.getValue().equals(coinType)) {
                    int slot = coinEntry.getKey();
                    ItemStack currentItem = inventory.getItem(slot);

                    // Ensure that the current item exists and is not air
                    if (currentItem != null && !currentItem.getType().isAir()) {
                        double coinValue = getCoinValue(coinType); // Get coin value
                        int itemAmount = currentItem.getAmount();
                        double totalCoinValue = coinValue * itemAmount;

                        // Check if adding another of this coin type won't exceed player balance
                        if (totalCoinValue + coinValue <= playerBalance) {
                            // Revert the button for this coin type to its original "add" state
                            ItemStack revertedButton = CustomStack.getInstance("vicky_utls:green_add_button").getItemStack();
                            ItemMeta meta = revertedButton.getItemMeta();
                            meta.setDisplayName(ChatColor.YELLOW + "ᴬᴰᴰ");
                            revertedButton.setItemMeta(meta);

                            // Update the correct button slot
                            inventory.setItem(buttonSlot, revertedButton);
                            plugin.getLogger().info("Reverted button for " + coinType + " at slot " + buttonSlot + " to original state.");
                            break; // Stop checking slots once one is found that can be added to
                        }
                    }
                }
            }
        }
    }
}
