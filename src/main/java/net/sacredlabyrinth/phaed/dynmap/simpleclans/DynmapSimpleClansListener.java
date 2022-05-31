package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.KillEntry;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.HomeLayer;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

import java.util.Objects;
import java.util.Optional;

public class DynmapSimpleClansListener implements Listener {

    private final DynmapSimpleClans plugin;

    public DynmapSimpleClansListener(DynmapSimpleClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayerManager().addEntry(event.getPlayer());
    }

    @EventHandler
    public void onSetHome(PlayerHomeSetEvent event) {
        HomeLayer homeLayer = plugin.getHomeLayer();
        Optional<MarkerSet> markerSet = homeLayer.getMarkerSet();

        Clan clan = event.getClan();
        String tag = clan.getTag();

        Location loc = event.getLocation();
        String worldName = Objects.requireNonNull(loc.getWorld()).getName();

        if (markerSet.isPresent()) {
            MarkerSet markers = markerSet.get();
            Marker marker = markers.findMarker(tag);

            if (marker != null) {
                marker.setLocation(worldName, loc.getX(), loc.getY(), loc.getZ());
            } else {
                homeLayer.createMarker(clan);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = null;

            // find attacker

            EntityDamageEvent lastDamageCause = victim.getLastDamageCause();

            if (lastDamageCause instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) lastDamageCause;

                if (entityEvent.getDamager() instanceof Player) {
                    attacker = (Player) entityEvent.getDamager();
                } else if (entityEvent.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) entityEvent.getDamager();

                    if (arrow.getShooter() instanceof Player) {
                        attacker = (Player) arrow.getShooter();
                    }
                }
            }

            if (attacker != null) {
                ClanPlayer acp = plugin.getClanManager().getCreateClanPlayer(attacker.getUniqueId());
                ClanPlayer vcp = plugin.getClanManager().getCreateClanPlayer(victim.getUniqueId());

                DynmapSimpleClans.getInstance().getKills().addKillEntry(new KillEntry(vcp, acp, victim.getLocation()));
            }
        }
    }
}