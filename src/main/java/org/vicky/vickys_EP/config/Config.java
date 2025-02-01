package org.vicky.vickys_EP.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.utilities.RanksLister;
import static org.vicky.vickys_EP.global.Utils.manager;

public class Config {

    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin){
        this.plugin = plugin;

    }

    RanksLister ranksLister = new RanksLister();

    public void registerConfigs() {

        if (!manager.doesPathExist("Main")){
            manager.setBracedConfigValue("Main", "","This is where the main config values go");
        }

        if (!manager.doesPathExist("Main.Bank")){
            manager.setConfigValue("Main", "Bank", "","This is where the settings for the main bank");
        }
        if (!manager.doesPathExist("Main.Bank.isEnabled")){
            manager.setConfigValue("Main.Bank", "isEnabled", true, "Used to enable or disable the /bank feature");
        }
        if (!manager.doesPathExist("Main.defaultWorld")){
            manager.setConfigValue("Main", "defaultWorld", "world", "default world to get time from.");
        }



        if (!manager.doesPathExist("Interest")){
            manager.setBracedConfigValue("Interest", "","The value of interest a particular rank gets. " +
                    "It defaults to zero but can be made automatic by setting .weightOriented to true and " +
                    ".weightChange to anything other than 0 (this is the multiplication factor)");
        }

        if (!manager.doesPathExist("Interest.isEnabled")){
            manager.setConfigValue("Interest", "isEnabled", true, null);
        }

        if (!manager.doesPathExist("Interest.allowLogging")){
            manager.setConfigValue("Interest", "allowLogging", true, null);
        }

        if (!manager.doesPathExist("Interest.weightOriented")){
            manager.setConfigValue("Interest", "weightOriented", false, null);
        }

        if (!manager.doesPathExist("Interest.weightChange")){
            manager.setConfigValue("Interest", "weightChange", 0, null);
        }
// "Interest.segmentDays"
        if (!manager.doesPathExist("Interest.interestFrequency")){
            manager.setConfigValue("Interest", "interestFrequency", 2, null);
        }

        if (!manager.doesPathExist("Interest.segmentDays")){
            manager.setConfigValue("Interest", "segmentDays", 5, null);
        }

        if (!manager.doesPathExist("Interest.Ranks")){
            manager.setConfigValue("Interest", "Ranks", "", null);
        }

        if (!manager.getBooleanValue("Interest.weightOriented")) {
            String[] availableRanks = ranksLister.getAllRanks();
            for (String rank : availableRanks) {
                if (!manager.doesPathExist("Interest.Ranks." + rank)) {
                    manager.setConfigValue("Interest.Ranks.", rank, 0, null);
                }
            }
        }else{
            double weightedValue = (manager.getDoubleValue("Interest.weightChange") == 0.0) ? 0.03 :
                    manager.getDoubleValue("Interest.weightChange");
            String[][] availableWeightedRanks = ranksLister.getAllRanksWithWeights(weightedValue);
            // Iterate over each rank and weight, and save to YAML
            for (String[] rankWithWeight : availableWeightedRanks) {
                String rank = rankWithWeight[0];  // Rank name
                String weight = rankWithWeight[1];  // Multiplied weight
                manager.setConfigValue("Interest.Ranks.", rank, Double.parseDouble(weight), null);
            }
        }
    }
}
