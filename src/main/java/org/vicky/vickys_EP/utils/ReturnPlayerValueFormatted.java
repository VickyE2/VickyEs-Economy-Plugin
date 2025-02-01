package org.vicky.vickys_EP.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.vicky.items_adder.FontImageSender;
import org.vicky.utilities.HexGenerator;

import java.util.Objects;

public class ReturnPlayerValueFormatted {
    private final Economy economy;
    public ReturnPlayerValueFormatted(Economy economy) {
        this.economy = economy;
    }

    // Converts Vault economy balance to a custom coin system
    public void LogAll(Player player) {
        double balance = economy.getBalance(player);
        player.sendMessage("[=================[Your-Balance]=================]");
        player.sendMessage(ChatColor.AQUA + "Current Amount of money in Bank: ﾶ" + ChatColor.BLUE + balance);
        player.sendMessage("In Standard Coin Format, SCF, You have: ");
        player.sendMessage(ChatColor.GOLD + formatCoins(balance, "gold") + HexGenerator.getHexGradient(" Gold Coins  ", "#FFD500", "#ECC500") + ChatColor.RESET + FontImageSender.getImage("vickyes_ep:gold_coin_unanimated") + ",");
        player.sendMessage(ChatColor.GRAY + formatCoins(balance, "silver") + HexGenerator.getHexGradient(" Silver Coins  ", "#CDCDCD", "#B4B3AA") + ChatColor.RESET + FontImageSender.getImage("vickyes_ep:silver_coin_unanimated") + ",");
        player.sendMessage(HexGenerator.getHexGradient(formatCoins(balance, "bronze") + " Copper Coins  ", "#B86D2B", "#E07D26") + FontImageSender.getImage("vickyes_ep:copper_coin_unanimated") + ", and");
        player.sendMessage(ChatColor.DARK_GRAY + formatCoins(balance, "tin") + HexGenerator.getHexGradient(" Tin Coins  ", "#E2E2E2", "#D2CBD2") + ChatColor.RESET + FontImageSender.getImage("vickyes_ep:tin_coin_unanimated"));

    }

    public String formatCoins(double totalAmount, String coin) {
        // Define the coin values
        double goldValue = 1000.00;
        double silverValue = 100.00;
        double bronzeValue = 1.00;
        double tinValue = 0.01;

        // Calculate the number of Gold, Silver, and Bronze coins
        double goldCoins = totalAmount / goldValue;
        double remainderAfterGold = totalAmount % goldValue;

        double silverCoins = remainderAfterGold / silverValue;
        double remainderAfterSilver = remainderAfterGold % silverValue;

        double bronzeCoins = remainderAfterSilver / bronzeValue;
        double remainderAfterBronze = remainderAfterSilver % bronzeValue;

        double tinCoins = remainderAfterBronze / tinValue;

        if (Objects.equals(coin, "gold")){
            return Integer.toString((int) goldCoins);
        }

        if (Objects.equals(coin, "silver")){
            return Integer.toString((int) silverCoins);
        }

        if (Objects.equals(coin, "bronze")){
            return Integer.toString((int) bronzeCoins);
        }

        if (Objects.equals(coin, "tin")){
            return Integer.toString((int) tinCoins);
        }

        return coin;
    }
}
