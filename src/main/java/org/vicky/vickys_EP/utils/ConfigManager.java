package org.vicky.vickys_EP.utils;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final VickysEconomyPlugin plugin;
    private YamlConfigurationLoader loader;
    private CommentedConfigurationNode rootNode;

    public ConfigManager(VickysEconomyPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    // Create and load the configuration file
    public void createConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        loader = YamlConfigurationLoader.builder().file(configFile).build();

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false); // Save the default config file if it doesn't exist
        }

        loadConfigValues();
    }

    // Load the configuration values
    public void loadConfigValues() {
        try {
            rootNode = loader.load(ConfigurationOptions.defaults());
            plugin.getLogger().info("Config loaded successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load config.yml!");
            e.printStackTrace();
        }
    }

    // Add a comment to a specific path in the configuration
    public void addComment(String path, String comment) {
        CommentedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
        node.comment(comment);

        saveConfig(); // Save the configuration after adding the comment
    }

    // Update a specific value in the config and optionally add a comment
    public void updateConfigValue(String path, Object value, String comment) {
        try {
            // Split the path into nodes and navigate to the desired location
            CommentedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));

            // Set the value for the node
            node.set(value);

            // Add a comment if provided
            if (comment != null && !comment.isEmpty()) {
                node.comment(comment);
            }

            // Save the updated configuration
            saveConfig();
        } catch (org.spongepowered.configurate.serialize.SerializationException e) {
            plugin.getLogger().severe("Failed to update config value at path: " + path + ". Error: " + e.getMessage());
            e.printStackTrace(); // Optional: print the stack trace for debugging
        }
    }

    // Update a value without adding a comment
    public void updateConfigValue(String path, Object value) {
        updateConfigValue(path, value, null); // Calls the method above without adding a comment
    }

    // Check if a path exists in the configuration
    public boolean doesPathExist(String path) {
        return !rootNode.node((Object[]) path.split("\\.")).virtual();
    }

    // Save the configuration to disk
    private void saveConfig() {
        try {
            loader.save(rootNode);
            plugin.getLogger().info("Config saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    // Reload the configuration
    public void reloadPluginConfig() {
        loadConfigValues();
        plugin.getLogger().info("Config reloaded successfully.");
    }

    // Retrieve a value from the configuration
    public Object getConfigValue(String path) {
        try {
            // Retrieve the value from the node at the specified path
            return rootNode.node((Object[]) path.split("\\.")).get(Object.class);
        } catch (org.spongepowered.configurate.serialize.SerializationException e) {
            plugin.getLogger().severe("Failed to get config value at path: " + path + ". Error: " + e.getMessage());
            e.printStackTrace(); // Optional: print the stack trace for debugging
            return null; // Return null or an appropriate default value if retrieval fails
        }
    }

    // Example of getting a boolean value
    public boolean getBooleanValue(String path) {
        return rootNode.node((Object[]) path.split("\\.")).getBoolean();
    }

    // Example of getting a string value
    public String getStringValue(String path) {
        return rootNode.node((Object[]) path.split("\\.")).getString();
    }
}
