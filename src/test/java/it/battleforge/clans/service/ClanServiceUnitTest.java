package it.battleforge.clans.service;

import it.battleforge.clans.model.ClanPermission;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class ClanServiceUnitTest {

    @Test
    void createClanValidatesNameAndUniqueness() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.INVALID_NAME, service.createClan(leader, "   "));
        assertEquals(ClanService.CreateResult.NAME_TOO_LONG, service.createClan(leader, "ABCDEFGHIJK"));
        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.CreateResult.ALREADY_IN_CLAN, service.createClan(leader, "Beta"));
        assertEquals(ClanService.CreateResult.NAME_TAKEN, service.createClan(other, " alpha "));
    }

    @Test
    void inviteAcceptAndDeclineFlowWorks() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, target));
        assertTrue(service.getPendingInvite(target).isPresent());
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(target));
        assertTrue(service.isInClan(target));

        UUID third = UUID.randomUUID();
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, third));
        assertTrue(service.declineInvite(third));
        assertEquals(ClanService.AcceptInviteResult.NO_INVITE, service.acceptInvite(third));
    }

    @Test
    void leaderCanManageRolesAndPermissions() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, member));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(member));

        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Officer"));
        assertEquals(ClanService.RolePermResult.OK,
                service.setRolePermission(leader, "Officer", ClanPermission.KICK, true));
        assertEquals(ClanService.AssignRoleResult.OK, service.assignRole(leader, member, "Officer"));
        assertTrue(service.hasPermission(member, ClanPermission.KICK));
    }

    @Test
    void kickRespectsLeaderAndRoleHierarchy() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID officer = UUID.randomUUID();
        UUID soldier = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));

        assertEquals(ClanService.InviteResult.OK, service.invite(leader, officer));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(officer));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, soldier));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(soldier));

        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Officer"));
        assertEquals(ClanService.RolePermResult.OK,
                service.setRolePermission(leader, "Officer", ClanPermission.KICK, true));
        assertEquals(ClanService.AssignRoleResult.OK, service.assignRole(leader, officer, "Officer"));

        assertEquals(ClanService.KickResult.TARGET_IS_LEADER, service.kick(officer, leader));
        assertEquals(ClanService.KickResult.OK, service.kick(officer, soldier));
    }

    @Test
    void dataPersistsAcrossServiceInstances() throws IOException {
        ClanService writer = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, writer.createClan(leader, "Alpha"));
        assertEquals(ClanService.InviteResult.OK, writer.invite(leader, member));
        assertEquals(ClanService.AcceptInviteResult.OK, writer.acceptInvite(member));

        File temp = File.createTempFile("clans-service", ".yml");
        try {
            writer.saveToFile(temp);

            ClanService reader = new ClanService();
            reader.loadFromFile(temp);

            assertTrue(reader.isInClan(leader));
            assertTrue(reader.isInClan(member));
            assertTrue(reader.getClanByPlayer(leader).isPresent());
            assertEquals("Alpha", reader.getClanByPlayer(leader).get().getName());
            assertFalse(reader.getPendingInvite(member).isPresent());
        } finally {
            temp.delete();
        }
    }

    @Test
    void leaderCanPromoteAndDemoteRoleWeightWithBaseRoleLocked() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Officer"));

        assertEquals(ClanService.RoleWeightResult.OK, service.adjustRoleWeight(leader, "Officer", 1));
        assertEquals(ClanService.RoleWeightResult.OK, service.adjustRoleWeight(leader, "Officer", -1));
        assertEquals(ClanService.RoleWeightResult.ROLE_LOCKED, service.adjustRoleWeight(leader, "membro", 1));
    }

    @Test
    void roleWeightCannotExceedConfiguredMaximum() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Officer"));
        assertEquals(ClanService.RoleWeightResult.OK, service.adjustRoleWeight(leader, "Officer", ClanService.MAX_ROLE_WEIGHT - 1));
        assertEquals(ClanService.RoleWeightResult.MAX_WEIGHT_REACHED, service.adjustRoleWeight(leader, "Officer", 1));
    }

    @Test
    void teleportHomeRequiresTpHomePermissionForMembers() {
        ClanService service = new ClanService();
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();

        assertEquals(ClanService.CreateResult.OK, service.createClan(leader, "Alpha"));
        assertEquals(ClanService.InviteResult.OK, service.invite(leader, member));
        assertEquals(ClanService.AcceptInviteResult.OK, service.acceptInvite(member));

        assertEquals(ClanService.HomeTpResult.NO_PERMISSION, service.canTeleportHome(member));

        assertEquals(ClanService.CreateRoleResult.OK, service.createRole(leader, "Raider"));
        assertEquals(ClanService.RolePermResult.OK,
                service.setRolePermission(leader, "Raider", ClanPermission.TP_HOME, true));
        assertEquals(ClanService.AssignRoleResult.OK, service.assignRole(leader, member, "Raider"));

        assertEquals(ClanService.HomeTpResult.HOME_NOT_SET, service.canTeleportHome(member));
    }
}
