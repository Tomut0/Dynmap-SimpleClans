package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public abstract class Layer {

    protected DynmapSimpleClans plugin = DynmapSimpleClans.getInstance();
    protected MarkerSet markerSet;
    protected @NotNull MarkerAPI markerApi;
    protected static @Nullable MarkerIcon defaultIcon;
    private final @NotNull ConfigurationSection section;

    private final HashMap<String, MarkerIcon> cachedIcons = new HashMap<>(); // No usages yet (I didn't think up a realization)
    private static final File IMAGES_FOLDER = new File(DynmapSimpleClans.getInstance().getDataFolder(), "/images");

    public Layer(@NotNull String section, @NotNull MarkerAPI markerApi) {
        this.markerApi = markerApi;
        this.section = Objects.requireNonNull(plugin.getConfig().getConfigurationSection(section));

        initMarkerSet(markerApi);
        initMarkers();
    }

    private void initMarkerSet(@NotNull MarkerAPI markerApi) {
        if (!getBoolean(LayerSettings.ENABLE)) {
            return;
        }
        markerSet = markerApi.getMarkerSet(getId());
        markerSet = (markerSet == null) ?
                markerApi.createMarkerSet(getId(), getLabel(), null, false) : null;

        if (markerSet == null) {
            DynmapSimpleClans.getInstance().getLogger().
                    severe(String.format("Failed to create market set with %s id", getId()));
            return;
        }

        markerSet.setLayerPriority(getInt(LayerSettings.PRIORITY));
        markerSet.setHideByDefault(getBoolean(LayerSettings.HIDDEN));
        markerSet.setMinZoom(getInt(LayerSettings.MINZOOM));
    }

    public @NotNull ConfigurationSection getSection() {
        return section;
    }

    protected abstract void initMarkers();

    @NotNull
    protected abstract String getId();

    @NotNull
    protected String getLabel() {
        return getString(LayerSettings.LABEL);
    }

    private MarkerIcon getOrCreateDefaultIcon(@NotNull String path, @NotNull String iconName) {
        return defaultIcon == null ?
                markerApi.createMarkerIcon(getId() + "." + iconName, iconName,
                        DynmapSimpleClans.class.getResourceAsStream(path)) : defaultIcon;
    }

    /**
     * @return {@link MarkerIcon} in /images folder
     */
    public MarkerIcon getIcon(@NotNull String iconName) {
        return getIcon(IMAGES_FOLDER, iconName);
    }

    /**
     * @return {@link MarkerIcon} from the folder or the default one
     */
    public MarkerIcon getIcon(@NotNull File iconFolder, @NotNull String iconName) {
        Optional<MarkerIcon> iconOptional = getIconsIn(iconFolder).stream().
                filter(markerIcon -> Objects.equals(markerIcon.getMarkerIconLabel(), iconName)).
                findAny();

        return iconOptional.orElseGet(() -> getOrCreateDefaultIcon(iconFolder.toString(), iconName));
    }

    /**
     * Creates and retrieves all icons in defined folder
     */
    public List<MarkerIcon> getIconsIn(@NotNull File iconsFolder) {
        File[] files = iconsFolder.listFiles(pathname -> {
            String mimeType = new MimetypesFileTypeMap().getContentType(pathname);
            return mimeType.split("/")[0].equals("image");
        });

        List<MarkerIcon> icons = new ArrayList<>();
        for (File icon : Objects.requireNonNull(files)) {
            String name = icon.getName();
            String nameWithoutExt = name.substring(0, name.lastIndexOf("."));
            try (FileInputStream stream = new FileInputStream(icon)) {
                MarkerIcon markerIcon = markerApi.createMarkerIcon(getId() + "." + nameWithoutExt, nameWithoutExt, stream);
                if (markerIcon != null) {
                    icons.add(markerIcon);
                }
            } catch (IOException ex) {
                plugin.getLogger().severe(
                        String.format("Error occurred while trying to create %s icon: %s", name, ex.getMessage()));
            }
        }

        return icons;
    }

    @NotNull
    public Integer getInt(LayerSettings setting) {
        return section.getInt(setting.path, NumberConversions.toInt(setting.def));
    }

    @NotNull
    public String getString(LayerSettings setting) {
        return section.getString(setting.path, String.valueOf(setting.def));
    }

    @NotNull
    public Boolean getBoolean(LayerSettings setting) {
        return section.getBoolean(setting.path, (Boolean) setting.def);
    }

    public enum LayerSettings {

        ENABLE("enable", true),
        PRIORITY("layer-priority", 1),
        LABEL("label", "Label"),
        HIDDEN("hide-by-default", false),
        MINZOOM("min-zoom", 0);

        private final String path;
        private final Object def;

        LayerSettings(String path, Object def) {
            this.path = path;
            this.def = def;
        }
    }
}
