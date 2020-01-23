package org.opentrafficsim.road.network.factory.shape;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.pmw.tinylog.Level;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.swing.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.swing.gui.DSOLApplication;
import nl.tudelft.simulation.dsol.swing.gui.DSOLPanel;

/**
 * Test model for parsing ESRI shape files.
 */
public class TestShapeParser extends DSOLApplication
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param title the title of the window
     * @param panel the panel to display the animation
     */
    public TestShapeParser(final String title, final DSOLPanel panel)
    {
        super(title, panel);
        panel.getConsole().setLogLevel(Level.TRACE);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     * @throws NamingException on animation creation error
     * @throws RemoteException on animation panel failure
     */
    public static void main(final String[] args) throws SimRuntimeException, NamingException, RemoteException
    {
        SimLogger.setAllLogLevel(Level.TRACE);
        OTSAnimator simulator = new OTSAnimator();
        GisNDWImport model = new GisNDWImport(simulator);
        DSOLPanel panel = new DSOLPanel(model, simulator);
        AnimationPanel animationPanel =
                new AnimationPanel(new Rectangle2D.Double(125000, 375000, 40000, 35000), new Dimension(800, 600), simulator);
        animationPanel.toggleClass(LinkAnimation.Text.class);
        animationPanel.toggleClass(NodeAnimation.Text.class);
        panel.getTabbedPane().add("animation", animationPanel);
        panel.getTabbedPane().setSelectedIndex(1);
        OTSReplication replication =
                OTSReplication.create("rep1", Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        SimLogger.setSimulator(simulator);
        new TestShapeParser("TestShapeParser", panel);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestShapeParser []";
    }

    /**
     * Model to test the Esri Shape File Format parser.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * initial version Jun 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class GisNDWImport extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The network. */
        private final OTSRoadNetwork network = new OTSRoadNetwork("test network", true);

        /**
         * Create the test model.
         * @param simulator the simulator
         */
        public GisNDWImport(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            try
            {
                // open the NWB basic shape file
                Map<String, AbstractNWBRoadElement> roadMapNWB = getRoadMapNWB("/A58", "NWB_A58", "NWB_wegvakken");

                // open the shape file with driving lane information
                Map<String, AbstractNWBRoadElement> laneMapNWB = getRoadMapNWB("/A58", "rijstroken_A58", "NWB_rijstroken");

                // open the shape file with specific lane information such as on and off ramps and weaving areas
                Map<String, AbstractNWBRoadElement> specialLaneMapNWB =
                        getRoadMapNWB("/A58", "mengstroken_A58", "NWB_mengstroken");

                combineNWBMaps(roadMapNWB, laneMapNWB, specialLaneMapNWB);
            }
            catch (NetworkException nwe)
            {
                nwe.printStackTrace();
            }

        }

        /**
         * Combine the data (split road elements and add lane-attributes).
         * @param roadMapNWB Map&lt;String,AbstractNWBRoadElement&gt;;
         * @param laneMapNWB Map&lt;String,AbstractNWBRoadElement&gt;;
         * @param specialLaneMapNWB Map&lt;String,AbstractNWBRoadElement&gt;;
         */
        private void combineNWBMaps(Map<String, AbstractNWBRoadElement> roadMapNWB,
                Map<String, AbstractNWBRoadElement> laneMapNWB, Map<String, AbstractNWBRoadElement> specialLaneMapNWB)
        {
            // TODO : Alexander, de combinemaps moet verder worden uitgewerkt!
            // Here, a segment of the NWB wegvak is being extracted (for instance the first part of the wegvak has an on-ramp)
            // We should thus split this NWB-wegvak in two parts, the first part with the on-ramp (additional lane) and the
            // remainder with the original attributes
            for (AbstractNWBRoadElement laneElement : laneMapNWB.values())
            {
                NWBDrivingLane lane = (NWBDrivingLane) laneElement;
                NWBRoadElement road = (NWBRoadElement) roadMapNWB.get(lane.getRoadId());
                if (road != null)
                {
                    List<LineString> lineSegmentList = splitRoad(road, lane);
                }
                else
                {
                    System.err.println("road for lane " + lane.toString() + " does not exist");
                }
            }
        }

        /**
         * Split a road if there is a lane along a PART of this road.
         * @param road NWBRoadElement;
         * @param segment NWBDrivingLane;
         * @return list of linestrings
         */
        private List<LineString> splitRoad(NWBRoadElement road, NWBDrivingLane segment)
        {
            MultiLineString lines = (MultiLineString) road.getMyGeom();
            LineString line = (LineString) lines.getGeometryN(0);
            List<LineString> lineSegmentList = new ArrayList<>();

            // The getSubstring is copied from The JCS Conflation Suite (JCS): I assume it is not supported anymore, but can
            // still be found...
            lineSegmentList.add(SubstringLine.getSubstring(line, segment.getBeginDistance(), segment.getEndDistance()));
            if (segment.getBeginDistance() > 0)
            {
                lineSegmentList.add(SubstringLine.getSubstring(line, 0, segment.getBeginDistance()));
            }
            if (segment.getEndDistance() < road.getEndDistance())
            {
                lineSegmentList.add(SubstringLine.getSubstring(line, 0, segment.getBeginDistance()));
            }
            return lineSegmentList;
        }

        /**
         * Import a list of road (link) elements from a shape file
         * @param initialDir String;
         * @param fileName String;
         * @param shapeIdentifier String;
         * @return map of naames road elements
         * @throws NetworkException network exception
         */
        private Map<String, AbstractNWBRoadElement> getRoadMapNWB(String initialDir, String fileName, String shapeIdentifier)
                throws NetworkException
        {
            FileDataStore dataStoreLink = null;
            try
            {
                dataStoreLink = newDatastore(initialDir, fileName);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // open and read shape file links
            FeatureIterator feautureIterator = getFeatureIterator(dataStoreLink);
            // loop through the features and first retrieve the geometry
            Map<String, AbstractNWBRoadElement> roadMApNWB = getFeatureAttributes(feautureIterator, shapeIdentifier);
            return roadMApNWB;

        }

        /**
         * @param initialDir String;
         * @param fileName String;
         * @return shapefile datastore
         * @throws IOException on IO exception
         */
        private FileDataStore newDatastore(String initialDir, final String fileName) throws IOException
        {
            try
            {
                URL url = URLResource.getResource(initialDir);
                File iniDir = new File(url.getFile());
                File file = new File(iniDir, fileName + ".shp");
                System.out.println(file + "  -- exists: " + file.exists());

                ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
                ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createDataStore(file.toURI().toURL());

                FileDataStore dataStoreLink = FileDataStoreFinder.getDataStore(file);
                System.out.println(dataStore);
                return dataStore;

            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            return null;

        }

        /**
         * @param dataStore FileDataStore;
         * @return iterator
         */
        private FeatureIterator getFeatureIterator(FileDataStore dataStore)
        {
            try
            {
                String[] typeNameLink = dataStore.getTypeNames();
                SimpleFeatureSource sourceLink;
                sourceLink = dataStore.getFeatureSource(typeNameLink[0]);
                SimpleFeatureCollection featuresLink = sourceLink.getFeatures();
                return featuresLink.features();

            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @param feautureIterator FeatureIterator;
         * @param shapeIdentifier String;
         * @return feature attributes
         * @throws NetworkException on network exception
         */
        private Map<String, AbstractNWBRoadElement> getFeatureAttributes(final FeatureIterator feautureIterator,
                String shapeIdentifier) throws NetworkException
        {
            Map<String, AbstractNWBRoadElement> roadMap = new LinkedHashMap<>();
            while (feautureIterator.hasNext())
            {
                Feature feature = feautureIterator.next();
                // geometry is always first
                if (shapeIdentifier.equals("NWB_wegvakken"))
                {
                    NWBRoadElement road = getPropertiesNWB(feature);
                    roadMap.put(road.getRoadId(), road);
                }
                else if (shapeIdentifier.equals("NWB_rijstroken"))
                {
                    NWBDrivingLane road = getPropertiesDrivingLanes(feature);
                    roadMap.put(road.getRoadId(), road);
                }
                else if (shapeIdentifier.equals("NWB_mengstroken"))
                {
                    NWBDrivingLane road = getPropertiesSpecialLanes(feature);
                    roadMap.put(road.getRoadId(), road);
                }
            }
            return roadMap;
        }

        /**
         * @param feature Feature;
         * @return one road element with properties
         * @throws NetworkException on network exception
         */
        private NWBRoadElement getPropertiesNWB(final Feature feature) throws NetworkException
        {
            Geometry theGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();
            Coordinate[] coordinates = theGeom.getCoordinates();
            Property property = feature.getProperty("WVK_ID");
            String roadId = property.getValue().toString();
            property = feature.getProperty("JTE_ID_BEG");
            String junctionIdBegin = property.getValue().toString();
            property = feature.getProperty("JTE_ID_END");
            String junctionIdEnd = property.getValue().toString();
            property = feature.getProperty("ADMRICHTNG");
            String adminDirection = property.getValue().toString();
            property = feature.getProperty("RIJRICHTNG");
            String drivingDirection = property.getValue().toString();
            property = feature.getProperty("BEGINKM");
            Double beginKM = parseDouble(property);
            property = feature.getProperty("EINDKM");
            Double endKM = parseDouble(property);
            property = feature.getProperty("BEGAFSTAND");
            Double beginDistance = parseDouble(property);
            property = feature.getProperty("ENDAFSTAND");
            Double endDistance = parseDouble(property);

            OTSNode startNode;
            if (this.network.containsNode(junctionIdBegin))
                startNode = (OTSNode) this.network.getNode(junctionIdBegin);
            else
                startNode = new OTSNode(this.network, junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode;
            if (this.network.containsNode(junctionIdEnd))
                endNode = (OTSNode) this.network.getNode(junctionIdEnd);
            else
                endNode = new OTSNode(this.network, junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            NWBRoadElement road = new NWBRoadElement(theGeom, startNode, endNode, roadId, beginDistance, endDistance,
                    junctionIdBegin, junctionIdEnd, adminDirection, drivingDirection, beginKM, endKM);
            if (getSimulator() instanceof AnimatorInterface)
            {
                try
                {
                    System.out.println(startNode);
                    new NodeAnimation(startNode, this.simulator);
                    new NodeAnimation(endNode, this.simulator);
                    OTSLink link = new OTSLink(this.network, UUID.randomUUID().toString(), startNode, endNode, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(coordinates), this.simulator);
                    new LinkAnimation(link, this.simulator, 2.0f);
                }
                catch (RemoteException | NamingException | OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
            }
            return road;
        }

        /**
         * @param feature Feature;
         * @return info on one driving lane
         * @throws NetworkException on network exception
         */
        private NWBDrivingLane getPropertiesDrivingLanes(final Feature feature) throws NetworkException
        {
            Geometry theGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();
            Coordinate[] coordinates = theGeom.getCoordinates();

            Property property = feature.getProperty("WVK_ID");
            String roadId = property.getValue().toString();

            property = feature.getProperty("KANTCODE");
            String sideCode = property.getValue().toString();

            property = feature.getProperty("BEGAFSTAND");
            Double beginDistance = parseDouble(property);

            property = feature.getProperty("ENDAFSTAND");
            Double endDistance = parseDouble(property);

            property = feature.getProperty("OMSCHR");
            String laneDescription = property.getValue().toString();
            String[] lanes = laneDescription.split("->");
            Integer startNumberOfLanes = Integer.parseInt(lanes[0].trim());
            Integer endNumberOfLanes = Integer.parseInt(lanes[1].trim());

            String junctionIdBegin = roadId + "_" + beginDistance;
            String junctionIdEnd = roadId + "_" + endDistance;

            OTSNode startNode;
            if (this.network.containsNode(junctionIdBegin))
                startNode = (OTSNode) this.network.getNode(junctionIdBegin);
            else
                startNode = new OTSNode(this.network, junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode;
            if (this.network.containsNode(junctionIdEnd))
                endNode = (OTSNode) this.network.getNode(junctionIdEnd);
            else
                endNode = new OTSNode(this.network, junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            NWBDrivingLane lane = new NWBDrivingLane(theGeom, startNode, endNode, roadId, beginDistance, endDistance,
                    startNumberOfLanes, endNumberOfLanes, sideCode);
            if (getSimulator() instanceof AnimatorInterface)
            {
                try
                {
                    new NodeAnimation(startNode, this.simulator);
                    new NodeAnimation(endNode, this.simulator);
                    OTSLink link = new OTSLink(this.network, UUID.randomUUID().toString(), startNode, endNode, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(coordinates), this.simulator);
                    new LinkAnimation(link, this.simulator, 1.0f);
                }
                catch (RemoteException | NamingException | OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
            }
            return lane;
        }

        /**
         * @param feature Feature;
         * @return info on one special lane
         * @throws NetworkException on network exception
         */
        private NWBDrivingLane getPropertiesSpecialLanes(final Feature feature) throws NetworkException
        {
            Geometry theGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();
            Coordinate[] coordinates = theGeom.getCoordinates();

            Property property = feature.getProperty("WVK_ID");
            String roadId = property.getValue().toString();

            property = feature.getProperty("KANTCODE");
            String sideCode = property.getValue().toString();

            property = feature.getProperty("BEGAFSTAND");
            Double beginDistance = parseDouble(property);

            property = feature.getProperty("ENDAFSTAND");
            Double endDistance = parseDouble(property);

            property = feature.getProperty("OMSCHR");
            String laneType = property.getValue().toString();

            property = feature.getProperty("AANT_MSK");
            String laneDescription = property.getValue().toString();
            String[] lanes = laneDescription.split("->");
            Integer startNumberOfLanes = Integer.parseInt(lanes[0].trim().substring(0, 1));
            Integer endNumberOfLanes = Integer.parseInt(lanes[1].trim().substring(0, 1));

            String junctionIdBegin = roadId + "_" + beginDistance;
            String junctionIdEnd = roadId + "_" + endDistance;
            OTSNode startNode;
            if (this.network.containsNode(junctionIdBegin))
                startNode = (OTSNode) this.network.getNode(junctionIdBegin);
            else
                startNode = new OTSNode(this.network, junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode;
            if (this.network.containsNode(junctionIdEnd))
                endNode = (OTSNode) this.network.getNode(junctionIdEnd);
            else
                endNode = new OTSNode(this.network, junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            NWBDrivingLane lane = new NWBDrivingLane(theGeom, startNode, endNode, roadId, beginDistance, endDistance,
                    startNumberOfLanes, endNumberOfLanes, sideCode);
            if (getSimulator() instanceof AnimatorInterface)
            {
                try
                {
                    new NodeAnimation(startNode, this.simulator);
                    new NodeAnimation(endNode, this.simulator);
                    OTSLink link = new OTSLink(this.network, UUID.randomUUID().toString(), startNode, endNode, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(coordinates), this.simulator);
                    new LinkAnimation(link, this.simulator, 1.0f);
                }
                catch (RemoteException | NamingException | OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
            }
            return lane;
        }

        /**
         * @param property Property;
         * @return a double
         */
        private Double parseDouble(Property property)
        {
            if (property.getValue() != null)
            {
                if (property.getValue().toString() != null)
                {
                    return Double.parseDouble(property.getValue().toString());
                }
            }
            return Double.NaN;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

    }

}
