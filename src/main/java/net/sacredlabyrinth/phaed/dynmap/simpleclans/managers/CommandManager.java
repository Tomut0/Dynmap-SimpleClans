package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;

public final class CommandManager implements CommandExecutor, TabExecutor {

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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("seticon") || args.length != 0) {
            return Collections.emptyList();
        }

        if (!sender.hasPermission("simpleclans.map.list")) {
            sender.sendMessage(lang("no-permission"));
            return Collections.emptyList();
        }

        List<String> icons = plugin.getHomeLayer().getIconStorage().getIcons().stream().
                map(MarkerIcon::getMarkerIconLabel).
                collect(Collectors.toList());

        if (icons.isEmpty()) {
            sender.sendMessage(lang("error-no-icons"));
            return Collections.emptyList();
        }

        return icons;
    }
}