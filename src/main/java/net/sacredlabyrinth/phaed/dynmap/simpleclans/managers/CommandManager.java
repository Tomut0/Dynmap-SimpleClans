package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;

public final class CommandManager implements CommandExecutor {

    private final @NotNull DynmapSimpleClans plugin;

    public CommandManager(@NotNull DynmapSimpleClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equals("clanmap")) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                help(sender);
                return true;
            }

            String cmd = args[0];
            if (cmd.equalsIgnoreCase("reload")) {
                return reload(sender);
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (cmd.equalsIgnoreCase("seticon") && args.length == 2) {
                    return setIcon(player, args[1]);
                }
                if (cmd.equalsIgnoreCase("listicons")) {
                    return listIcons(player);
                }
            }
        }
        return false;
    }

    /**
     * Shows help command to the sender
     */
    private void help(@NotNull CommandSender sender) {
        plugin.getConfig().getStringList("help-command").forEach(s ->
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
    }

    /**
     * Retrieves the list of available icons
     */
    private boolean listIcons(@NotNull Player player) {
        if (!player.hasPermission("simpleclans.map.list")) {
            player.sendMessage(lang("no-permission"));
            return true;
        }

        player.sendMessage(lang("available-icons"));

        Set<MarkerIcon> icons = plugin.getHomeLayer().getIconStorage().getIcons();
        for (MarkerIcon icon : icons) {
            String message = lang("icon-line").replace("{iconName}", icon.getMarkerIconLabel());
            player.sendMessage(message);
        }

        if (icons.isEmpty()) {
            player.sendMessage(lang("error-no-icons"));
        }

        return true;
    }

    /**
     * Reloads the plugin
     */
    private boolean reload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("simpleclans.map.reload")) {
            sender.sendMessage(lang("no-permission"));
            return true;
        }

        sender.sendMessage(lang("reloading"));
        plugin.cleanup();
        Preferences.loadPreferences();
        plugin.reload();

        return true;
    }

    private boolean setIcon(@NotNull Player player, @NotNull String icon) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
        if (cp == null || cp.getClan() == null) {
            player.sendMessage(lang("not-member"));
            return true;
        }
        if (!player.hasPermission("simpleclans.map.seticon") || !cp.isLeader()) {
            player.sendMessage(lang("no-permission"));
            return true;
        }

        if (plugin.getHomeLayer().getIconStorage().has(icon.toLowerCase())) {
            if (!player.hasPermission("simpleclans.map.icon.bypass") &&
                    !player.hasPermission("simpleclans.map.icon." + icon)) {
                player.sendMessage(lang("no-permission"));
                return true;
            }
            Preferences pm = new Preferences(cp.getClan());
            pm.setClanHomeIcon(icon);
            player.sendMessage(lang("icon-changed"));
        } else {
            player.sendMessage(lang("icon-not-found"));
        }

        return true;
    }
}