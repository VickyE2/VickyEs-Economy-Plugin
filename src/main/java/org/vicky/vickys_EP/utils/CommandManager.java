package org.vicky.vickys_EP.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.vickys_EP.VickysEconomyPlugin;
import org.vicky.vickys_EP.config.Config;
import org.vicky.vickys_EP.guis.BankGuiMain;

import static org.vicky.vickys_EP.global.Utils.config;

public class CommandManager implements CommandExecutor {

    private final VickysEconomyPlugin plugin;

    public CommandManager(VickysEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vep_bank")) {
            if (config.getBooleanValue("Main_Configurations.Bank_is_Enabled")) {
                if (sender instanceof Player player) {
                    BankGuiMain bankGuiMain = new BankGuiMain(plugin);
                    bankGuiMain.showGui(player);
                } else if (sender instanceof ConsoleCommandSender console) {
                    console.sendMessage("You can't open a GUI, you dum dum ._.");
                }
            }else{
                sender.sendMessage(ChatColor.RED + "Bank is currently disabled in the config. Enable it to use this command");
            }
        } else if(command.getName().equalsIgnoreCase("config")){
            if ((args.length > 0 && args[0].equalsIgnoreCase("generate"))){
                Config config1 = new Config(plugin);
                config1.registerConfigs();
                config.reloadPluginConfig();
                sender.sendMessage("Config has generated all values.");
            }else if ((args.length > 0 && args[0].equalsIgnoreCase("reload"))){
                config.reloadPluginConfig();
                sender.sendMessage("Config values have been reloaded");
            }
        }else {
            sender.sendMessage("Unknown command. Use " + ChatColor.GOLD + "/" + command.getName() + " help " + ChatColor.RESET + " for a list of available commands.");
        }
        return true;
    }
}
