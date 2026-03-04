package it.battleforge.clans.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatManager {

    private final Map<UUID, Long> lastCombatMillis = new ConcurrentHashMap<>();

    public void tag(UUID player) {
        lastCombatMillis.put(player, System.currentTimeMillis());
    }

    public boolean isInCombat(UUID player, int requiredSecondsOutOfCombat) {
        return getRemainingSeconds(player, requiredSecondsOutOfCombat) > 0;
    }

    public long getRemainingSeconds(UUID player, int requiredSecondsOutOfCombat) {
        Long last = lastCombatMillis.get(player);
        if (last == null) return 0;

        long elapsed = System.currentTimeMillis() - last;
        long requiredMillis = requiredSecondsOutOfCombat * 1000L;
        long remainingMillis = requiredMillis - elapsed;
        if (remainingMillis <= 0) return 0;

        return (remainingMillis + 999) / 1000;
    }

    public void clear(UUID player) {
        lastCombatMillis.remove(player);
    }
}
