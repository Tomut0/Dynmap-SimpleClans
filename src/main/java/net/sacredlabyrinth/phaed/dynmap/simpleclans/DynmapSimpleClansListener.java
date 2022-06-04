package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.simpleclans.Kill;
import net.sacredlabyrinth.phaed.simpleclans.events.AddKillEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;

public class DynmapSimpleClansListener implements Listener {

    private final DynmapSimpleClans plugin;

    public DynmapSimpleClansListener(DynmapSimpleClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSetHome(PlayerHomeSetEvent event) {
        plugin.getHomeLayer().upsertMarker(event.getClan());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onKill(AddKillEvent event) {
        plugin.getKillsLayer().createMarker(new Kill(event.getAttacker(), event.getVictim(), LocalDateTime.now()));
    }
}