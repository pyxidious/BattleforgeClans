package it.battleforge.clans.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Clan {
    private final String name;          // nome "display"
    private final String key;           // nome normalizzato (lower) per lookup
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>(); // include leader

    public Clan(String name, UUID leader) {
        this.name = name;
        this.key = normalize(name);
        this.leader = leader;
        this.members.add(leader);
    }

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