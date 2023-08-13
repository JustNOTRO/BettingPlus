package me.notro.bettingplus.commands;

import me.notro.bettingplus.BettingPlus;
import me.notro.bettingplus.utils.Message;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BetCommand implements CommandExecutor {

    private final BettingPlus plugin;

    public BetCommand(BettingPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Message.getPrefix().append(Message.NO_SENDER_EXECUTOR));
            return false;
        }

        if (!player.hasPermission("bettingplus.use")) {
            player.sendMessage(Message.getPrefix().append(Message.NO_PERMISSION));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "offer" -> {
                if (args.length < 3) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&7/" + label + " <offer> <player> <amount>")));
                    return false;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(Message.getPrefix().append(Message.NO_PLAYER_EXISTENCE));
                    return false;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getBet().setRequestedCash(amount);
                } catch (NumberFormatException exception) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cAmount must be numeric&7.")));
                    return false;
                }

                if (plugin.getBettingManager().getCoins(player) < plugin.getBet().getRequestedCash()) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have enough cash to perform that bet&7.")));
                    return false;
                }

                if (plugin.getBet().getRequestedCash() <= 0) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou can only bet at the minimum cash of 1$&7.")));
                    return false;
                }

                plugin.getBettingManager().requestBet(player, target);
            }

            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&7/" + label + " <accept> <player>")));
                    return false;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(Message.getPrefix().append(Message.NO_PLAYER_EXISTENCE));
                    return false;
                }

                if (!plugin.getBettingManager().hasRequests(target)) {
                    target.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have any requests to accept&7.")));
                    return false;
                }

                if (plugin.getBettingManager().getCoins(player) < plugin.getBet().getRequestedCash()) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have enough cash to perform that bet&7.")));
                    return false;
                }

                player.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have accepted a bet offer from &c" + target.getName() + "&7.")));
                plugin.getBettingManager().removeCoins(player, plugin.getBet().getRequestedCash());
                plugin.getBettingManager().removeCoins(target, plugin.getBet().getRequestedCash());

                for (int i = 3; i >= 1; i--) {
                    player.sendTitlePart(TitlePart.TITLE, Message.fixColor("&cStarting in:"));
                    target.sendTitlePart(TitlePart.TITLE, Message.fixColor("&cStarting in:"));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exception) {
                        throw new RuntimeException(exception);
                    }

                    player.sendTitlePart(TitlePart.SUBTITLE, Message.fixColor("&7" + i));
                    target.sendTitlePart(TitlePart.SUBTITLE, Message.fixColor("&7" + i));
                }

                plugin.getBettingManager().removeRequest(player);
                plugin.getBettingManager().randomWinner(player, target);
            }

            case "deny" -> {
                if (args.length < 2) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&7/" + label + " <deny> <player>")));
                    return false;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(Message.getPrefix().append(Message.NO_PLAYER_EXISTENCE));
                    return false;
                }

                plugin.getBettingManager().removeRequest(player);
                player.sendMessage(Message.getPrefix().append(Message.fixColor("&c" + target.getName() + " &7denied your bet request.")));
                target.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have denied bet request from &c" + player.getName() + "&7.")));
            }

            default -> player.sendMessage(Message.getPrefix().append(Message.fixColor("&cStates: &7<offer/accept/deny>")));
        }
        return true;
    }
}
