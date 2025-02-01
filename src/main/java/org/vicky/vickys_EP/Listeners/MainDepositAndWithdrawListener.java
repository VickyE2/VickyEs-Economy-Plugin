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
    private Inventory guiInventory = null;
    private final VickysEconomyPlugin plugin;

    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 200; // Milliseconds

    public MainDepositAndWithdrawListener(VickysEconomyPlugin plugin) {
        this.plugin = plugin;
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

    public void registerClickableSlots(String slotRange, int guiWidth) {
        Set<Integer> slots = parseSlots(slotRange, guiWidth);
        clickableSlots.addAll(slots);
        Bukkit.getLogger().info(ANSIColor.colorize("Registered clickable slots: " + slots, ANSIColor.CYAN));
    }

    public void registerValidItemSlots(String slotRange, int guiWidth, ItemStack... itemStacks) {
        Set<Integer> slots = parseSlots(slotRange, guiWidth);
        for (int slot : slots) {
            validItemSlots.put(slot, new ArrayList<>(List.of(itemStacks)));
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Registered valid item slots for items: " + Arrays.toString(itemStacks) + " in slots: " + slots, ANSIColor.CYAN));    }
    public void registerCustomValidItemSlots(String slotRange, int guiWidth, String... customStacks) {
        Set<Integer> slots = parseSlots(slotRange, guiWidth);
        for (int slot : slots) {
            validCustomItemSlots.put(slot, new ArrayList<>(List.of(customStacks)));
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Registered valid item slots for items: " + Arrays.toString(customStacks) + " in slots: " + slots, ANSIColor.CYAN));    }

    @Override
    public void setGuiInventory(Inventory guiInventory) {
        if (guiInventory == null) {
            Bukkit.getLogger().warning("GUI inventory is being set to null! Make sure to initialize it before use.");
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Inventory [Depo-With] has been set to: " + guiInventory, ANSIColor.CYAN));
        this.guiInventory = guiInventory;
    }

    public Inventory getGuiInventory() {
        if (guiInventory == null) {
            Bukkit.getLogger().warning("There is no Inventory");
            return null;
        }
        Bukkit.getLogger().info(ANSIColor.colorize("Inventory is currently: " + guiInventory, ANSIColor.CYAN));
        return guiInventory;

    }

    public void registerButton(ButtonAction action, int GuiWidth, GuiCreator.ItemConfig... itemConfigs) {
        Bukkit.getLogger().info(ANSIColor.colorize("Button has been registered with Action " + action.getActionType() + " and action data: " + action.getActionData(), ANSIColor.CYAN));
        for (GuiCreator.ItemConfig itemConfig : itemConfigs) {
            Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange(), GuiWidth);
            for (int slot : slotSet) {
                buttonActions.put(slot, action);
            }
        }
    }

    public void registerADSBButton(ButtonAction action, int slot) {
        Bukkit.getLogger().info(ANSIColor.colorize("Button has been registered with Action " + action.getActionType() + " and action data: " + action.getActionData(), ANSIColor.CYAN));
        buttonActions.put(slot, action);

    }

    @Override
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != guiInventory) {
            event.setCancelled(false);
            return;
        }
        if (event.getClickedInventory() == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DELAY) {
            event.setCancelled(true); // Cancel the event to prevent processing
            return; // Ignore this click
        }
        lastClickTime = currentTime;

        if (guiInventory == null) {
            event.setCancelled(false);
            Bukkit.getLogger().warning("Attempted to click an inventory, but guiInventory is null!  [Depo-With]");
            return;
        }



        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (buttonActions.containsKey(slot)) {
            ButtonAction action = buttonActions.get(slot);
            action.execute(player, plugin);
        }

        if (clickableSlots.contains(slot) || buttonActions.containsKey(slot)) {
            if (validItemSlots.containsKey(slot) || validCustomItemSlots.containsKey(slot)) {
                ItemStack itemStackInSlot = guiInventory.getItem(slot);

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
    }

    @Override
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == guiInventory) {
            buttonActions.clear();
            validItemSlots.clear();
            clickableSlots.clear();
            setGuiInventory(null);
        }
    }

    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        return item1.isSimilar(item2);
    }
}
