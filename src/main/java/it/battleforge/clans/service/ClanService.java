package it.battleforge.clans.service;

import it.battleforge.clans.model.Clan;

import java.util.*;

public final class ClanService {

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

    public boolean isInClan(UUID player) {
        return clanOf.containsKey(player);
    }

    public boolean isLeader(UUID player) {
        return getClanByPlayer(player).map(c -> c.getLeader().equals(player)).orElse(false);
    }

    public CreateResult createClan(UUID creator, String clanNameRaw) {
        String clanName = clanNameRaw == null ? "" : clanNameRaw.trim();
        if (clanName.isEmpty()) return CreateResult.INVALID_NAME;

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
        return AcceptInviteResult.OK;
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

    public KickResult kick(UUID leader, UUID target) {
        Optional<Clan> clanOpt = getClanByPlayer(leader);
        if (clanOpt.isEmpty()) return KickResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        if (!clan.getLeader().equals(leader)) return KickResult.NOT_LEADER;

        if (leader.equals(target)) return KickResult.CANNOT_KICK_SELF;
        if (!clan.isMember(target)) return KickResult.TARGET_NOT_IN_YOUR_CLAN;

        clan.removeMember(target);
        clanOf.remove(target);
        return KickResult.OK;
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

    // --- Result enums ---
    public enum CreateResult { OK, INVALID_NAME, NAME_TAKEN, ALREADY_IN_CLAN }
    public enum InviteResult { OK, NOT_IN_CLAN, NOT_LEADER, TARGET_ALREADY_IN_CLAN, TARGET_ALREADY_INVITED }
    public enum AcceptInviteResult { OK, NO_INVITE, ALREADY_IN_CLAN, CLAN_NO_LONGER_EXISTS }
    public enum DeleteResult { OK, NOT_IN_CLAN, NOT_LEADER }
    public enum KickResult { OK, NOT_IN_CLAN, NOT_LEADER, CANNOT_KICK_SELF, TARGET_NOT_IN_YOUR_CLAN }
    public enum LeaveResult { OK, NOT_IN_CLAN, LEADER_CANNOT_LEAVE }
}