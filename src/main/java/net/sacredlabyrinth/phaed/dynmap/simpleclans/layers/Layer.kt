package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig.LayerField.*
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

abstract class Layer(
    val iconStorage: IconStorage,
    protected val config: LayerConfig,
    private val markerAPI: MarkerAPI
) {

    lateinit var markerSet: MarkerSet

    init {
        initMarkerSet()
    }

    private fun initMarkerSet() {
        if (!config.getBoolean(ENABLE)) {
            return
        }

        markerSet = markerAPI.getMarkerSet(getId()) ?: markerAPI.createMarkerSet(getId(), getLabel(), null, true)

        markerSet.layerPriority = config.getInt(PRIORITY)
        markerSet.hideByDefault = config.getBoolean(HIDDEN)
        markerSet.minZoom = config.getInt(MINZOOM)
    }

    protected abstract fun getId(): String

    protected open fun getLabel(): String {
        return config.getString(LABEL)
    }
}