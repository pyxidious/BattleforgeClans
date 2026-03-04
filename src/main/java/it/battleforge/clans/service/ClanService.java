package it.battleforge.clans.service;

import it.battleforge.clans.model.Clan;
import it.battleforge.clans.model.ClanPermission;
import it.battleforge.clans.model.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ClanService {
    public static final int MAX_ROLE_WEIGHT = 1000;

    private final Set<UUID> clanChatMode = new HashSet<>();
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

    public boolean isClanChatEnabled(UUID player) {
        return clanChatMode.contains(player);
    }

    public boolean toggleClanChat(UUID player) {
        if (clanChatMode.contains(player)) {
            clanChatMode.remove(player);
            return false;
        }

        clanChatMode.add(player);
        return true;
    }

    public String getRoleDisplay(UUID player) {
        var clanOpt = getClanByPlayer(player);
        if (clanOpt.isEmpty()) return "";

        var clan = clanOpt.get();
        if (clan.getLeader().equals(player)) return "capo";

        String roleKey = clan.getMemberRole().getOrDefault(player, "membro");
        var role = clan.getRoles().get(roleKey);
        return role != null ? role.getName() : "membro";
    }

    public KickResult kick(UUID actor, UUID target) {
        Optional<Clan> clanOpt = getClanByPlayer(actor);
        if (clanOpt.isEmpty()) return KickResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();

        if (actor.equals(target)) return KickResult.CANNOT_KICK_SELF;
        if (!clan.isMember(target)) return KickResult.TARGET_NOT_IN_YOUR_CLAN;
        if (clan.getLeader().equals(target)) return KickResult.TARGET_IS_LEADER;

        boolean actorIsLeader = clan.getLeader().equals(actor);
        boolean canKick = actorIsLeader || hasPermission(actor, ClanPermission.KICK);
        if (!canKick) return KickResult.NOT_LEADER;

        if (!actorIsLeader) {
            int actorWeight = getRoleWeight(clan, actor);
            int targetWeight = getRoleWeight(clan, target);
            if (actorWeight <= targetWeight) {
                return KickResult.INSUFFICIENT_ROLE_HIERARCHY;
            }
        }

        clan.removeMember(target);
        clan.getMemberRole().remove(target);
        clanOf.remove(target);
        clanChatMode.remove(target);
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

        for (UUID member : new ArrayList<>(clan.getMembers())) {
            clanOf.remove(member);
            clanChatMode.remove(member);
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
        clan.getMemberRole().remove(player);
        clanOf.remove(player);
        clanChatMode.remove(player);
        return LeaveResult.OK;
    }

    public boolean hasPermission(UUID player, ClanPermission perm) {
        Optional<Clan> clanOpt = getClanByPlayer(player);
        if (clanOpt.isEmpty()) return false;

        Clan clan = clanOpt.get();
        if (clan.getLeader().equals(player)) return true;

        String roleKey = clan.getMemberRole().get(player);
        if (roleKey == null) return false;

        ClanRole role = clan.getRoles().get(roleKey);
        return role != null && role.has(perm);
    }

    public enum SetHomeResult { OK, NOT_IN_CLAN, NO_PERMISSION }

    public SetHomeResult setHome(UUID actor, Location loc) {
        var clanOpt = getClanByPlayer(actor);
        if (clanOpt.isEmpty()) return SetHomeResult.NOT_IN_CLAN;
        if (!hasPermission(actor, ClanPermission.SET_HOME)) return SetHomeResult.NO_PERMISSION;

        clanOpt.get().setHome(loc);
        return SetHomeResult.OK;
    }

    public enum HomeTpResult { OK, NOT_IN_CLAN, NO_PERMISSION, HOME_NOT_SET }

    public HomeTpResult canTeleportHome(UUID actor) {
        var clanOpt = getClanByPlayer(actor);
        if (clanOpt.isEmpty()) return HomeTpResult.NOT_IN_CLAN;
        if (!hasPermission(actor, ClanPermission.TP_HOME)) return HomeTpResult.NO_PERMISSION;
        return clanOpt.get().getHome() == null ? HomeTpResult.HOME_NOT_SET : HomeTpResult.OK;
    }

    public Location getHomeLocation(UUID actor) {
        return getClanByPlayer(actor).map(Clan::getHome).orElse(null);
    }

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

        int nextWeight = clan.getRoles().values().stream()
                .mapToInt(ClanRole::getWeight)
                .max()
                .orElse(0) + 1;

        clan.getRoles().put(key, new ClanRole(roleName, nextWeight));
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

    public enum RoleWeightResult {
        OK,
        NOT_IN_CLAN,
        NOT_LEADER,
        ROLE_NOT_FOUND,
        ROLE_LOCKED,
        MIN_WEIGHT_REACHED,
        MAX_WEIGHT_REACHED
    }

    public RoleWeightResult adjustRoleWeight(UUID leader, String roleName, int delta) {
        var clanOpt = getClanByPlayer(leader);
        if (clanOpt.isEmpty()) return RoleWeightResult.NOT_IN_CLAN;

        Clan clan = clanOpt.get();
        if (!clan.getLeader().equals(leader)) return RoleWeightResult.NOT_LEADER;

        String key = roleName.toLowerCase();
        ClanRole role = clan.getRoles().get(key);
        if (role == null) return RoleWeightResult.ROLE_NOT_FOUND;
        if ("membro".equals(key)) return RoleWeightResult.ROLE_LOCKED;

        int newWeight = role.getWeight() + delta;
        if (newWeight < 1) return RoleWeightResult.MIN_WEIGHT_REACHED;
        if (newWeight > MAX_ROLE_WEIGHT) return RoleWeightResult.MAX_WEIGHT_REACHED;

        role.setWeight(newWeight);
        return RoleWeightResult.OK;
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

    public void loadFromFile(File file) {
        clans.clear();
        clanOf.clear();
        pendingInvite.clear();
        clanChatMode.clear();

        if (!file.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection clansSection = yaml.getConfigurationSection("clans");
        if (clansSection != null) {
            for (String clanKey : clansSection.getKeys(false)) {
                ConfigurationSection clanSection = clansSection.getConfigurationSection(clanKey);
                if (clanSection == null) continue;

                UUID leader = parseUuid(clanSection.getString("leader"));
                if (leader == null) continue;

                String name = clanSection.getString("name", clanKey);
                Clan clan = new Clan(name, leader);

                clan.getRoles().clear();
                clan.getMemberRole().clear();

                ConfigurationSection rolesSection = clanSection.getConfigurationSection("roles");
                if (rolesSection != null) {
                    for (String roleKey : rolesSection.getKeys(false)) {
                        ConfigurationSection roleSection = rolesSection.getConfigurationSection(roleKey);
                        if (roleSection == null) continue;

                        String roleName = roleSection.getString("name", roleKey);
                        int weight = roleSection.getInt("weight", 0);
                        ClanRole role = new ClanRole(roleName, weight);

                        for (String permName : roleSection.getStringList("permissions")) {
                            try {
                                role.set(ClanPermission.valueOf(permName), true);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }

                        clan.getRoles().put(roleKey.toLowerCase(), role);
                    }
                }

                if (!clan.getRoles().containsKey("membro")) {
                    clan.getRoles().put("membro", new ClanRole("membro", 0));
                }

                List<String> members = clanSection.getStringList("members");
                if (members.isEmpty()) {
                    clan.addMember(leader);
                } else {
                    for (String raw : members) {
                        UUID member = parseUuid(raw);
                        if (member != null) clan.addMember(member);
                    }
                    clan.addMember(leader);
                }

                ConfigurationSection memberRoleSection = clanSection.getConfigurationSection("member-role");
                if (memberRoleSection != null) {
                    for (String rawUuid : memberRoleSection.getKeys(false)) {
                        UUID member = parseUuid(rawUuid);
                        if (member == null || !clan.isMember(member)) continue;

                        String roleKey = memberRoleSection.getString(rawUuid, "membro").toLowerCase();
                        if (!clan.getRoles().containsKey(roleKey)) roleKey = "membro";
                        clan.getMemberRole().put(member, roleKey);
                    }
                }

                for (UUID member : clan.getMembers()) {
                    clan.getMemberRole().putIfAbsent(member, "membro");
                }

                ConfigurationSection homeSection = clanSection.getConfigurationSection("home");
                if (homeSection != null) {
                    Location home = deserializeLocation(homeSection);
                    clan.setHome(home);
                }

                clans.put(clan.getKey(), clan);
                for (UUID member : clan.getMembers()) {
                    clanOf.put(member, clan.getKey());
                }
            }
        }

        ConfigurationSection inviteSection = yaml.getConfigurationSection("pending-invites");
        if (inviteSection != null) {
            for (String rawUuid : inviteSection.getKeys(false)) {
                UUID invited = parseUuid(rawUuid);
                if (invited == null || isInClan(invited)) continue;

                String clanKey = inviteSection.getString(rawUuid, "").toLowerCase();
                if (clans.containsKey(clanKey)) {
                    pendingInvite.put(invited, clanKey);
                }
            }
        }

        for (String rawUuid : yaml.getStringList("chat-mode")) {
            UUID player = parseUuid(rawUuid);
            if (player != null && isInClan(player)) {
                clanChatMode.add(player);
            }
        }
    }

    public void saveToFile(File file) {
        YamlConfiguration yaml = new YamlConfiguration();

        ConfigurationSection clansSection = yaml.createSection("clans");
        for (Clan clan : clans.values()) {
            ConfigurationSection clanSection = clansSection.createSection(clan.getKey());
            clanSection.set("name", clan.getName());
            clanSection.set("leader", clan.getLeader().toString());

            List<String> members = clan.getMembers().stream().map(UUID::toString).toList();
            clanSection.set("members", members);

            ConfigurationSection rolesSection = clanSection.createSection("roles");
            for (Map.Entry<String, ClanRole> entry : clan.getRoles().entrySet()) {
                String roleKey = entry.getKey();
                ClanRole role = entry.getValue();

                ConfigurationSection roleSection = rolesSection.createSection(roleKey);
                roleSection.set("name", role.getName());
                roleSection.set("weight", role.getWeight());

                List<String> perms = new ArrayList<>();
                for (ClanPermission permission : ClanPermission.values()) {
                    if (role.has(permission)) {
                        perms.add(permission.name());
                    }
                }
                roleSection.set("permissions", perms);
            }

            ConfigurationSection memberRoleSection = clanSection.createSection("member-role");
            for (Map.Entry<UUID, String> entry : clan.getMemberRole().entrySet()) {
                memberRoleSection.set(entry.getKey().toString(), entry.getValue());
            }

            Location home = clan.getHome();
            if (home != null) {
                ConfigurationSection homeSection = clanSection.createSection("home");
                serializeLocation(home, homeSection);
            }
        }

        ConfigurationSection inviteSection = yaml.createSection("pending-invites");
        for (Map.Entry<UUID, String> entry : pendingInvite.entrySet()) {
            inviteSection.set(entry.getKey().toString(), entry.getValue());
        }

        List<String> chatMode = clanChatMode.stream().map(UUID::toString).toList();
        yaml.set("chat-mode", chatMode);

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile salvare i dati clan su file", e);
        }
    }

    private int getRoleWeight(Clan clan, UUID player) {
        if (clan.getLeader().equals(player)) return Integer.MAX_VALUE;

        String roleKey = clan.getMemberRole().getOrDefault(player, "membro");
        ClanRole role = clan.getRoles().get(roleKey);
        return role != null ? role.getWeight() : 0;
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static void serializeLocation(Location location, ConfigurationSection section) {
        World world = location.getWorld();
        if (world == null) return;

        section.set("world", world.getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private static Location deserializeLocation(ConfigurationSection section) {
        String worldName = section.getString("world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    // --- Result enums ---
    public enum CreateResult { OK, ALREADY_IN_CLAN, NAME_TAKEN, NOT_IN_CLAN, NOT_LEADER, INVALID_NAME, NAME_TOO_LONG, ALREADY_EXISTS }
    public enum InviteResult { OK, NOT_IN_CLAN, NOT_LEADER, TARGET_ALREADY_IN_CLAN, TARGET_ALREADY_INVITED }
    public enum AcceptInviteResult { OK, NO_INVITE, ALREADY_IN_CLAN, CLAN_NO_LONGER_EXISTS }
    public enum DeleteResult { OK, NOT_IN_CLAN, NOT_LEADER }
    public enum KickResult {
        OK,
        NOT_IN_CLAN,
        NOT_LEADER,
        CANNOT_KICK_SELF,
        TARGET_NOT_IN_YOUR_CLAN,
        TARGET_IS_LEADER,
        INSUFFICIENT_ROLE_HIERARCHY
    }
    public enum LeaveResult { OK, NOT_IN_CLAN, LEADER_CANNOT_LEAVE }
}
