package org.opentrafficsim.road.network.factory.nwb;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
                            collected++;
                            return (Long) null;
                        }
                    }
                }
                catch (OTSGeometryException e)
                {
                    e.printStackTrace();
                }
                return (Long) feature.getProperty("WVK_ID").getValue();
            }
        });
        long end = System.nanoTime();
        System.out.println(String.format("Data collection time %.3fs", (end - start) / 1e9));
        System.out.println("Retrieved " + roadData.size() + " records");

        start = System.nanoTime();
        ShapeFileReader lanes = new ShapeFileReader(baseDir + File.separator + "wegvakken", date,
                "Rijstroken" + File.separator + "Rijstroken.shp");
        Map<Long, Feature> laneData = lanes.readShapeFile(new FeatureQualifier()
        {

            @Override
            public Long qualify(Feature feature)
            {
                // System.out.println(feature);
                Long wvkId = (Long) feature.getProperty("WVK_ID").getValue();
                return roadData.containsKey(wvkId) ? wvkId : null;
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

        for (Long wvkId : roadData.keySet())
        {
            Feature feature = roadData.get(wvkId);
            OTSNode startNode = null;
            OTSNode endNode = null;
            OTSLine3D designLine = ShapeFileReader.designLine(feature);
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
                    String.format("%s.%d", feature.getProperty("STT_NAAM").getValue(), ++nextLinkNumber), startNode, endNode,
                    linkType, designLine, simulator, LaneKeepingPolicy.KEEPRIGHT);
            // System.out.println("Adding feature for " + wvkId + " which contains " + roadData.get(wvkId));
            link.addFeature("Wegvakken", feature);
            Feature f = laneData.get(wvkId);
            if (null != f)
            {
                link.addFeature("Basic roads database Rijstroken", f);
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
                            widthAtStart, widthAtEnd, standardLaneType, speedLimitMap, false);
                    cumulativeOffsetAtStart = cumulativeOffsetAtStart.minus(widthAtStart);
                    cumulativeOffsetAtEnd = cumulativeOffsetAtEnd.minus(widthAtEnd);
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
