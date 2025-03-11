package org.vicky.vickys_EP.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.vicky.guiparent.ButtonAction;
import org.vicky.listeners.BaseGuiListener;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.ANSIColor;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainBankGuiListener extends BaseGuiListener {
    private Inventory guiInventory = null;
    private final VickysEconomyPlugin plugin;

    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 200; // Milliseconds

    public MainBankGuiListener(VickysEconomyPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }
}

