package com.pyzatech.pyzanode.gameselector.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Small value object representing one configurable menu entry.
 *
 * All of the data here comes from config.yml so server owners can move buttons
 * around or change groups / presets without recompiling.
 */
public final class MenuEntry {

    private final String key;
    private final int slot;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final String group;
    private final String presetId;
    private final String permission;
    private final Sound clickSound;

    private MenuEntry(String key,
                      int slot,
                      Material material,
                      String displayName,
                      List<String> lore,
                      String group,
                      String presetId,
                      String permission,
                      Sound clickSound) {
        this.key = key;
        this.slot = slot;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.group = group;
        this.presetId = presetId;
        this.permission = permission;
        this.clickSound = clickSound;
    }

    /**
     * Build an entry from the {@code menu.entries.<key>} section.
     */
    public static MenuEntry fromConfig(String key, ConfigurationSection section) {
        if (section == null) throw new IllegalArgumentException("section is null");

        int slot = section.getInt("slot", -1);
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0 for " + key);
        }

        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.STONE;

        String name = section.getString("name", "&f" + key);
        List<String> lore = section.getStringList("lore");
        String group = section.getString("group", key);
        String presetId = section.getString("preset-id", "");
        String permission = section.getString("permission", "");

        String soundName = section.getString("open-sound", "UI_BUTTON_CLICK");
        Sound clickSound = null;
        try {
            clickSound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // Fallback: no sound if it doesn't exist on this MC version.
        }

        return new MenuEntry(key, slot, material, name, lore, group, presetId, permission, clickSound);
    }

    public String getKey() {
        return key;
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getGroup() {
        return group;
    }

    /**
     * @return preset ID for this entry, or null to use the global default-preset-id.
     */
    public String getPresetIdOrNull() {
        return (presetId != null && !presetId.isEmpty()) ? presetId : null;
    }

    public String getPermission() {
        return permission;
    }

    public Sound getClickSound() {
        return clickSound;
    }
}


