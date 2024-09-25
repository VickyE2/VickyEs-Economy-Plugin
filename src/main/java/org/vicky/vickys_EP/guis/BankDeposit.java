package org.vicky.vickys_EP.guis;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.guiparent.BaseGui;
import org.v_utls.guiparent.ButtonAction;
import org.v_utls.guiparent.GuiCreator;
import org.v_utls.utilities.HexGenerator;
import org.vicky.vickys_EP.Listeners.MainDepositAndWithdrawListener;
import org.vicky.vickys_EP.VickysEconomyPlugin;
import org.vicky.vickys_EP.global.Utils;
import org.vicky.vickys_EP.utils.DepositChecker;

import java.util.Arrays;
import java.util.List;

import static org.vicky.vickys_EP.global.Listeners.mainDepositAndWithdrawListener;
import static org.vicky.vickys_EP.global.Utils.withdrawChecker;

public class BankDeposit extends BaseGui {
    private final GuiCreator guiManager;
    private final MainDepositAndWithdrawListener listener;
    boolean itemsAdderEnabled = false;
    Economy economy = VickysEconomyPlugin.getEconomy();

    public BankDeposit(JavaPlugin plugin) {
        // Pass the listener to the superclass constructor
        super(plugin, mainDepositAndWithdrawListener);

        // Initialize listener before calling super()
        this.listener = mainDepositAndWithdrawListener;

        // Initialize other fields
        this.guiManager = new GuiCreator(plugin, mainDepositAndWithdrawListener);
    }


    GuiCreator.ItemConfig IADepositButton = null;
    GuiCreator.ItemConfig IASwapButton = null;
    GuiCreator.ItemConfig IAHeadButton = null;


    DepositChecker depositChecker = Utils.depositChecker;


    @Override
    public void showGui(Player player) {

        if (Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderEnabled = true;
        }
        if (itemsAdderEnabled) {

            //green_swap_button
            IADepositButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "ᴅᴇᴘᴏsɪᴛ",
                    "14",
                    true,
                    null,
                    "vicky_utls:green_arrow_right",
                    null
            );
            listener.registerButton(new ButtonAction(Dplayer -> {
                Dplayer.sendMessage("Trying to Deposit");
                List<Integer> coinSlots = Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);
                depositChecker.depositCoins(Dplayer, coinSlots, 13,  true);  // This allows passing parameters
            }, false), 9, IADepositButton);

            IASwapButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "sᴡᴀᴘ ᴛᴏ ᴡɪᴛʜᴅʀᴀᴡ",
                    "9",
                    true,
                    null,
                    "vicky_utls:green_swap_button",
                    null
            );
            listener.registerButton(new ButtonAction(ButtonAction.ActionType.OPEN_GUI, BankWithdraw.class, plugin, true), 9, IASwapButton);


            List<String> lore = Arrays.asList(
                    ChatColor.AQUA + "Your Balance: ﾶ" + economy.getBalance(player),
                    ChatColor.AQUA + "Your balance in SCF: ",
                    ChatColor.GOLD + "   Total in Gold: " + (int) economy.getBalance(player) / 1000,
                    ChatColor.GRAY + "   Total in Silver: " + (int) economy.getBalance(player) / 100,
                    HexGenerator.getHexGradient("   Total in Copper: " + (int) economy.getBalance(player), "#B86D2B", "#E07D26"),
                    ChatColor.DARK_GRAY + "   Total in Tin: " + (int) economy.getBalance(player) / 0.01);
            IAHeadButton = new GuiCreator.ItemConfig(
                    null,
                    ChatColor.DARK_GREEN + "ʏᴏᴜʀ ᴅᴇᴛᴀɪʟs",
                    "5",
                    true,
                    null,
                    "vicky_utls:player_head_gui_size_1",
                    lore
            );
            listener.registerClickableSlots("1,10,19,28,9,18,27,36", 9);
            listener.registerCustomValidItemSlots("1,10,19,28,9,18,27,36", 9,
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
                    "vickyes_ep:oak_bank_gui_deposit", -8, IADepositButton, IASwapButton, IAHeadButton);
        } else {
            guiManager.openGUI(player, 3, 9, HexGenerator.getHexGradient("Bank", "#F8D305", "#F7BC05"),
                    false, null, 0, BalanceButton, InterestButton, DepositnWithdrawButton, Black_panes);
        }

    }
}
