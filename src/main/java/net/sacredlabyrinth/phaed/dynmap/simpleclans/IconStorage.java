package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class IconStorage {
    private final @NotNull MarkerAPI markerAPI;
    private final @NotNull Set<MarkerIcon> iconSet;
    private final @NotNull MarkerIcon defaultIcon;

    /**
     * Creates an unmodified storage for {@link MarkerIcon}.
     *
     * @param workingPath The path of working directory, where icons stored.
     * @param defaultIcon The icon, located inside working directory.
     *                    Used if working directory doesn't contain any icons.
     * @param markerAPI   {@link MarkerAPI}
     * @throws IOException if workingPath is invalid.
     */
    public IconStorage(@NotNull String workingPath, @NotNull String defaultIcon, @NotNull MarkerAPI markerAPI) throws IOException {
        if (!isValidPath(workingPath)) {
            throw new IOException(String.format("Provided workingPath ( %s ) is invalid!", workingPath));
        }

        File workingDir = new File(workingPath);
        this.iconSet = getIconsIn(workingDir);
        this.defaultIcon = markerAPI.createMarkerIcon(defaultIcon, defaultIcon,
                DynmapSimpleClans.class.getResourceAsStream(workingPath + File.separator + defaultIcon));
        this.markerAPI = markerAPI;
    }

    /**
     * @return {@link MarkerIcon} from the working directory or the default one
     */
    public @NotNull MarkerIcon getIcon(@NotNull String iconName) {
        Optional<MarkerIcon> iconOptional = iconSet.stream().
                filter(markerIcon -> Objects.equals(markerIcon.getMarkerIconLabel(), iconName)).
                findAny();

        return iconOptional.orElse(defaultIcon);
    }

    /**
     * Retrieves all {@link MarkerIcon} in defined folder
     */
    private @NotNull Set<MarkerIcon> getIconsIn(@NotNull File iconsFolder) {
        File[] files = iconsFolder.listFiles(pathname -> {
            String mimeType = new MimetypesFileTypeMap().getContentType(pathname);
            return mimeType.split("/")[0].equals("image");
        });

        if (files == null) {
            return new HashSet<>();
        }

        HashSet<MarkerIcon> icons = new HashSet<>();
        for (File icon : files) {
            String name = icon.getName();
            String nameWithoutExt = name.substring(0, name.lastIndexOf("."));
            try (FileInputStream stream = new FileInputStream(icon)) {
                MarkerIcon markerIcon = markerAPI.createMarkerIcon(nameWithoutExt, nameWithoutExt, stream);
                if (markerIcon != null) {
                    icons.add(markerIcon);
                }
            } catch (IOException ex) {
                DynmapSimpleClans.getInstance().getLogger().severe(
                        String.format("Error occurred while trying to create %s icon: %s", name, ex.getMessage()));
            }
        }

        return Collections.unmodifiableSet(icons);
    }

    /**
     * Checks if a string is a valid path.
     * Null safe.
     *
     * <pre>
     * Calling examples:
     *    isValidPath("c:/test");      // returns true
     *    isValidPath("c:/te:t");      // returns false
     *    isValidPath("c:/te?t");      // returns false
     *    isValidPath("c/te*t");       // returns false
     *    isValidPath("good.txt");     // returns true
     *    isValidPath("not|good.txt"); // returns false
     *    isValidPath("not:good.txt"); // returns false
     * </pre>
     */
    private static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (NullPointerException ex) {
            return false;
        }
        return true;
    }
}
