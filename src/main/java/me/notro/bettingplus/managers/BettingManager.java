package me.notro.bettingplus.managers;

import me.notro.bettingplus.BettingPlus;
import me.notro.bettingplus.utils.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class BettingManager {

    private final BettingPlus plugin;
    private final HashMap<UUID, Integer> coins = new HashMap<>();
    private final HashMap<UUID, Long> requests = new HashMap<>();

    public BettingManager(BettingPlus plugin) {
        this.plugin = plugin;
    }

    public void requestBet(Player requester, Player target) {
        if (requester == target) {
            requester.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou can't offer yourself a bet&7.")));
            return;
        }

        Component acceptMessage = sendClickableCommand("&7[&aAccept&7] ", "bet accept " + requester.getName());
        Component denyMessage = sendClickableCommand(" &7[&cDeny&7]", "bet deny " + requester.getName());

        addRequest(requester);
        requester.sendMessage(Message.fixColor("&7Successfully sent a bet offer to &c" + target.getName() + "&7."));
        target.sendMessage(Message.fixColor("&c" + requester.getName() + " &7offered a bet of &a" + plugin.getBet().getRequestedCash() + "$&7. ").append(acceptMessage).append(denyMessage));
    }

    private Component sendClickableCommand(String message, String command) {
        TextComponent component = LegacyComponentSerializer.legacy('&').deserialize(message);
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        return component;
    }

    public void addCoins(Player target, int amount) {
        coins.put(target.getUniqueId(), getCoins(target) + amount);
    }

    public void removeCoins(Player target, int amount) {
        coins.put(target.getUniqueId(), getCoins(target) - amount);
    }

    public int getCoins(Player target) {
        return coins.getOrDefault(target.getUniqueId(), 500);
    }

    public void addRequest(Player requester) {
        requests.put(requester.getUniqueId(), System.currentTimeMillis());
    }

    public void removeRequest(Player requester) {
        requests.remove(requester.getUniqueId());
    }

    public boolean hasRequests(Player target) {
        return requests.containsKey(target.getUniqueId());
    }

    public boolean hasExpired(Player requester) {
        return requests.get(requester.getUniqueId()) >= 20000;
    }

    public void randomWinner(Player requester, Player target) {
        Random randomPlayers = new Random();
        int randomIndex = randomPlayers.nextInt(2);

        if (randomIndex == 0) {
            addCoins(requester, plugin.getBet().getRequestedCash() * 2);
            plugin.getConfig().set("betting-cash." + requester.getName() + ".coins", plugin.getBettingManager().getCoins(requester));
            requester.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have won &a" + plugin.getBet().getRequestedCash() * 2 + "$")));
            target.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou have lost &a" + plugin.getBet().getRequestedCash() + "$")));
            return;
        }

        addCoins(target, plugin.getBet().getRequestedCash() * 2);
        plugin.getConfig().set("betting-cash." + target.getName() + ".coins", plugin.getBettingManager().getCoins(target));
        target.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have won &a" + plugin.getBet().getRequestedCash() + "$")));
        requester.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou have lost &a" + plugin.getBet().getRequestedCash() + "$")));
    }
}