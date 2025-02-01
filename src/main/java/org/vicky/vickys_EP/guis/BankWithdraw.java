package org.vicky.vickys_EP.guis;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.guiparent.BaseGui;
import org.vicky.guiparent.ButtonAction;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.HexGenerator;
import org.vicky.vickys_EP.Listeners.MainDepositAndWithdrawListener;
import org.vicky.vickys_EP.VickysEconomyPlugin;
import org.vicky.vickys_EP.global.Utils;
import org.vicky.vickys_EP.utils.DepositChecker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.vicky.vickys_EP.global.Listeners.mainDepositAndWithdrawListener;
import static org.vicky.vickys_EP.global.Utils.withdrawChecker;

public class BankWithdraw extends BaseGui {
    private final GuiCreator guiManager;
    private final MainDepositAndWithdrawListener listener;
    boolean itemsAdderEnabled = false;
    Economy economy = VickysEconomyPlugin.getEconomy();

    public BankWithdraw(JavaPlugin plugin) {
        // Pass the listener to the superclass constructor
        super(plugin, mainDepositAndWithdrawListener);

        // Initialize listener before calling super()
        this.listener = mainDepositAndWithdrawListener;

        // Initialize other fields
        this.guiManager = new GuiCreator(plugin, mainDepositAndWithdrawListener);
    }


    GuiCreator.ItemConfig IASwapButton = null;
    GuiCreator.ItemConfig IAAddButton = null;
    GuiCreator.ItemConfig IASUBButton = null;
    GuiCreator.ItemConfig IAWithdrawButton = null;
    GuiCreator.ItemConfig IAPlayerHead = null;


    DepositChecker depositChecker = Utils.depositChecker;


    @Override
    public void showGui(Player player) {

        if (Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderEnabled = true;
        }
        if (itemsAdderEnabled) {

            IASwapButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "sᴡᴀᴘ ᴛᴏ ᴅᴇᴘᴏsɪᴛ",
                    "9",
                    true,
                    null,
                    "vicky_utls:green_swap_button",
                    null
            );
            listener.registerButton(new ButtonAction(ButtonAction.ActionType.OPEN_GUI, BankDeposit.class, plugin, true), 9, IASwapButton);

            IAAddButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.YELLOW + "ᴬᴰᴰ",
                    "1,3,6,8",
                    true,
                    null,
                    "vicky_utls:green_add_button",
                    null
            );
            IASUBButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.YELLOW + "sᴜʙᴛʀᴀᴄᴛ",
                    "28,30,33,35",
                    true,
                    null,
                    "vicky_utls:red_sub_button",
                    null
            );
            IAWithdrawButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "ᴡɪᴛʜᴅʀᴀᴡ",
                    "32",
                    true,
                    null,
                    "vicky_utls:green_arrow_right",
                    null
            );

            Map<String, List<Integer>> coinSlotMapping = new HashMap<>();
            coinSlotMapping.put("gold", Arrays.asList(9, 10, 18, 19));
            coinSlotMapping.put("silver", Arrays.asList(11, 12, 20, 21));
            coinSlotMapping.put("copper", Arrays.asList(14, 15, 23, 24));
            coinSlotMapping.put("tin", Arrays.asList(16, 17, 25, 26));

            withdrawChecker.setButton(0, true, false, "gold", player, listener, coinSlotMapping);
            withdrawChecker.setButton(2, true, false, "silver", player, listener, coinSlotMapping);
            withdrawChecker.setButton(5, true, false, "copper", player, listener, coinSlotMapping);
            withdrawChecker.setButton(7, true, false, "tin", player, listener, coinSlotMapping);
            withdrawChecker.setButton(27, false, false, "gold", player, listener, coinSlotMapping);
            withdrawChecker.setButton(29, false, false, "silver", player, listener, coinSlotMapping);
            withdrawChecker.setButton(32, false, false, "copper", player, listener, coinSlotMapping);
            withdrawChecker.setButton(34, false, false, "tin", player, listener, coinSlotMapping);
            withdrawChecker.setButton(31, false, true, null, player, listener, coinSlotMapping);

            List<String> lore = Arrays.asList(
                    ChatColor.AQUA + "Your Balance: ﾶ" + economy.getBalance(player),
                    ChatColor.AQUA + "Your balance in SCF: ",
                    ChatColor.GOLD + "   Total in Gold: " + (int) economy.getBalance(player) / 1000,
                    ChatColor.GRAY + "   Total in Silver: " + (int) economy.getBalance(player) / 100,
                    HexGenerator.getHexGradient("   Total in Copper: " + (int) economy.getBalance(player), "#B86D2B", "#E07D26"),
                    ChatColor.DARK_GRAY + "   Total in Tin: " + (int) economy.getBalance(player) / 0.01);
            IAPlayerHead = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "ʏᴏᴜʀ ᴅᴇᴛᴀɪʟs",
                    "5",
                    true,
                    null,
                    "vicky_utls:player_head_gui_size_1",
                    lore
            );
            listener.registerClickableSlots("7,16,25,34,6,15,24,33,31", 9);
            listener.registerCustomValidItemSlots("9,10,18,19,11,12,20,21,14,15,23,24,16,17,25,26", 9,
                    "vickyes_ep:gold_coin",
                    "vickyes_ep:silver_coin",
                    "vickyes_ep:copper_coin",
                    "vickyes_ep:tin_coin");
        }

        GuiCreator.ItemConfig Black_panes = new GuiCreator.ItemConfig(
                Material.BLACK_STAINED_GLASS_PANE,
                ChatColor.DARK_GREEN + "",
                "1-9,10,12,13,15,16,18,19-27",
                false,
                null,
                null,
                null
        );
        GuiCreator.ItemConfig BalanceButton = new GuiCreator.ItemConfig(
                Material.GREEN_STAINED_GLASS_PANE,
                ChatColor.DARK_GREEN + "Deposit",
                "11",
                true,
                null,
                null,
                null
        );
        GuiCreator.ItemConfig InterestButton = new GuiCreator.ItemConfig(
                Material.GREEN_STAINED_GLASS_PANE,
                ChatColor.YELLOW + "Interest Rates",
                "14",
                true,
                null,
                null,
                null
        );
        GuiCreator.ItemConfig DepositnWithdrawButton = new GuiCreator.ItemConfig(
                Material.GREEN_STAINED_GLASS_PANE,
                ChatColor.DARK_GREEN + "Deposit or Withdraw",
                "17",
                true,
                null,
                null,
                null
        );

        if (itemsAdderEnabled) {
            guiManager.openGUI(player, 4, 9, "", true,
                    "vickyes_ep:oak_bank_gui_withdraw", -8, IASwapButton, IAAddButton, IASUBButton, IAWithdrawButton,IAPlayerHead);
        } else {
            guiManager.openGUI(player, 3, 9, HexGenerator.getHexGradient("Bank", "#F8D305", "#F7BC05"),
                    false, null, 0, BalanceButton, InterestButton, DepositnWithdrawButton, Black_panes);
        }

    }
}
