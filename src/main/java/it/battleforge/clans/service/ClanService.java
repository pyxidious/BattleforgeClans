package it.battleforge.clans.service;

import it.battleforge.clans.model.Clan;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.model.ClanRole;
import it.battleforge.clans.service.ClanService.CreateResult;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.model.ClanRole;
import org.bukkit.Location;

import java.util.*;

public final class ClanService {

    private final java.util.Set<java.util.UUID> clanChatMode = new java.util.HashSet<>();
    // clanKey -> clan
    private final Map<String, Clan> clans = new HashMap<>();
    // player -> clanKey
    private final Map<UUID, String> clanOf = new HashMap<>();
    // invitedPlayer -> clanKey
    private final Map<UUID, String> pendingInvite = new HashMap<>();


    public Optional<Clan> getClanByPlayer(UUID player) {
        String key = clanOf.get(player);
        if (key == null) return Optional.empty();
        return Optional.ofNullable(clans.get(key));
    }

    public Optional<Clan> getClanByName(String name) {
        return Optional.ofNullable(clans.get(Clan.normalize(name)));
    }


    // Clan Chat mode

    public boolean isClanChatEnabled(java.util.UUID player) {
        return clanChatMode.contains(player);
    }

    public boolean toggleClanChat(java.util.UUID player) {
        if (clanChatMode.contains(player)) {
            clanChatMode.remove(player);
            return false; // OFF
        } else {
            clanChatMode.add(player);
            return true; // ON
        }
    }

    public String getRoleDisplay(UUID player) {
    var clanOpt = getClanByPlayer(player);
    if (clanOpt.isEmpty()) return "";

    var clan = clanOpt.get();
    if (clan.getLeader().equals(player)) return "capo"; // o "leader"

    String roleKey = clan.getMemberRole().getOrDefault(player, "membro");
    var role = clan.getRoles().get(roleKey);
    return role != null ? role.getName() : "membro";
}

    // --- Kick: ora permesso anche a sottoruoli abilitati ---
    public KickResult kick(UUID actor, UUID target) {
        Optional<Clan> clanOpt = getClanByPlayer(actor);
        if (clanOpt.isEmpty()) return KickResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        boolean canKick = clan.getLeader().equals(actor) || hasPermission(actor, ClanPermission.KICK);
        if (!canKick) return KickResult.NOT_LEADER;

        if (actor.equals(target)) return KickResult.CANNOT_KICK_SELF;
        if (!clan.isMember(target)) return KickResult.TARGET_NOT_IN_YOUR_CLAN;

        clan.removeMember(target);
        clanOf.remove(target);
        return KickResult.OK;
    }

    public boolean isInClan(UUID player) {
        return clanOf.containsKey(player);
    }

    public boolean isLeader(UUID player) {
        return getClanByPlayer(player).map(c -> c.getLeader().equals(player)).orElse(false);
    }

    public CreateResult createClan(UUID creator, String clanNameRaw) {
        String clanName = clanNameRaw == null ? "" : clanNameRaw.trim();
        if (clanName.isEmpty()) return CreateResult.INVALID_NAME;
        if (clanName.length() > 10) return CreateResult.NAME_TOO_LONG;

        String key = Clan.normalize(clanName);

        if (isInClan(creator)) return CreateResult.ALREADY_IN_CLAN;
        if (clans.containsKey(key)) return CreateResult.NAME_TAKEN;

        Clan clan = new Clan(clanName, creator);
        clans.put(key, clan);
        clanOf.put(creator, key);

        return CreateResult.OK;
    }

    public InviteResult invite(UUID leader, UUID target) {
        Optional<Clan> clanOpt = getClanByPlayer(leader);
        if (clanOpt.isEmpty()) return InviteResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        if (!clan.getLeader().equals(leader)) return InviteResult.NOT_LEADER;

        if (isInClan(target)) return InviteResult.TARGET_ALREADY_IN_CLAN;
        if (pendingInvite.containsKey(target)) return InviteResult.TARGET_ALREADY_INVITED;

        pendingInvite.put(target, clan.getKey());
        return InviteResult.OK;
    }

    public AcceptInviteResult acceptInvite(UUID player) {
        if (isInClan(player)) return AcceptInviteResult.ALREADY_IN_CLAN;

        String clanKey = pendingInvite.remove(player);
        if (clanKey == null) return AcceptInviteResult.NO_INVITE;

        Clan clan = clans.get(clanKey);
        if (clan == null) return AcceptInviteResult.CLAN_NO_LONGER_EXISTS;

        clan.addMember(player);
        clanOf.put(player, clanKey);
        clan.getMemberRole().put(player, "membro");
        return AcceptInviteResult.OK;
    }

    public Optional<String> getPendingInvite(UUID player) {
        return Optional.ofNullable(pendingInvite.get(player));
    }

    public boolean declineInvite(UUID player) {
        return pendingInvite.remove(player) != null;
    }

    public DeleteResult deleteClan(UUID leader) {
        Optional<Clan> clanOpt = getClanByPlayer(leader);
        if (clanOpt.isEmpty()) return DeleteResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        if (!clan.getLeader().equals(leader)) return DeleteResult.NOT_LEADER;

        // rimuove membership
        for (UUID member : new ArrayList<>(clan.getMembers())) {
            clanOf.remove(member);
            // rimuovi eventuali inviti pendenti verso quel clan
            pendingInvite.entrySet().removeIf(e -> e.getValue().equals(clan.getKey()));
        }

        clans.remove(clan.getKey());
        return DeleteResult.OK;
    }

