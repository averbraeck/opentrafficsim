package org.opentrafficsim.road.network.factory.shape;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

public class TestShapeParser extends AbstractWrappableAnimation {

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    TestShapeParser xmlModel = new TestShapeParser();
                    // 1 hour simulation run for testing
                    xmlModel.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND), new Duration(
                        60.0, TimeUnit.MINUTE), new ArrayList<AbstractProperty<?>>(), null, true);
                } catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName() {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description() {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads() {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) {
        return new GisNDWImport();
    }

    /** {@inheritDoc} */
    @Override
    protected final java.awt.geom.Rectangle2D.Double makeAnimationRectangle() {
        return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "TestGISParser []";
    }

    /**
     * Model to test the Esri Shape File Format parser.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    /**
     * @author P070518
     */
    class GisNDWImport implements OTSModelInterface {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
                throws SimRuntimeException {

            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

            // open the NWB basic shape file
            Map<String, AbstractNWBRoadElement> roadMapNWB = getRoadMapNWB("A58", "NWB_A58", "NWB_wegvakken");

            // open the shape file with driving lane information
            Map<String, AbstractNWBRoadElement> laneMapNWB = getRoadMapNWB("A58", "rijstroken_A58", "NWB_rijstroken");

            // open the shape file with specific lane information such as on and off ramps and weaving areas
            Map<String, AbstractNWBRoadElement> specialLaneMapNWB = getRoadMapNWB("A58", "mengstroken_A58",
                "NWB_mengstroken");

            combineNWBMaps(roadMapNWB, laneMapNWB, specialLaneMapNWB);

        }

        /**
         * Combine the data (split road elements and add lane-attributes).
         * @param roadListNWB
         * @param laneListNWB
         * @param specialLaneListNWB
         */
        private void combineNWBMaps(Map<String, AbstractNWBRoadElement> roadMapNWB,
            Map<String, AbstractNWBRoadElement> laneMapNWB, Map<String, AbstractNWBRoadElement> specialLaneMapNWB) {
            // TODO : Alexander, de combinemaps moet verder worden uitgewerkt!
            // Here, a segment of the NWB wegvak is being extracted (for instance the first part of the wegvak has an on-ramp)
            // We should thus split this NWB-wegvak in two parts, the first part with the on-ramp (additional lane) and the
            // remainder with the original attributes
            for (AbstractNWBRoadElement laneElement : laneMapNWB.values()) {
                NWBDrivingLane lane = (NWBDrivingLane) laneElement;
                NWBRoadElement road = (NWBRoadElement) roadMapNWB.get(lane.getRoadId());
                List<LineString> lineSegmentList = splitRoad(road, lane);
            }
        }

        /**
         * Split a road if there is a lane along a PART of this road.
         * @param road
         * @param segment
         */
        private List<LineString> splitRoad(NWBRoadElement road, NWBDrivingLane segment) {
            MultiLineString lines = (MultiLineString) road.getMyGeom();
            LineString line = (LineString) lines.getGeometryN(0);
            List<LineString> lineSegmentList = new ArrayList<>();
            ;
            // The getSubstring is copied from The JCS Conflation Suite (JCS): I assume it is not supported anymore, but can
            // still be found...
            lineSegmentList.add(SubstringLine.getSubstring(line, segment.getBeginDistance(), segment.getEndDistance()));
            if (segment.getBeginDistance() > 0) {
                lineSegmentList.add(SubstringLine.getSubstring(line, 0, segment.getBeginDistance()));
            }
            if (segment.getEndDistance() < road.getEndDistance()) {
                lineSegmentList.add(SubstringLine.getSubstring(line, 0, segment.getBeginDistance()));
            }
            return lineSegmentList;
        }

        /**
         * Import a list of road (link) elements from a shape file
         * @param initialDir
         * @param fileName
         * @param shapeIdentifier
         * @return
         */
        private Map<String, AbstractNWBRoadElement> getRoadMapNWB(String initialDir, String fileName,
            String shapeIdentifier) {
            FileDataStore dataStoreLink = null;
            try {
                dataStoreLink = newDatastore(initialDir, fileName);
            } catch (IOException e) {
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
         * @param initialDir
         * @param fileName
         * @return
         * @throws IOException
         */
        private FileDataStore newDatastore(String initialDir, final String fileName) throws IOException {
            try {
                URL url = TestShapeParser.class.getResource("/");
                File file = new File(url.getFile() + "../../Data/" + initialDir);
                String fn = file.getCanonicalPath();
                fn = fn.replace('\\', '/');
                File iniDir = new File(fn);
                file = new File(iniDir, fileName + ".shp");

                FileDataStore dataStoreLink = FileDataStoreFinder.getDataStore(file);
                return dataStoreLink;

            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return null;

        }

        /**
         * @param dataStore
         * @return
         */
        private FeatureIterator getFeatureIterator(FileDataStore dataStore) {
            try {
                String[] typeNameLink = dataStore.getTypeNames();
                SimpleFeatureSource sourceLink;
                sourceLink = dataStore.getFeatureSource(typeNameLink[0]);
                SimpleFeatureCollection featuresLink = sourceLink.getFeatures();
                return featuresLink.features();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @param feautureIterator
         * @param shapeIdentifier
         * @return
         */
        private Map<String, AbstractNWBRoadElement> getFeatureAttributes(final FeatureIterator feautureIterator,
            String shapeIdentifier) {
            Map<String, AbstractNWBRoadElement> roadMap = new HashMap<>();
            while (feautureIterator.hasNext()) {
                Feature feature = feautureIterator.next();
                // geometry is always first
                if (shapeIdentifier.equals("NWB_wegvakken")) {
                    NWBRoadElement road = getPropertiesNWB(feature);
                    roadMap.put(road.getRoadId(), road);
                } else if (shapeIdentifier.equals("NWB_rijstroken")) {
                    NWBDrivingLane road = getPropertiesDrivingLanes(feature);
                    roadMap.put(road.getRoadId(), road);
                } else if (shapeIdentifier.equals("NWB_mengstroken")) {
                    NWBDrivingLane road = getPropertiesSpecialLanes(feature);
                    roadMap.put(road.getRoadId(), road);
                }
            }
            return roadMap;
        }

        /**
         * @param feature
         * @return
         */
        private NWBRoadElement getPropertiesNWB(final Feature feature) {
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

            OTSNode startNode = new OTSNode(junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode = new OTSNode(junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            return new NWBRoadElement(theGeom, startNode, endNode, roadId, beginDistance, endDistance, junctionIdBegin,
                junctionIdEnd, adminDirection, drivingDirection, beginKM, endKM);
        }

        /**
         * @param feature
         * @return
         */
        private NWBDrivingLane getPropertiesDrivingLanes(final Feature feature) {
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
            OTSNode startNode = new OTSNode(junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode = new OTSNode(junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            return new NWBDrivingLane(theGeom, startNode, endNode, roadId, beginDistance, endDistance, startNumberOfLanes,
                endNumberOfLanes, sideCode);
        }

        /**
         * @param feature
         * @return
         */
        private NWBDrivingLane getPropertiesSpecialLanes(final Feature feature) {
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
            OTSNode startNode = new OTSNode(junctionIdBegin, new OTSPoint3D(coordinates[0]));
            OTSNode endNode = new OTSNode(junctionIdEnd, new OTSPoint3D(coordinates[coordinates.length - 1]));
            return new NWBDrivingLane(theGeom, startNode, endNode, roadId, beginDistance, endDistance, startNumberOfLanes,
                endNumberOfLanes, sideCode);
        }

        /**
         * @param property
         * @return
         */
        private Double parseDouble(Property property) {
            if (property.getValue() != null) {
                if (property.getValue().toString() != null) {
                    return Double.parseDouble(property.getValue().toString());
                }
            }
            return Double.NaN;
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()

        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString() {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

    }

}
