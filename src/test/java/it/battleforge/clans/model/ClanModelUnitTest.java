package it.battleforge.clans.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ClanModelUnitTest {

    @Test
    void normalizeTrimsAndLowercases() {
        assertEquals("myclan", Clan.normalize("  MyClan  "));
    }

    @Test
    void newClanContainsLeaderAndDefaultRole() {
        UUID leader = UUID.randomUUID();
        Clan clan = new Clan("Alpha", leader);

        assertTrue(clan.isMember(leader));
        assertTrue(clan.getRoles().containsKey("membro"));
        assertEquals("membro", clan.getMemberRole().get(leader));
    }

    @Test
    void rolePermissionCanBeEnabledAndDisabled() {
        ClanRole role = new ClanRole("ufficiale");

        assertFalse(role.has(ClanPermission.KICK));
        role.set(ClanPermission.KICK, true);
        assertTrue(role.has(ClanPermission.KICK));
        role.set(ClanPermission.KICK, false);
        assertFalse(role.has(ClanPermission.KICK));
    }
}