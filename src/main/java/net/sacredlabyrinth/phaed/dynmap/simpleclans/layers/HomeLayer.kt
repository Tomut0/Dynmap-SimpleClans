package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.simpleclans.Clan
import net.sacredlabyrinth.phaed.simpleclans.utils.VanishUtils
import org.dynmap.markers.MarkerAPI

class HomeLayer(
    iconStorage: IconStorage,
    config: LayerConfig,
    markerAPI: MarkerAPI
) : Layer(iconStorage, config, markerAPI) {
    init {
        initMarkers()
    }

    fun upsertMarker(clan: Clan) {
        val tag = clan.tag
        val icon = iconStorage.getIcon(tag)
        val loc = clan.homeLocation
        val world = loc?.world ?: return

        if (isHidden(tag, world.name)) {
            return
        }

        val marker = markerSet.findMarker(tag)
        marker?.setLocation(world.name, loc.x, loc.y, loc.z) ?: markerSet.createMarker(
            tag, formatClanLabel(clan), true, world.name, loc.x, loc.y, loc.z, icon, true
        )
    }

    private fun isHidden(tag: String, worldName: String): Boolean {
        val hidden = config.section.getStringList("hidden-markers")
        return hidden.contains(tag) || hidden.contains("world:$worldName")
    }

    private fun formatClanLabel(clan: Clan): String? {
        val inactive = "${clan.inactiveDays}/${clan.maxInactiveDays}"

        var onlineMembers =
            clan.onlineMembers.map { cp -> cp.toPlayer() }.count { player -> VanishUtils.isVanished(player) }.toString()

        onlineMembers = "${onlineMembers}/${clan.size}"

        val status = if (clan.isVerified) lang("verified") else lang("unverified")
        val feeEnabled = if (clan.isMemberFeeEnabled) lang("fee-enabled") else lang("fee-disabled")

        val label = config.section.getString("format", "{clan} &8(home)")!!
            .replace("{clan}", clan.name)
            .replace("{tag}", clan.tag)
            .replace("{member_count}", clan.members.size.toString())
            .replace("{inactive}", inactive)
            .replace("{founded}", clan.foundedString)
            .replace("{rival}", clan.totalRival.toString())
            .replace("{neutral}", clan.totalNeutral.toString())
            .replace("{deaths}", clan.totalDeaths.toString())
            .replace("{kdr}", clan.totalKDR.toString())
            .replace("{civilian}", clan.totalCivilian.toString())
            .replace("{members_online}", onlineMembers)
            .replace("{leaders}", clan.getLeadersString("", ", "))
            .replace("{allies}", clan.getAllyString(", ", null))
            .replace("{rivals}", clan.getRivalString(", ", null))
            .replace("{fee_value}", clan.memberFee.toString())
            .replace("{status}", status)
            .replace("{fee_enabled}", feeEnabled)

        return Helper.colorToHTML(label)
    }

    private fun initMarkers() {
        for (clan in getClansWithHome()) {
            upsertMarker(clan)
        }
    }

    private fun getClansWithHome(): List<Clan> {
        return DynmapSimpleClans.getInstance().clanManager.clans
            .filter { clan -> clan.homeLocation != null }
            .filter { clan -> clan.homeLocation!!.world != null }
            .toList()
    }

    override fun getId() = "simpleclans.clan.homes"
}