package me.notro.bettingplus.managers;

import lombok.Getter;
import lombok.NonNull;
import me.notro.bettingplus.BettingPlus;
import me.notro.bettingplus.models.Bet;
import me.notro.bettingplus.utils.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BettingManager {

    @Getter
    private BukkitTask expiryTask;

    private final BettingPlus plugin;
    private final List<UUID> requests = new ArrayList<>();
    private final List<UUID> queues = new ArrayList<>();

    public BettingManager(BettingPlus plugin) {
        this.plugin = plugin;
    }

    public void requestBet(@NonNull Player requester, @NonNull Player target ,@NonNull Bet bet) {
        if (requester == target) {
            requester.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou can't offer yourself a bet&7.")));
            return;
        }

        Component acceptMessage = sendClickableCommand("&7[&aAccept&7] ", "bet accept " + requester.getName());
        Component denyMessage = sendClickableCommand(" &7[&cDeny&7]", "bet deny " + requester.getName());

        addRequest(target);
        requester.sendMessage(Message.fixColor("&7Successfully sent a bet offer to &c" + target.getName() + "&7."));
        target.sendMessage(Message.fixColor("&c" + requester.getName() + " &7offered a bet of &a" + bet.getRequestedCash() + "$&7. ").append(acceptMessage).append(denyMessage));
        target.sendMessage(Message.fixColor("&cYou have 60 seconds to accept&7!"));
    }

    public void startExpiryCount(@NonNull Player requester, @NonNull Player target) {
        expiryTask = new BukkitRunnable() {
            int counter = 1;
            @Override
            public void run() {
                if (counter <= 60) {
                    counter++;
                    return;
                }

                cancel();
                removeRequest(target);
                target.sendMessage(Message.getPrefix().append(Message.fixColor("&7Bet offer from &c" + requester.getName() + " &7has been expired.")));
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void chooseRandomWinner(@NonNull Player requester, @NonNull Player target, @NonNull Bet bet) {
        final int numberOfPlayers = 2;
        final int chosenPlayerIndex = ThreadLocalRandom.current().nextInt(numberOfPlayers);

        if (chosenPlayerIndex == 0) {
            addCoins(requester, bet.getRequestedCash() * 2);
            requester.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have won &a" + bet.getRequestedCash() + "$")));
            target.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou have lost &a" + bet.getRequestedCash() + "$")));
            removeQueue(requester, target);
            return;
        }

        addCoins(target, bet.getRequestedCash() * 2);
        removeQueue(requester, target);
        target.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have won &a" + bet.getRequestedCash() + "$")));
        requester.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou have lost &a" + bet.getRequestedCash() + "$")));
    }

    public void startGame(@NonNull Player requester, @NonNull Player target ,@NonNull Bet bet) {
        requester.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have accepted a bet offer from &c" + target.getName() + "&7.")));
        target.sendMessage(Message.getPrefix().append(Message.fixColor("&c" + requester.getName() + " &7accepted your bet.")));

        removeCoins(requester, bet.getRequestedCash());
        removeCoins(target, bet.getRequestedCash());

        new BukkitRunnable() {
            int counter = 3;
            @Override
            public void run() {
                if (counter >= 1) {
                    requester.showTitle(Title.title(Message.fixColor("&cStarting in&7:"), Message.fixColor(counter + " second(s)!")));
                    target.showTitle(Title.title(Message.fixColor("&cStarting in&7:"), Message.fixColor(counter + " second(s)!")));
                    counter--;
                    return;
                }

                cancel();
                chooseRandomWinner(requester, target, bet);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private Component sendClickableCommand(@NonNull String message, @NonNull String command) {
        Component component = Message.fixColor(message);
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        return component;
    }

    private void addCoins(@NonNull Player target, double amount) {
        PersistentDataContainer data = target.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "cash");

        data.set(key, PersistentDataType.DOUBLE, getCoins(target) + amount);
    }

    private void removeCoins(@NonNull Player target, double amount) {
        PersistentDataContainer data = target.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "cash");

        data.set(key, PersistentDataType.DOUBLE, getCoins(target) - amount);
    }

    public double getCoins(@NonNull Player target) {
        PersistentDataContainer data = target.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "cash");

        return data.getOrDefault(key, PersistentDataType.DOUBLE, 500D);
    }

    public void addRequest(@NonNull Player target) {
        requests.add(target.getUniqueId());
    }

    public void removeRequest(@NonNull Player target) {
        requests.remove(target.getUniqueId());
    }

    public boolean hasRequests(@NonNull Player target) {
        return requests.contains(target.getUniqueId());
    }

    public void addQueue(@NonNull Player requester, @NonNull Player target) {
        queues.add(requester.getUniqueId());
        queues.add(target.getUniqueId());
    }

    public void removeQueue(@NonNull Player requester, @NonNull Player target) {
        queues.remove(requester.getUniqueId());
        queues.remove(target.getUniqueId());
    }
}