package org.vicky.vickys_EP.config;

import org.v_utls.utilities.RanksLister;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.util.HashMap;

import static org.vicky.vickys_EP.global.Utils.config;

public class Config {
    public final VickysEconomyPlugin plugin;

    public Config(VickysEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    RanksLister ranksLister = new RanksLister();

    public void registerConfigs() {

        if (!config.doesPathExist("Main_Configurations")) {
            config.updateConfigValue("Main_Configurations", new HashMap<>(), "This is where the main plugin configurations go");
        }
        if (!config.doesPathExist("Main_Configurations.Bank_is_Enabled")) {
            config.updateConfigValue("Main_Configurations.Bank_is_Enabled", true);
        }

        if (!config.doesPathExist("Rank_Interest_Rate")) {
            config.updateConfigValue("Rank_Interest_Rate", new HashMap<>(), "This is where the interest rates for each rank is configured :]");
        }
        if (!config.doesPathExist("Rank_Interest_Rate.Enabled")) {
            config.updateConfigValue("Rank_Interest_Rate.Enabled", true);
        }
        String[] ranks = ranksLister.getAllRanks();
        for (String rank : ranks) {
            String rankPath = "Rank_Interest_Rate." + rank;
            if (!config.doesPathExist(rankPath)) {
                config.updateConfigValue(rankPath, 0);
            }
        }

    }

}
