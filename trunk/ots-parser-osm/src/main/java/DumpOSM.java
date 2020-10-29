import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.opentrafficsim.road.network.factory.osm.OSMNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMNode;
import org.opentrafficsim.road.network.factory.osm.OSMTag;
import org.opentrafficsim.road.network.factory.osm.OSMWay;
import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;
import org.opentrafficsim.road.network.factory.osm.input.ReadOSMFile;

/**
 * Dump an OSM file.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 12, 2018 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DumpOSM implements ProgressListener
{

    /**
     * Program execution entry point.
     * @param args String[]; command line arguments (not used)
     * @throws FileNotFoundException ...
     * @throws MalformedURLException ...
     * @throws URISyntaxException ... 
     */
    public static void main(final String[] args) throws FileNotFoundException, MalformedURLException, URISyntaxException
    {
        String fileName = "file:///C:/OldD/TUD.osm";
        ArrayList<OSMTag> wantedTags = new ArrayList<>();
        // wantedTags.add(new OSMTag("highway", "primary"));
        // wantedTags.add(new OSMTag("highway", "secondary"));
        // wantedTags.add(new OSMTag("highway", "tertiary"));
        // wantedTags.add(new OSMTag("highway", "cycleway"));
        // wantedTags.add(new OSMTag("highway", "trunk"));
        // wantedTags.add(new OSMTag("highway", "path"));
        // wantedTags.add(new OSMTag("cycleway", "lane"));
        // wantedTags.add(new OSMTag("highway", "residential"));
        // wantedTags.add(new OSMTag("highway", "service"));
        // wantedTags.add(new OSMTag("highway", "motorway"));
        // wantedTags.add(new OSMTag("highway", "bus_stop"));
        // wantedTags.add(new OSMTag("highway", "motorway_link"));
        // wantedTags.add(new OSMTag("highway", "unclassified"));
        // wantedTags.add(new OSMTag("highway", "footway"));
        // wantedTags.add(new OSMTag("cycleway", "track"));
        // wantedTags.add(new OSMTag("highway", "road"));
        // wantedTags.add(new OSMTag("highway", "pedestrian"));
        // wantedTags.add(new OSMTag("highway", "track"));
        // wantedTags.add(new OSMTag("highway", "living_street"));
        // wantedTags.add(new OSMTag("highway", "tertiary_link"));
        // wantedTags.add(new OSMTag("highway", "secondary_link"));
        // wantedTags.add(new OSMTag("highway", "primary_link"));
        // wantedTags.add(new OSMTag("highway", "trunk_link"));
        ArrayList<String> filteredKeys = new ArrayList<>();
        ProgressListener listener = new DumpOSM();
        ReadOSMFile osmf = new ReadOSMFile(fileName, wantedTags, filteredKeys, listener);
        OSMNetwork net = osmf.getNetwork();
        Map<Long, OSMNode> nodeMap = net.getNodes();
        for (Long key : nodeMap.keySet())
        {
            OSMNode node = nodeMap.get(key);
            System.out.println(node);
        }
        Map<Long, OSMWay> wayMap = net.getWays();
        for (Long key : wayMap.keySet())
        {
            OSMWay way = wayMap.get(key);
            System.out.println(way);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void progress(final ProgressEvent progressEvent)
    {
        System.out.println(progressEvent.getProgress());
    }
}
