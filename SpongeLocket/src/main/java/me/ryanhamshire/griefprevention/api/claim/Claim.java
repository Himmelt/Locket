package me.ryanhamshire.griefprevention.api.claim;

import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public interface Claim {
    boolean isUserTrusted(UUID uuid, TrustType type);

    boolean isUserTrusted(User user, TrustType type);
}
