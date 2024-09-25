package org.vicky.vickys_EP.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.guiparent.BaseGui;
import org.v_utls.guiparent.ButtonAction;
import org.v_utls.guiparent.GuiCreator;
import org.v_utls.utilities.HexGenerator;
import org.vicky.vickys_EP.Listeners.MainBankGuiListener;
import org.vicky.vickys_EP.VickysEconomyPlugin;
import org.vicky.vickys_EP.utils.ReturnPlayerValueFormatted;

import static org.vicky.vickys_EP.global.Listeners.mainBankGuiListener;

public class BankGuiMain extends BaseGui{
    private final GuiCreator guiManager;
    ReturnPlayerValueFormatted playerValueFormatted = new ReturnPlayerValueFormatted(VickysEconomyPlugin.getEconomy());
    private final MainBankGuiListener listener;
    private final JavaPlugin plugin;
    boolean itemsAdderEnabled = false;

    public BankGuiMain(JavaPlugin plugin) {
        // Pass the listener to the superclass constructor
        super(plugin, mainBankGuiListener);

        // Initialize listener before calling super()
        this.listener = mainBankGuiListener;
        this.plugin = plugin;

        // Initialize other fields
        this.guiManager = new GuiCreator(plugin, mainBankGuiListener);
    }


    GuiCreator.ItemConfig IABalanceButton = null;
    GuiCreator.ItemConfig IAInterestButton = null;
    GuiCreator.ItemConfig IADnWButton = null;



    @Override
    public void showGui(Player player) {

        if (Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderEnabled = true;
        }
        if (itemsAdderEnabled) {
            if (player.getWorld().getBiome(player.getLocation()).equals(Biome.PLAINS) || player.getWorld().getBiome(player.getLocation()).equals(Biome.SNOWY_PLAINS) || player.getWorld().getBiome(player.getLocation()).equals(Biome.SUNFLOWER_PLAINS)) {
                IABalanceButton = new GuiCreator.ItemConfig(
                        null,
                        ChatColor.DARK_GREEN + "Balance",
                        "29",
                        true,
                        null,
                        "vickyes_ep:oak_balance_button",
                        null
                );
                listener.registerButton(new ButtonAction(player1 -> playerValueFormatted.LogAll(player1), true), 9, IABalanceButton);

                IAInterestButton = new GuiCreator.ItemConfig(
                        null,
                        ChatColor.YELLOW + "Interest Rates",
                        "32",
                        true,
                        null,
                        "vickyes_ep:oak_interest_button",
                        null
                );
                listener.registerButton(new ButtonAction(ButtonAction.ActionType.OPEN_GUI, BankGuiMain.class, plugin, true), 9, IAInterestButton);

                IADnWButton = new GuiCreator.ItemConfig(
                        null,
                        ChatColor.DARK_GREEN + "Deposit or Withdraw",
                        "35",
                        true,
                        null,
                        "vickyes_ep:oak_dnw_button",
                        null
                );
                listener.registerButton(new ButtonAction(ButtonAction.ActionType.OPEN_GUI, BankDeposit.class, plugin, true), 9, IADnWButton);

            }
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
                ChatColor.DARK_GREEN + "Balance",
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
                    "vickyes_ep:oak_bank_gui_main", -8, IABalanceButton, IAInterestButton, IADnWButton);
        } else {
           guiManager.openGUI(player, 3, 9, HexGenerator.getHexGradient("Bank", "#F8D305", "#F7BC05"),
                    false, null, 0,BalanceButton, InterestButton, DepositnWithdrawButton, Black_panes);
        }

    }
}
