package it.battleforge.clans.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import it.battleforge.clans.model.ClanRole;


public final class Clan {
    private final String name;          // nome "display"
    private final String key;           // nome normalizzato (lower) per lookup
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>(); // include leader
    private final Map<String, ClanRole> roles = new HashMap<>();
    private final Map<UUID, String> memberRole = new HashMap<>();
    private Location home;

public Clan(String name, UUID leader) {
    this.name = name;
    this.key = normalize(name);
    this.leader = leader;

    this.members.add(leader);

    // ruolo di default
    this.roles.put("membro", new ClanRole("membro"));
    this.memberRole.put(leader, "membro"); // opzionale, il leader non è influenzato comunque
}

    public Map<String, ClanRole> getRoles() { return roles; }
    public Map<UUID, String> getMemberRole() { return memberRole; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public String getName() { return name; }
    public String getKey() { return key; }
    public UUID getLeader() { return leader; }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase();
    }
}