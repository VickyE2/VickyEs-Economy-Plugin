package org.vicky.vickys_EP.Listeners;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.vicky.guiparent.ButtonAction;
import org.vicky.listeners.BaseGuiListener;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.ANSIColor;
import org.vicky.vickys_EP.VickysEconomyPlugin;
import org.vicky.vickys_EP.global.Utils;

import java.util.*;

public class MainDepositAndWithdrawListener extends BaseGuiListener {
    private final Map<Integer, ButtonAction> buttonActions = new HashMap<>();
    private final Map<Integer, List<ItemStack>> validItemSlots = new HashMap<>();
    private final Map<Integer, List<String>> validCustomItemSlots = new HashMap<>();
    private final Set<Integer> clickableSlots = new HashSet<>();

    public MainDepositAndWithdrawListener(VickysEconomyPlugin plugin) {
        super(plugin);
        addInventoryClickHandler(event -> {
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (buttonActions.containsKey(slot)) {
                ButtonAction action = buttonActions.get(slot);
                action.execute(player, plugin);
            }

            if (clickableSlots.contains(slot) || buttonActions.containsKey(slot)) {
                if (validItemSlots.containsKey(slot) || validCustomItemSlots.containsKey(slot)) {
                    ItemStack itemStackInSlot = event.getInventory().getItem(slot);

                    if (itemStackInSlot != null) {
                        CustomStack itemInContact = CustomStack.byItemStack(itemStackInSlot);

                        boolean isValid = false;  // Declare `isValid` before the loop
                        for (String namespace : validCustomItemSlots.get(slot)) {
                            if (itemInContact != null && itemInContact.getNamespacedID().equals(namespace)) {
                                isValid = true;
                                break;  // If valid, exit loop
                            }
                        }

                        // Now you can use `isValid` to take action
                        if (!isValid) {
                            // Handle the case where the item is not valid
                            // Example: Send a message, cancel the action, etc.
                            player.sendMessage(ChatColor.RED + "Invalid coin in slot " + slot);
                            event.setCancelled(true);
                        } else {
                            // Handle the valid case, e.g., process the item
                            event.setCancelled(false);
                        }
                    }


                }
            }else {
                player.sendMessage(ChatColor.RED + "This slot is not clickable!");
            }
        });
        addInventoryCloseHandler(event -> {
                buttonActions.remove(event.getInventory());
                validItemSlots.remove(event.getInventory());
                clickableSlots.remove(event.getInventory());
        });
    }

    private Set<Integer> parseSlots(String slotRange, int width) {
        Set<Integer> slots = new HashSet<>();
        String[] parts = slotRange.split(",");

        for (String part : parts) {
            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = Math.max(0, Integer.parseInt(range[0]) - 1);
                int end = Math.min(width * 9 - 1, Integer.parseInt(range[1]) - 1);
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } else {
                int slot = Integer.parseInt(part) - 1;
                slots.add(slot);
            }
        }
        return slots;
    }

    public void registerClickableSlots(String slotRange) {
        Set<Integer> slots = parseSlots(slotRange, 9);
        clickableSlots.addAll(slots);
        Bukkit.getLogger().info(ANSIColor.colorize("Registered clickable slots: " + slots, ANSIColor.CYAN));
    }

    public void registerValidItemSlots(String slotRange, ItemStack... itemStacks) {
        Set<Integer> slots = parseSlots(slotRange, 9);
        for (int slot : slots) {
            validItemSlots.put(slot, new ArrayList<>(List.of(itemStacks)));
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Registered valid item slots for items: " + Arrays.toString(itemStacks) + " in slots: " + slots, ANSIColor.CYAN));    }
    public void registerCustomValidItemSlots(String slotRange, String... customStacks) {
        Set<Integer> slots = parseSlots(slotRange, 9);
        for (int slot : slots) {
            validCustomItemSlots.put(slot, new ArrayList<>(List.of(customStacks)));
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Registered valid item slots for items: " + Arrays.toString(customStacks) + " in slots: " + slots, ANSIColor.CYAN));    }


    public void registerADSBButton(ButtonAction action, int slot) {
        Bukkit.getLogger().info(ANSIColor.colorize("Button has been registered with Action " + action.getActionType() + " and action data: " + action.getActionData(), ANSIColor.CYAN));
        buttonActions.put(slot, action);

    }
}
