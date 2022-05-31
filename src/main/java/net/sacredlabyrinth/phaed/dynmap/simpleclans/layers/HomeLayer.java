package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.Preferences;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.utils.VanishUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;

public class HomeLayer extends Layer {

    public HomeLayer(@NotNull IconStorage iconStorage, @NotNull LayerConfig config, @NotNull MarkerAPI markerAPI) {
        super(iconStorage, config, markerAPI);
    }

    public void createMarker(Clan clan) {
        if (markerSet == null) {
            return;
        }

        Preferences preferences = new Preferences(clan);
        String iconName = preferences.getClanHomeIcon();
        MarkerIcon icon = iconStorage.getIcon(iconName);

        String tag = clan.getTag();
        Location loc = Objects.requireNonNull(clan.getHomeLocation());
        World world = Objects.requireNonNull(loc.getWorld());

        if (isHidden(tag, world.getName())) {
            return;
        }

        markerSet.createMarker(tag, formatClanLabel(clan),
                true, world.getName(), loc.getX(), loc.getY(), loc.getZ(),
                icon, false);
    }

    @Override
    protected void initMarkers() {
        for (Clan clan : getClansWithHome()) {
            createMarker(clan);
        }
    }

    @Override
    protected @NotNull String getId() {
        return "simpleclans.clan.homes";
    }

    private List<Clan> getClansWithHome() {
        return DynmapSimpleClans.getInstance().getClanManager().getClans().stream().
                filter(clan -> clan.getHomeLocation() != null).
                filter(clan -> clan.getHomeLocation().getWorld() != null).
                collect(Collectors.toList());
    }

    private boolean isHidden(String tag, String worldName) {
        List<String> hidden = config.getSection().getStringList("hidden-markers");
        return hidden.contains(tag) || hidden.contains("world:" + worldName);
    }

    private String formatClanLabel(Clan clan) {
        String inactive = clan.getInactiveDays() + "/" + clan.getMaxInactiveDays();

        String onlineMembers = String.valueOf(clan.getOnlineMembers().stream().
                map(ClanPlayer::toPlayer).
                filter(VanishUtils::isVanished).
                count());

        onlineMembers = onlineMembers + "/" + clan.getSize();

        String status = (clan.isVerified() ? lang("verified") : lang("unverified"));
        String feeEnabled = (clan.isMemberFeeEnabled() ? lang("fee-enabled") : lang("fee-disabled"));

        String label = config.getSection().getString("format", "{clan} &8(home)")
                .replace("{clan}", clan.getName())
                .replace("{tag}", clan.getTag())
                .replace("{member_count}", String.valueOf(clan.getMembers().size()))
                .replace("{inactive}", inactive)
                .replace("{founded}", clan.getFoundedString())
                .replace("{rival}", String.valueOf(clan.getTotalRival()))
                .replace("{neutral}", String.valueOf(clan.getTotalNeutral()))
                .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                .replace("{kdr}", String.valueOf(clan.getTotalKDR()))
                .replace("{civilian}", String.valueOf(clan.getTotalCivilian()))
                .replace("{members_online}", onlineMembers)
                .replace("{leaders}", clan.getLeadersString("", ", "))
                .replace("{allies}", clan.getAllyString(", ", null))
                .replace("{rivals}", clan.getRivalString(", ", null))
                .replace("{fee_value}", String.valueOf(clan.getMemberFee()))
                .replace("{status}", status)
                .replace("{fee_enabled}", feeEnabled);

        return Helper.colorToHTML(label);
    }
}
