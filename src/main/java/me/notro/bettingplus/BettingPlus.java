package me.notro.bettingplus;

import lombok.Getter;
import me.notro.bettingplus.commands.BetCommand;
import me.notro.bettingplus.commands.CashCommand;
import me.notro.bettingplus.managers.BettingManager;
import me.notro.bettingplus.objects.Bet;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BettingPlus extends JavaPlugin {

    @Getter
    private static BettingPlus instance;
    private BettingManager bettingManager;
    private Bet bet;

    @Override
    public void onEnable() {
        instance = this;

        loadCommands();
        loadListeners();
        loadManagers();
        loadObjects();
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getLogger().info("has been enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("has been disabled");
    }

    private void loadCommands() {
        getCommand("bet").setExecutor(new BetCommand(this));
        getCommand("cash").setExecutor(new CashCommand(this));
    }

    private void loadListeners() {

    }

    private void loadManagers() {
        bettingManager = new BettingManager(this);
    }

    private void loadObjects() {
        bet = new Bet();
    }
}
