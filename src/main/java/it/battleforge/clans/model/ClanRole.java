package it.battleforge.clans.model;

import java.util.EnumSet;
import java.util.Set;

public final class ClanRole {
    private final String name;
    private final Set<ClanPermission> permissions = EnumSet.noneOf(ClanPermission.class);

    public ClanRole(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public boolean has(ClanPermission perm) { return permissions.contains(perm); }

    public void set(ClanPermission perm, boolean enabled) {
        if (enabled) permissions.add(perm);
        else permissions.remove(perm);
    }
}