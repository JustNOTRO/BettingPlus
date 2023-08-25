package me.notro.bettingplus.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Bet {

    private final UUID requester;
    private final UUID target;
    private double requestedCash;
}
