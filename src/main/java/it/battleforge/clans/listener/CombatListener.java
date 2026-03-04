package it.battleforge.clans.listener;

import it.battleforge.clans.util.CombatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CombatListener implements Listener {

    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        Player victim = asPlayer(event.getEntity());
        Player attacker = resolveAttacker(event.getDamager());

        if (victim == null || attacker == null) return;

        combatManager.tag(victim.getUniqueId());
        combatManager.tag(attacker.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        combatManager.clear(event.getPlayer().getUniqueId());
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            Object shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private Player asPlayer(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }
}
