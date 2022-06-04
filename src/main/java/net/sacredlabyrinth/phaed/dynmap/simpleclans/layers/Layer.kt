package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig.LayerField.*
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

abstract class Layer(
    private val id: String,
    private val label: String,
    val iconStorage: IconStorage,
    protected val config: LayerConfig,
    markerAPI: MarkerAPI
) {

    protected var markerSet: MarkerSet

    init {
        check(config.getBoolean(ENABLE)) { "[$id] Layer $label is disabled!" }

        markerSet = markerAPI.getMarkerSet(id) ?: markerAPI.createMarkerSet(id, label, null, true)

        markerSet.layerPriority = config.getInt(PRIORITY)
        markerSet.hideByDefault = config.getBoolean(HIDDEN)
        markerSet.minZoom = config.getInt(MINZOOM)
    }
}