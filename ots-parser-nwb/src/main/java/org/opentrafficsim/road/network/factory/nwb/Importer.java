package org.opentrafficsim.road.network.factory.nwb;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opengis.feature.Feature;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader.FeatureQualifier;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Access to the NWB (Nationaal WegenBestand - Dutch National Road database) shape files.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 may 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Importer extends OTSSimulationApplication<OTSModelInterface>
{

    /** ... */
    private static final long serialVersionUID = 20190514L;

    /**
     * Construct a viewer for imported NWB data.
     * @param model model
     * @param animationPanel animation panel
     * @throws OTSDrawingException when animation panel cannot be drawn
     */
    public Importer(OTSModelInterface model, OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
    }

    /**
     * Program entry point.
     * @param args String[]; command line arguments; currently not used
     * @throws OTSDrawingException ...
     * @throws SimRuntimeException cannot happen
     * @throws NamingException When a name collision occurs
     * @throws IOException When a file could not be read
     * @throws OTSGeometryException When the geometry of a design line contains duplicate points
     * @throws NetworkException Should not happen
     * @throws IllegalAccessException on error
     * @throws IllegalArgumentException on error
     * @throws SecurityException on error
     * @throws NoSuchFieldException on error
     */
    public static void main(final String[] args)
            throws OTSDrawingException, SimRuntimeException, NamingException, IOException, OTSGeometryException,
            NetworkException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
    {
        System.out.println("Parsing road sections");
        String baseDir = "c:\\NWB";
        int date = 20190401;
        long start = System.nanoTime();
        ShapeFileReader shr =
                new ShapeFileReader(baseDir + File.separator + "wegen", date, "Wegvakken" + File.separator + "Wegvakken.shp");
        Map<Long, Feature> roadData = shr.readShapeFile(new FeatureQualifier()
        {
            int count = 0;

            int collected = 0;

            @Override
            public Long qualify(Feature feature)
            {
                if (++count % 100000 == 0)
                {
                    System.out.println("processed " + count + " road sections, collected " + collected);
                }
                try
                {
                    for (OTSPoint3D p : ShapeFileReader.designLine(feature).getPoints())
                    {
                        if (p.x < 80000 || p.y < 444000 || p.x > 90000 || p.y > 455000)
                        {
                            return (Long) null;
                        }
                    }
                }
                catch (OTSGeometryException e)
                {
                    e.printStackTrace();
                }
                collected++;
                return (Long) feature.getProperty("WVK_ID").getValue();
            }
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + roadData.size() + " records");
        Map<String, Map<Long, Feature>> additionalData = new LinkedHashMap<>();
        List<String> databases = Arrays.asList(new String[] { "adv_snelheden", "beb_kommen", "convergenties", "divergenties",
                "doelgroepstroken", "gel_beperkingen", "inhaalverboden", "kantstroken", "kruispunten", "kunstinweg",
                "kunstoverweg", "lichtmasten", "max_snelheden", "mengstroken", "portalen", "rijbanen", "rijstroken",
                "signaleringen", "spoorovergangen", "strksignaleringn", "verhardingen", "verlichtingen", "voorrangswegen",
                "wegbermen", "wegcat_beleving", "wegcat_formeel" });
        for (String database : databases)
        {
            start = System.nanoTime();
            ShapeFileReader lanes = new ShapeFileReader(baseDir + File.separator + "wegvakken", date,
                    database + File.separator + database + ".shp");
            Map<Long, Feature> laneData = lanes.readShapeFile(new FeatureQualifier()
            {

                @Override
                public Long qualify(Feature feature)
                {
                    Long wvkId = (Long) feature.getProperty("WVK_ID").getValue();
                    return roadData.containsKey(wvkId) ? wvkId : null;
                }
            });
            end = System.nanoTime();
            System.out.println(String.format("Data collection time for %s %.3fs", database, (end - start) / 1e9));
            System.out.println("Retrieved " + laneData.size() + " records");
            additionalData.put(database, laneData);
        }
        /*
         * The kenmerken shape files do not have a WVK_ID column. If we read those, we would have to match to WVK_ID using road
         * number and distance range. At this time I have the impression that the kenmerken shape files do not add anything that
         * is not in the wegvakken shape files.
         */

        OTSRoadNetwork network = new OTSRoadNetwork("NWB import", true);

        int nextNodeNumber = 0;

        int nextLinkNumber = 0;

        LinkType freeWayLinkType = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LinkType roadLinkType = network.getLinkType(LinkType.DEFAULTS.ROAD);

        OTSAnimator simulator = new OTSAnimator();

        NWBModel nwbModel = new NWBModel(simulator, network, "NWB network", "NWB network");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), nwbModel);

        Length standardLaneWidth = Length.instantiateSI(3.5);

        LaneType freeWay = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        LaneType highWay = network.getLaneType(LaneType.DEFAULTS.HIGHWAY);
        LaneType urbanWay = network.getLaneType(LaneType.DEFAULTS.URBAN_ROAD_LANE);

        Map<GTUType, Speed> speedLimitMap = new LinkedHashMap<>();

        for (Long wvkId : roadData.keySet())
        {
            Feature feature = roadData.get(wvkId);
            OTSRoadNode startNode = null;
            OTSRoadNode endNode = null;
            OTSLine3D designLine = ShapeFileReader.designLine(feature);
            OTSPoint3D prevPoint = null;
            for (OTSPoint3D p : designLine.getPoints())
            {
                String nodeName = String.format("Node%d", ++nextNodeNumber);
                Direction direction ;
                if (null == startNode)
                {
                    OTSPoint3D nextPoint = designLine.getPoints()[1];
                    direction = Direction.instantiateSI(Math.atan2(nextPoint.y - p.y, nextPoint.x - p.x));
                }
                else
                {
                    direction = Direction.instantiateSI(Math.atan2(p.y - prevPoint.y, p.x - prevPoint.x));
                }
                OTSRoadNode node = new OTSRoadNode(network, nodeName, p, direction);
                if (null == startNode)
                {
                    startNode = node;
                }
                endNode = node;
                prevPoint = p;
            }
            ExtendedCrossSectionLink link = new ExtendedCrossSectionLink(network,
                    String.format("%s.%d", feature.getProperty("STT_NAAM").getValue(), ++nextLinkNumber), startNode, endNode,
                    "R".equals(feature.getProperty("WEGBEHSRT").getValue()) ? freeWayLinkType : roadLinkType, designLine,
                    simulator, LaneKeepingPolicy.KEEPRIGHT);
            // System.out.println("Adding feature for " + wvkId + " which contains " + roadData.get(wvkId));
            link.addFeature("Wegvakken", feature);
            for (String database : databases)
            {
                Feature f = additionalData.get(database).get(wvkId);
                if (null != f)
                {
                    link.addFeature(database, f);
                    if ("rijstroken".equals(database))
                    {
                        LaneType laneType = null;
                        switch ((String) feature.getProperty("WEGBEHSRT").getValue())
                        {
                            case "R":
                                laneType = freeWay;
                                break;
                            case "P":
                                laneType = highWay;
                                break;
                            default: // G, T, W
                                laneType = urbanWay;
                                break;
                        }
                        // We don't know lateral positions of the CrossSectionElements; this leads to odd problems...
                        String laneFormat = (String) f.getProperty("OMSCHR").getValue();
                        int lanesAtStart = Integer.parseInt(laneFormat.substring(0, 1));
                        int lanesAtEnd = Integer.parseInt(laneFormat.substring(5, 6));
                        int maxLaneNo = Math.max(lanesAtStart, lanesAtEnd);
                        Length cumulativeOffsetAtStart = Length.ZERO;
                        Length cumulativeOffsetAtEnd = Length.ZERO;
                        for (int laneNo = 0; laneNo < maxLaneNo; laneNo++)
                        {
                            Length widthAtStart = laneNo < lanesAtStart - lanesAtEnd ? Length.ZERO : standardLaneWidth;
                            Length widthAtEnd = laneNo < lanesAtEnd - lanesAtStart ? Length.ZERO : standardLaneWidth;
                            new Lane(link, String.format("Lane %d", laneNo + 1), cumulativeOffsetAtStart, cumulativeOffsetAtEnd,
                                    widthAtStart, widthAtEnd, laneType, speedLimitMap, false);
                            cumulativeOffsetAtStart = cumulativeOffsetAtStart.minus(widthAtStart);
                            cumulativeOffsetAtEnd = cumulativeOffsetAtEnd.minus(widthAtEnd);
                        }
                    }
                }
            }
        }

        OTSAnimationPanel animationPanel = new OTSAnimationPanel(network.getExtent(), new Dimension(800, 600), simulator,
                nwbModel, DEFAULT_COLORER, nwbModel.getNetwork());
        new Importer(nwbModel, animationPanel);
    }

    /**
     * The Model.
     */
    static class NWBModel extends AbstractOTSModel
    {
        /** ... */
        private static final long serialVersionUID = 20190514L;

        /** The network. */
        private final OTSRoadNetwork network;

        /**
         * Construct a new NWBModel.
         * @param simulator OTSSimulatorInterface; required for rendering
         * @param network OTSNetwork; the network
         * @param shortName String; concise name
         * @param description String; descriptive name
         */
        public NWBModel(OTSSimulatorInterface simulator, OTSRoadNetwork network, String shortName, String description)
        {
            super(simulator, shortName, description);
            this.network = network;
        }

        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            return; // Do nothing
        }
    }
}
