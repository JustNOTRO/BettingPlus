package me.notro.bettingplus.commands;

import me.notro.bettingplus.BettingPlus;
import me.notro.bettingplus.models.Bet;
import me.notro.bettingplus.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BetCommand implements CommandExecutor {

    private final BettingPlus plugin;
    private Bet bet;

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

        if (args.length == 0) {
            player.sendMessage(Message.getPrefix().append(Message.fixColor("&cStates: /bet &7<offer/accept/deny> <player>")));
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

                if (!isNumeric(args[2])) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cAmount must be numeric&7.")));
                    return false;
                }

                bet = new Bet(player.getUniqueId(), target.getUniqueId(), Double.parseDouble(args[2]));

                if (plugin.getBettingManager().getCoins(player) < bet.getRequestedCash()) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have enough cash to perform that bet&7.")));
                    return false;
                }

                if (bet.getRequestedCash() <= 0) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou can only bet at the minimum cash of 1.0$&7.")));
                    return false;
                }

                plugin.getBettingManager().startExpiryCount(player, target);
                plugin.getBettingManager().requestBet(player, target ,bet);
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

                if (!plugin.getBettingManager().hasRequests(player)) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have any requests to accept&7.")));
                    return false;
                }

                if (plugin.getBettingManager().getCoins(player) < bet.getRequestedCash()) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have enough cash to perform that bet&7.")));
                    return false;
                }

                plugin.getBettingManager().getExpiryTask().cancel();
                plugin.getBettingManager().removeRequest(player);
                plugin.getBettingManager().addQueue(player, target);
                plugin.getBettingManager().startGame(player, target ,bet);
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

                if (!plugin.getBettingManager().hasRequests(player)) {
                    player.sendMessage(Message.getPrefix().append(Message.fixColor("&cYou don't have any requests to deny&7.")));
                    return false;
                }

                plugin.getBettingManager().removeRequest(player);
                player.sendMessage(Message.getPrefix().append(Message.fixColor("&7You have denied bet request from &c" + target.getName() + "&7.")));
                target.sendMessage(Message.getPrefix().append(Message.fixColor("&c" + target.getName() + " &7denied your bet request.")));
            }

            default -> player.sendMessage(Message.getPrefix().append(Message.fixColor("&cStates: &7<offer/accept/deny>")));
        }
        return true;
    }

    private boolean isNumeric(String number) {
        try {
            Double.parseDouble(number);
        } catch (NumberFormatException exception) {
            return false;
        }

        return true;
    }
}
