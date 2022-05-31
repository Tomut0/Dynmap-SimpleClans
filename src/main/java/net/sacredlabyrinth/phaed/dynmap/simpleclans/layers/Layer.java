package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class Layer {

    protected @Nullable MarkerSet markerSet;
    protected @NotNull final IconStorage iconStorage;
    protected @NotNull final LayerConfig config;
    protected @NotNull MarkerAPI markerAPI;

    public Layer(@NotNull IconStorage iconStorage, @NotNull LayerConfig config, @NotNull MarkerAPI markerAPI) {
        this.iconStorage = iconStorage;
        this.config = config;
        this.markerAPI = markerAPI;

        initMarkerSet();
        initMarkers();
    }

    public Optional<MarkerSet> getMarkerSet() {
        return Optional.ofNullable(markerSet);
    }

    public @NotNull IconStorage getIconStorage() {
        return iconStorage;
    }

    private void initMarkerSet() {
        if (!config.getBoolean(LayerConfig.LayerField.ENABLE)) {
            return;
        }

        markerSet = markerAPI.getMarkerSet(getId());
        markerSet = (markerSet == null) ?
                markerAPI.createMarkerSet(getId(), getLabel(), null, false) : null;

        if (markerSet == null) {
            DynmapSimpleClans.getInstance().getLogger().
                    severe(String.format("Failed to create market set with %s id", getId()));
            return;
        }

        markerSet.setLayerPriority(config.getInt(LayerConfig.LayerField.PRIORITY));
        markerSet.setHideByDefault(config.getBoolean(LayerConfig.LayerField.HIDDEN));
        markerSet.setMinZoom(config.getInt(LayerConfig.LayerField.MINZOOM));
    }

    protected abstract void initMarkers();

    @NotNull
    protected abstract String getId();

    @NotNull
    protected String getLabel() {
        return config.getString(LayerConfig.LayerField.LABEL);
    }
}
