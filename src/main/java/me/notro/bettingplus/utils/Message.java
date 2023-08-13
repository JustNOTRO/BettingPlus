package me.notro.bettingplus.utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Message {

    public static final Component
            NO_SENDER_EXECUTOR = fixColor("&cOnly players can execute this command&7."),
            NO_PERMISSION = fixColor("&cYou don't have permission to execute this command&7."),
            NO_PLAYER_EXISTENCE = fixColor("&cPlayer does not exist/online&7.");

    @Getter
    private final static Component prefix = fixColor("&7[&3Betting&bPlus&7] &8>>> ");


    public static Component fixColor(String message) {
        return LegacyComponentSerializer.legacy('&').deserialize(message);
    }
}
