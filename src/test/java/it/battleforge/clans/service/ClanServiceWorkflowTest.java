package it.battleforge.clans.service;

import it.battleforge.clans.model.ClanPermission;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("workflow")
class ClanServiceWorkflowTest {

    @Test
    void endToEndMembershipAndModerationWorkflow() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID officer = UUID.randomUUID();
        UUID recruit = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Forge"));

        assertEquals(ClanService.InviteResult.OK, service.invite(leader, officer));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(officer));

        assertEquals(ClanService.InviteResult.OK, service.invite(leader, recruit));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(recruit));

        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Officer"));
        assertEquals(ClanService.RolePermResult.OK,
                service.setRolePermission(leader, "Officer", ClanPermission.KICK, true));
        assertEquals(ClanService.AssignRoleResult.OK, service.assignRole(leader, officer, "Officer"));

        assertTrue(service.hasPermission(officer, ClanPermission.KICK));
        assertEquals(ClanService.KickResult.OK, service.kick(officer, recruit));
        assertFalse(service.isInClan(recruit));
    }

    @Test
    void deletingClanCleansMembershipAndPendingInvites() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID invited = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, member));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(member));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, invited));

        assertEquals(ClanService.DeleteResult.OK, service.deleteClan(leader));
        assertFalse(service.isInClan(leader));
        assertFalse(service.isInClan(member));
        assertEquals(ClanService.AcceptInviteResult.NO_INVITE, service.acceptInvite(invited));
    }
}