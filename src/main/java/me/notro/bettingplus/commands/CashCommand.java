package me.notro.bettingplus.commands;

import me.notro.bettingplus.BettingPlus;
import me.notro.bettingplus.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CashCommand implements CommandExecutor {

    private final BettingPlus plugin;

    public CashCommand(BettingPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Message.getPrefix().append(Message.NO_SENDER_EXECUTOR));
            return false;
        }

        if (!player.hasPermission("bettingplus.cash")) {
            player.sendMessage(Message.getPrefix().append(Message.NO_PERMISSION));
            return false;
        }

        if (args.length > 0) {
            player.sendMessage(Message.getPrefix().append(Message.fixColor("&7/" + label)));
            return false;
        }

        player.sendMessage(Message.getPrefix().append(Message.fixColor("&7Current cash: &a" + plugin.getBettingManager().getCoins(player) + "$")));
        return true;
    }
}
