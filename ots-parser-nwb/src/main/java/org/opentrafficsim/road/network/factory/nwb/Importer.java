package org.opentrafficsim.road.network.factory.nwb;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opengis.feature.Feature;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
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
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.nwb.ShapeFileReader.FeatureQualifier;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.LaneType.DEFAULTS;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Access to the NWB (Nationaal WegenBestand - Dutch National Road database) shape files.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param model
     * @param animationPanel
     * @throws OTSDrawingException
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
     */
    public static void main(final String[] args) throws OTSDrawingException, SimRuntimeException, NamingException, IOException,
            OTSGeometryException, NetworkException
    {
        System.out.println("Parsing road sections");
        String baseDir = "c:\\NWB";
        int date = 20190401;
        long start = System.nanoTime();
        ShapeFileReader shr =
                new ShapeFileReader(baseDir + File.separator + "wegen", date, "Wegvakken" + File.separator + "Wegvakken.shp");
        List<Feature> roadData = shr.readShapeFile(new FeatureQualifier()
        {
            int count = 0;

            int collected = 0;

            @Override
            public boolean qualify(Feature feature)
            {
                if (++count % 100000 == 0)
                {
                    System.out.println("processed " + count + " road sections, collected " + collected);
                }
                try
                {
                    for (OTSPoint3D p : FeatureViewer.designLine(feature).getPoints())
                    {
                        if (p.x < 80000 || p.y < 444000 || p.x > 90000 || p.y > 455000)
                        {
                            return false;
                        }
                    }
                }
                catch (OTSGeometryException e)
                {
                    e.printStackTrace();
                }
                collected++;
                return true;
                // String roadNumber = ((String) feature.getProperty("WEGNUMMER").getValue());
                // return roadNumber.contains("020") || roadNumber.contains("470") || roadNumber.contains("013")
                // || roadNumber.contains("471") || roadNumber.contains("209") || roadNumber.contains("472")
                // || roadNumber.contains("473");
            }
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + roadData.size() + " records");

        Map<Integer, Feature> wvkMap = new HashMap<>();
        for (Feature feature : roadData)
        {
            wvkMap.put(Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue()), feature);
        }
        start = System.nanoTime();
        ShapeFileReader lanes = new ShapeFileReader(baseDir + File.separator + "wegvakken", date,
                "Rijstroken" + File.separator + "Rijstroken.shp");
        List<Feature> laneData = lanes.readShapeFile(new FeatureQualifier()
        {

            @Override
            public boolean qualify(Feature feature)
            {
                // System.out.println(feature);
                Integer wvkId = Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue());
                return wvkMap.containsKey(wvkId);
                // if (((String) feature.getProperty("WEGNUMMER").getValue()).contains("013"))
                // {
                // System.out.println(feature);
                // }
                // return ((String) feature.getProperty("WEGNUMMER").getValue()).contains("013");
            }
        });
        end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + laneData.size() + " records");
        OTSRoadNetwork network = new OTSRoadNetwork("NWB import", true);
        int nextNodeNumber = 0;
        int nextLinkNumber = 0;
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<>((LinkType) null);
        LinkType linkType = new LinkType("Road", null, compatibility, network);
        OTSAnimator simulator = new OTSAnimator();
        NWBModel nwbModel = new NWBModel(simulator, network, "NWB network", "NWB network");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), nwbModel);
        Length standardLaneWidth = Length.createSI(3.5);
        LaneType standardLaneType = network.getLaneType(DEFAULTS.HIGHWAY);
        Map<GTUType, Speed> speedLimitMap = new HashMap<>();

        for (Feature feature : laneData)
        {
            OTSNode startNode = null;
            OTSNode endNode = null;
            OTSLine3D designLine = FeatureViewer.designLine(feature);
            for (OTSPoint3D p : designLine.getPoints())
            {
                String nodeName = String.format("Node%d", ++nextNodeNumber);
                OTSNode node = new OTSNode(network, nodeName, p);
                if (null == startNode)
                {
                    startNode = node;
                }
                endNode = node;
            }
            ExtendedCrossSectionLink link = new ExtendedCrossSectionLink(network,
                    String.format("%s.%d", feature.getProperty("WVK_ID").getValue(), ++nextLinkNumber), startNode, endNode,
                    linkType, designLine, simulator, LaneKeepingPolicy.KEEPRIGHT);
            Integer wvkId = Math.toIntExact((Long) feature.getProperty("WVK_ID").getValue());
            System.out.println("Adding feature for " + wvkId + " which contains " + wvkMap.get(wvkId));
            link.addFeature("Wegvakken", wvkMap.get(wvkId));
            link.addFeature("Basic roads database Rijstroken", feature);
            // We don't know lateral positions of the CrossSectionElements; this leads to odd problems...
            String laneFormat = (String) feature.getProperty("OMSCHR").getValue();
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
                        widthAtStart, widthAtEnd, standardLaneType, speedLimitMap, false);
                cumulativeOffsetAtStart = cumulativeOffsetAtStart.minus(widthAtStart);
                cumulativeOffsetAtEnd = cumulativeOffsetAtEnd.minus(widthAtEnd);
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
