package it.battleforge.clans.model;

import java.util.EnumSet;
import java.util.Set;

public final class ClanRole {
    private final String name;
    private final Set<ClanPermission> permissions = EnumSet.noneOf(ClanPermission.class);
    private int weight;

    public ClanRole(String name) {
        this(name, 0);
    }

    public ClanRole(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() { return name; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public boolean has(ClanPermission perm) { return permissions.contains(perm); }

    public void set(ClanPermission perm, boolean enabled) {
        if (enabled) permissions.add(perm);
        else permissions.remove(perm);
    }
}
