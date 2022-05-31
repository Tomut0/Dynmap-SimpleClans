package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig.LayerField.*;

public abstract class Layer {

    protected final @NotNull IconStorage iconStorage;
    protected final @NotNull LayerConfig config;
    protected MarkerSet markerSet;
    protected @NotNull MarkerAPI markerAPI;

    public Layer(@NotNull IconStorage iconStorage, @NotNull LayerConfig config, @NotNull MarkerAPI markerAPI) {
        this.iconStorage = iconStorage;
        this.config = config;
        this.markerAPI = markerAPI;

        initMarkerSet();
        initMarkers();
    }

    public @NotNull IconStorage getIconStorage() {
        return iconStorage;
    }

    private void initMarkerSet() {
        if (!config.getBoolean(ENABLE)) {
            return;
        }

        markerSet = markerAPI.getMarkerSet(getId());
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet(getId(), getLabel(), null, false);
        }

        markerSet.setLayerPriority(config.getInt(PRIORITY));
        markerSet.setHideByDefault(config.getBoolean(HIDDEN));
        markerSet.setMinZoom(config.getInt(MINZOOM));
    }

    protected abstract void initMarkers();

    @NotNull
    protected abstract String getId();

    @NotNull
    protected String getLabel() {
        return config.getString(LABEL);
    }
}