    public LeaveResult leave(UUID player) {
        Optional<Clan> clanOpt = getClanByPlayer(player);
        if (clanOpt.isEmpty()) return LeaveResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        if (clan.getLeader().equals(player)) return LeaveResult.LEADER_CANNOT_LEAVE;

        clan.removeMember(player);
        clanOf.remove(player);
        return LeaveResult.OK;
    }

    public boolean hasPermission(UUID player, ClanPermission perm) {
    Optional<Clan> clanOpt = getClanByPlayer(player);
    if (clanOpt.isEmpty()) return false;

    Clan clan = clanOpt.get();
    if (clan.getLeader().equals(player)) return true; // leader = tutto

    String roleKey = clan.getMemberRole().get(player);
    if (roleKey == null) return false;

    ClanRole role = clan.getRoles().get(roleKey);
    return role != null && role.has(perm);
}

// --- HOME ---
public enum SetHomeResult { OK, NOT_IN_CLAN, NO_PERMISSION }
public SetHomeResult setHome(UUID actor, Location loc) {
    var clanOpt = getClanByPlayer(actor);
    if (clanOpt.isEmpty()) return SetHomeResult.NOT_IN_CLAN;
    if (!hasPermission(actor, ClanPermission.SET_HOME)) return SetHomeResult.NO_PERMISSION;

    clanOpt.get().setHome(loc);
    return SetHomeResult.OK;
}

public enum HomeTpResult { OK, NOT_IN_CLAN, HOME_NOT_SET }
public HomeTpResult canTeleportHome(UUID actor) {
    var clanOpt = getClanByPlayer(actor);
    if (clanOpt.isEmpty()) return HomeTpResult.NOT_IN_CLAN;
    return clanOpt.get().getHome() == null ? HomeTpResult.HOME_NOT_SET : HomeTpResult.OK;
}

public Location getHomeLocation(UUID actor) {
    return getClanByPlayer(actor).map(Clan::getHome).orElse(null);
}

// --- ROLES ---
public enum CreateRoleResult { OK, NOT_IN_CLAN, NOT_LEADER, INVALID_NAME, NAME_TOO_LONG, ALREADY_EXISTS }
public CreateRoleResult createRole(UUID leader, String roleNameRaw) {
    var clanOpt = getClanByPlayer(leader);
    if (clanOpt.isEmpty()) return CreateRoleResult.NOT_IN_CLAN;

    Clan clan = clanOpt.get();
    if (!clan.getLeader().equals(leader)) return CreateRoleResult.NOT_LEADER;

    String roleName = roleNameRaw == null ? "" : roleNameRaw.trim();
    if (roleName.isEmpty()) return CreateRoleResult.INVALID_NAME;
    if (roleName.length() > 10) return CreateRoleResult.NAME_TOO_LONG;

    String key = roleName.toLowerCase();
    if (clan.getRoles().containsKey(key)) return CreateRoleResult.ALREADY_EXISTS;

    clan.getRoles().put(key, new ClanRole(roleName));
    return CreateRoleResult.OK;
}

public enum RolePermResult { OK, NOT_IN_CLAN, NOT_LEADER, ROLE_NOT_FOUND }
public RolePermResult setRolePermission(UUID leader, String roleName, ClanPermission perm, boolean enabled) {
    var clanOpt = getClanByPlayer(leader);
    if (clanOpt.isEmpty()) return RolePermResult.NOT_IN_CLAN;

    Clan clan = clanOpt.get();
    if (!clan.getLeader().equals(leader)) return RolePermResult.NOT_LEADER;

    ClanRole role = clan.getRoles().get(roleName.toLowerCase());
    if (role == null) return RolePermResult.ROLE_NOT_FOUND;

    role.set(perm, enabled);
    return RolePermResult.OK;
}

public enum AssignRoleResult { OK, NOT_IN_CLAN, NOT_LEADER, ROLE_NOT_FOUND, TARGET_NOT_IN_YOUR_CLAN }
public AssignRoleResult assignRole(UUID leader, UUID target, String roleName) {
    var clanOpt = getClanByPlayer(leader);
    if (clanOpt.isEmpty()) return AssignRoleResult.NOT_IN_CLAN;

    Clan clan = clanOpt.get();
    if (!clan.getLeader().equals(leader)) return AssignRoleResult.NOT_LEADER;

    if (!clan.isMember(target)) return AssignRoleResult.TARGET_NOT_IN_YOUR_CLAN;

    String key = roleName.toLowerCase();
    if (!clan.getRoles().containsKey(key)) return AssignRoleResult.ROLE_NOT_FOUND;

    clan.getMemberRole().put(target, key);
    return AssignRoleResult.OK;
}

    // --- Result enums ---
    public enum CreateResult { OK, ALREADY_IN_CLAN, NAME_TAKEN, NOT_IN_CLAN, NOT_LEADER, INVALID_NAME, NAME_TOO_LONG, ALREADY_EXISTS }
    public enum InviteResult { OK, NOT_IN_CLAN, NOT_LEADER, TARGET_ALREADY_IN_CLAN, TARGET_ALREADY_INVITED }
    public enum AcceptInviteResult { OK, NO_INVITE, ALREADY_IN_CLAN, CLAN_NO_LONGER_EXISTS }
    public enum DeleteResult { OK, NOT_IN_CLAN, NOT_LEADER }
    public enum KickResult { OK, NOT_IN_CLAN, NOT_LEADER, CANNOT_KICK_SELF, TARGET_NOT_IN_YOUR_CLAN }
    public enum LeaveResult { OK, NOT_IN_CLAN, LEADER_CANNOT_LEAVE }
}