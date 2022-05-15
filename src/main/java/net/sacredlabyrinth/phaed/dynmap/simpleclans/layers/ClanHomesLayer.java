package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.PreferencesManager;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Location;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;
import static net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager.ConfigField.PURGE_INACTIVE_CLAN_DAYS;
import static net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager.ConfigField.PURGE_UNVERIFIED_CLAN_DAYS;

public class ClanHomesLayer extends Layer {

    public ClanHomesLayer(@NotNull String section, @NotNull MarkerAPI markerApi) {
        super(section, markerApi);
    }

    @Override
    protected void initMarkers() {
        File clanHomesFolder = new File(plugin.getDataFolder(), "/images/clanhome".replaceAll("/", File.separator));
        for (Clan clan : getClansWithHome()) {

            PreferencesManager pm = new PreferencesManager(clan);
            String iconName = pm.getClanHomeIcon();
            MarkerIcon icon = getIcon(clanHomesFolder, iconName);

            String tag = clan.getTag();
            Location loc = clan.getHomeLocation();

            if (markerSet.findMarker(tag) == null) {
                // loc.getWorld() can't be null because of filter in #getClansWithHome()
                //noinspection ConstantConditions
                markerSet.createMarker(tag, formatClanLabel(clan), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                        icon, false);
            }
        }
    }

    private List<Clan> getClansWithHome() {
        return plugin.getClanManager().getClans().stream().
                filter(clan -> clan.getHomeLocation() != null).
                filter(clan -> clan.getHomeLocation().getWorld() != null).
                collect(Collectors.toList());
    }

    private String formatClanLabel(Clan clan) {
        String inactive = clan.getInactiveDays() + "/" + (clan.isVerified() ?
                plugin.getSettingsManager().getInt(PURGE_INACTIVE_CLAN_DAYS) :
                plugin.getSettingsManager().getInt(PURGE_UNVERIFIED_CLAN_DAYS));

        String membersOnline = net.sacredlabyrinth.phaed.simpleclans.Helper.
                stripOffLinePlayers(clan.getMembers()).size() + "/" + clan.getSize();

        String status = (clan.isVerified() ? lang("verified") : lang("unverified"));
        String feeEnabled = (clan.isMemberFeeEnabled() ? lang("fee-enabled") : lang("fee-disabled"));

        String label = getSection().getString("format", "{clan} &8(home)")
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
                .replace("{members_online}", membersOnline)
                .replace("{leaders}", clan.getLeadersString("", ", "))
                .replace("{allies}", clan.getAllyString(", ", null))
                .replace("{rivals}", clan.getRivalString(", ", null))
                .replace("{fee_value}", String.valueOf(clan.getMemberFee()))
                .replace("{status}", status)
                .replace("{fee_enabled}", feeEnabled);

        return Helper.colorToHTML(label);
    }

    @Override
    protected @NotNull String getId() {
        return "simpleclans.clan.homes";
    }
}
