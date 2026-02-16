package org.opentrafficsim.animation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opentrafficsim.base.logger.Logger;

/**
 * Run this file to download all the icons. Most icons are from the <a href="https://icon-sets.iconify.design/mdi/">Material
 * Design Icons</a> set on Iconify. Other icons are from the same site, but different icon sets. This class composes URL's to
 * the icon API which sets the color. This URL is forwarded to an online API which translates them in to 24x24px PNG's.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DownloadIcons
{

    /** Set of files saved to check there are no duplicates in script. */
    private static Set<String> fileNames;

    /**
     * Constructor.
     */
    private DownloadIcons()
    {
        //
    }

    /**
     * Main method.
     * @param args ignored
     */
    public static void main(final String[] args)
    {

        fileNames = new LinkedHashSet<>();

        // downloadIcon("mdi", "car-clock", "TravelTime");
        // downloadIcon("mdi", "car-connected", "Cacc");
        // downloadIcon("mdi", "car-cruise-control", "Acc");
        // downloadIcon("mdi", "car-speed-limiter", "Isa");

        downloadIcon("ic", "baseline-edit-road", "RoadLayout");
        downloadIcon("iconoir", "dot-arrow-right", "Connector");
        downloadIcon("ix", "road-filled", "Lane");
        downloadIcon("ix", "road", "Stripe");
        downloadIcon("mdi", "account-view-outline", "Social");
        downloadIcon("mdi", "arrow-decision", "Route", "rotate=90deg"); // rotate right
        downloadIcon("mdi", "arrow-expand-all", "ZoomAll");
        downloadIcon("mdi", "arrow-right-bold-circle-outline", "Step");
        downloadIcon("mdi", "arrow-up-down", "UpDown");
        downloadIcon("mdi", "bus-stop", "BusStop");
        downloadIcon("mdi", "calendar-clock-outline", "Calendar");
        downloadIcon("mdi", "car-electric", "Detector");
        downloadIcon("mdi", "car-emergency", "Shoulder");
        downloadIcon("mdi", "car-multiple", "Queue");
        downloadIcon("mdi", "car-side", "Gtu");
        downloadIcon("mdi", "chart-ppf", "Clothoid");
        downloadIcon("mdi", "chevron-down", "Expanded");
        downloadIcon("mdi", "chevron-right", "Collapsed");
        downloadIcon("mdi", "chevron-up", "Up");
        downloadIcon("mdi", "chevron-triple-right", "Generator");
        downloadIcon("mdi", "cog", "Application");
        downloadIcon("mdi", "cross-circle-outline", "Delete");
        downloadIcon("mdi", "database-outline", "Database");
        downloadIcon("mdi", "eye-outline", "Eye");
        downloadIcon("mdi", "file-outline", "File");
        downloadIcon("mdi", "file-import-outline", "Import");
        downloadIcon("mdi", "file-report-outline", "Output");
        downloadIcon("mdi", "filmstrip-box-multiple", "Scenario");
        downloadIcon("mdi", "folder-open-outline", "FolderOpen");
        downloadIcon("mdi", "folder-outline", "Folder");
        downloadIcon("mdi", "global-search", "Find");
        downloadIcon("mdi", "google-nearby", "Priority");
        downloadIcon("mdi", "graph", "Network", "rotate=270deg"); // rotate left
        downloadIcon("mdi", "grid", "Grid");
        downloadIcon("mdi", "home", "Home");
        downloadIcon("mdi", "horizontal-line", "CenterLine");
        downloadIcon("mdi", "information-outline", "Information");
        downloadIcon("mdi", "label-outline", "Id", "flip=horizontal"); // flip
        downloadIcon("mdi", "map-marker-check-outline", "Sink");
        downloadIcon("mdi", "menu-close", "Dropdown");
        downloadIcon("mdi", "pause-circle-outline", "Pause");
        downloadIcon("mdi", "play-circle-outline", "Play");
        downloadIcon("mdi", "puzzle-outline", "Component");
        downloadIcon("mdi", "question-mark-circle-outline", "Question");
        downloadIcon("mdi", "ray-start-end", "Link");
        downloadIcon("mdi", "ray-start-vertex-end", "Path");
        downloadIcon("mdi", "ray-vertex", "Node");
        downloadIcon("mdi", "routes", "Directions");
        downloadIcon("mdi", "sign-caution", "Blockage");
        downloadIcon("mdi", "sign-yield", "Yield");
        downloadIcon("mdi", "skip-next-circle-outline", "Next");
        downloadIcon("mdi", "slope-uphill", "Elevation");
        downloadIcon("mdi", "stopwatch-play-outline", "Run");
        downloadIcon("mdi", "table", "Table");
        downloadIcon("mdi", "text-long", "Text");
        downloadIcon("mdi", "texture-box", "Conflict");
        downloadIcon("mdi", "traffic-cone", "Roadworks");
        downloadIcon("mdi", "transit-connection-horizontal", "Centroid");
        downloadIcon("mdi", "variable", "Parameter");
        downloadIcon("mdi", "vector-bezier", "Bezier");
        downloadIcon("mdi", "vector-line", "Straight");
        downloadIcon("mdi", "vector-point", "Point");
        downloadIcon("mdi", "vector-polyline", "PolyLine");
        downloadIcon("mdi", "vector-radius", "Arc");
        downloadIcon("mdi", "warning-outline", "Warning");
        downloadIcon("tabler", "drone", "Perception");
        downloadIcon("tabler", "traffic-lights", "TrafficLight");

    }

    /**
     * Download icon.
     * @param iconSet icon set on Iconify
     * @param icon icon name on Iconify
     * @param file file name for in resources (minus "24.png")
     * @param transformations e.g. {@code flip=horizontal} or {@code rotate=45}
     */
    private static void downloadIcon(final String iconSet, final String icon, final String file,
            final String... transformations)
    {
        // Check no duplicates
        if (!fileNames.add(file))
        {
            Logger.ots().warn("File {} is specified at least twice.", file);
        }
        // Api at api.iconify.design setting color #0066c4
        String iconifyUrl = "https://api.iconify.design/%s/%s.svg?color=%%230066c4";
        for (String transformation : transformations)
        {
            iconifyUrl += ("&" + transformation);
        }
        String encoded = URLEncoder.encode(String.format(iconifyUrl, iconSet, icon), StandardCharsets.UTF_8);
        // Service to save .svg as .png
        String wsrvUrl = "https://wsrv.nl/?url=" + encoded + "&w=24&h=24&output=png";
        // Download to resources
        try (InputStream in = new URL(wsrvUrl).openStream())
        {
            Files.copy(in, Path.of("src", "main", "resources", "ots-icons", file + "24.png"),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (MalformedURLException exception)
        {
            Logger.ots().warn("Unable to download icon {}/{}", iconSet, icon);
        }
        catch (IOException exception)
        {
            Logger.ots().warn("Unable to save icon {}/{}", iconSet, icon);
        }
    }
}
