package org.vicky.vickys_EP.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.vicky.utilities.RanksLister;
import org.vicky.vickys_EP.VickysEconomyPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.vicky.vickys_EP.global.Utils.manager;

public class InterestObserver {
    private final JavaPlugin plugin;
    private final Economy economy = VickysEconomyPlugin.getEconomy();
    private final Set<UUID> processedPlayers = new HashSet<>();
    private long currentDay = 0;
    private int playersPerDay = 0;

    public InterestObserver(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void Interest() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        if (offlinePlayers.length <= 70) {
            scheduler.runTaskTimer(plugin, () -> {
                plugin.getLogger().warning("Interests are about to begin. Some lag might be experienced.");
                if (manager.getBooleanValue("Interest.isEnabled")) {
                    try {
                        processAllPlayers(offlinePlayers);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0L, 24000L * manager.getIntegerValue("Interest.interestFrequency"));
        } else {
            scheduler.runTaskTimer(plugin, () -> {
                plugin.getLogger().warning("Interests are about to begin. Some lag might be experienced.");
                if (manager.getBooleanValue("Interest.isEnabled")) {

                    if (Bukkit.getWorld(manager.getStringValue("Main.defaultWorld")) != null) {
                        // Segment players over multiple Minecraft days
                        long day = Bukkit.getWorld(manager.getStringValue("Main.defaultWorld")).getFullTime() / 24000L;
                        if (day != currentDay) {
                            currentDay = day;
                            try {
                                processPlayersForTheDay(offlinePlayers);
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        plugin.getLogger().severe("The default world for time check: " + manager.getStringValue("Main.defaultWorld") + " doesn't exist please change it to the main Bukkit world");
                    }

                }
            }, 0L, 24000L);
        }
    }

    private void processAllPlayers(OfflinePlayer[] offlinePlayers) throws ExecutionException, InterruptedException {
        RanksLister lister = new RanksLister();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (offlinePlayer.hasPlayedBefore()) {
                applyInterest(offlinePlayer.getUniqueId(), lister);
            }
        }
        plugin.getLogger().info("Processed interest for all players at once.");
    }

    private void processPlayersForTheDay(OfflinePlayer[] allPlayers) throws ExecutionException, InterruptedException {
        List<OfflinePlayer> unprocessedPlayers = Arrays.stream(allPlayers)
                .filter(p -> !processedPlayers.contains(p.getUniqueId()))
                .collect(Collectors.toList());

        if (processedPlayers.isEmpty()) {
            // Calculate how many players to process each day
            playersPerDay = Math.max(1, allPlayers.length / manager.getIntegerValue("Interest.segmentDays")); // Default 10 days
        }

        List<OfflinePlayer> todaysPlayers = unprocessedPlayers.subList(0, Math.min(playersPerDay, unprocessedPlayers.size()));

        RanksLister lister = new RanksLister();
        for (OfflinePlayer player : todaysPlayers) {
            applyInterest(player.getUniqueId(), lister);
            processedPlayers.add(player.getUniqueId());
        }

        plugin.getLogger().info("Processed interest for " + todaysPlayers.size() + " players on day " + currentDay);

        if (processedPlayers.size() >= allPlayers.length) {
            // Reset the list once all players have been processed
            processedPlayers.clear();
            plugin.getLogger().info("All players have been processed. Resetting the list for the next cycle.");
        }
    }

    private void applyInterest(UUID playerUUID, RanksLister lister) throws ExecutionException, InterruptedException {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

        if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
            // Fetch the player's interest rate asynchronously
            getPlayerInterestRate(playerUUID).thenAccept(totalInterest -> {
                if (totalInterest > 0) {
                    double currentBalance = economy.getBalance(offlinePlayer);
                    if (currentBalance <= 0) {
                        return; // Skip players with no balance or players not tracked by Vault yet
                    }

                    double interest = (currentBalance * totalInterest);
                    economy.depositPlayer(offlinePlayer, interest);

                    if (manager.getBooleanValue("Interest.allowLogging")) {
                        plugin.getLogger().info("Applied Interest for Player: " + offlinePlayer.getName() + " | Interest: " + totalInterest);
                    }
                }
            }).exceptionally(ex -> {
                // Handle any exceptions here
                plugin.getLogger().severe("Failed to apply interest for player " + offlinePlayer.getName() + ": " + ex.getMessage());
                return null;
            });
        }
    }



    public CompletableFuture<Double> getPlayerInterestRate(UUID name) throws ExecutionException, InterruptedException {
        RanksLister lister = new RanksLister();

        // Fetch ranks asynchronously and calculate interest in the thenAccept block
        return lister.getPlayerRanks(name).thenApply(ranks -> {
            double totalInterest = 0.0;

            // Iterate over the player's ranks and calculate interest
            for (String currentRank : ranks) {
                totalInterest += manager.getDoubleValue("Interest.Ranks." + currentRank);
                plugin.getLogger().info("Added interest for rank " + currentRank +
                        " with value " + manager.getDoubleValue("Interest.Ranks." + currentRank) +
                        " leading to a total interest of " + totalInterest);
            }

            return totalInterest; // Return the total interest
        });
    }

}

