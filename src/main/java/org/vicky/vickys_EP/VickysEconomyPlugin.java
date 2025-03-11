package org.vicky.vickys_EP;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ConfigManager;
import org.vicky.utilities.FileManager;
import org.vicky.vicky_utils;
import org.vicky.vickys_EP.Listeners.MainBankGuiListener;
import org.vicky.vickys_EP.Listeners.MainDepositAndWithdrawListener;
import org.vicky.vickys_EP.config.Config;
import org.vicky.vickys_EP.utils.CommandManager;
import org.vicky.vickys_EP.utils.DepositChecker;
import org.vicky.vickys_EP.utils.InterestObserver;
import org.vicky.vickys_EP.utils.WithdrawChecker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.vicky.vickys_EP.global.Listeners.mainBankGuiListener;
import static org.vicky.vickys_EP.global.Listeners.mainDepositAndWithdrawListener;
import static org.vicky.vickys_EP.global.Utils.*;

public final class VickysEconomyPlugin extends JavaPlugin {

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    public static VickysEconomyPlugin plugin;

    public static VickysEconomyPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        vicky_utils.hookDependantPlugin(this, this.getClassLoader());
    }

    @Override
    public void onEnable() {
        getLogger().info(ANSIColor.colorize("VickyE's Economy Plugin is Being Enabled", ANSIColor.PURPLE_BOLD));
        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("Vicky-s_Utilities") != null){
            this.getDataFolder().mkdirs();
            if (!setupEconomy()) {
                getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            if (!setupPermissions()) {
                getLogger().severe(String.format("[%s] - Disabled due to no Vault Permission failure!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null){
                FileManager fileManager = new FileManager(this);
                List<String> files = Arrays.asList("contents/vickyes_ep");
                fileManager.extractDefaultIAAssets(files);
            }else{
                getLogger().info("ItemsAdder isn't present. Defaulting to untextured gui's");
            }

            try {
                if (getCommand("vep_bank") == null) {
                    getLogger().severe("The command /vep_bank is not registered! Check your plugin.yml");
                } else {
                    Objects.requireNonNull(getCommand("vep_bank")).setExecutor(new CommandManager(this));
                    getLogger().info("/vep_bank command registered.");
                    Objects.requireNonNull(getCommand("config")).setExecutor(new CommandManager(this));
                    getLogger().info("/config command registered.");
                }
            } catch (Exception e) {
                getLogger().severe("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }

            mainBankGuiListener = new MainBankGuiListener(this);
            mainDepositAndWithdrawListener = new MainDepositAndWithdrawListener(this);
            depositChecker = new DepositChecker(this);
            withdrawChecker = new WithdrawChecker(this);
            manager = new ConfigManager(this, "config.yml");
            Config config = new Config(this);
            config.registerConfigs();

            InterestObserver observer = new InterestObserver(this);
            observer.Interest();

            getServer().getPluginManager().registerEvents(mainBankGuiListener, this);
            getServer().getPluginManager().registerEvents(mainDepositAndWithdrawListener, this);

        }else{
            getLogger().severe("Necessary Dependency 'Vicky's Utilities' is missing. Disabling Plugin");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        vicky_utils.unhookDependantPlugin(this);
    }



    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault is not installed or not enabled!");
            return false;
        }

        getLogger().info("Vault found, looking for an economy provider...");

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy plugin is providing the Economy service to Vault!");
            return false;
        }

        econ = rsp.getProvider();

        if (econ == null) {
            getLogger().severe("Economy provider is null!");
        } else {
            getLogger().info("Successfully hooked into " + econ.getName());
        }

        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }
}
