package org.opentrafficsim.road.network.factory.shape;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

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
        return new TestGisImport();
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle() {
        return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "TestXMLParser []";
    }

    /**
     * Model to test the XML parser.
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
    class TestGisImport implements OTSModelInterface {
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
            URL url = URLResource.getResource("/shapeExample/circuit.shp");
            // URL url = URLResource.getResource("C:/data/places.shp");
            File file = new File(url.getFile());
            Map<String, Object> map = new HashMap();
            try {
                map.put("url", file.toURI().toURL());
                DataStore dataStoreLink = DataStoreFinder.getDataStore(map);
                FileDataStore dataStoreLink1 = FileDataStoreFinder.getDataStore(file);
                FileDataStoreFactorySpi dataStoreLink2 = FileDataStoreFinder.getDataStoreFactory(file.getParent());
                // open and read shape file links
                String[] typeNameLink = dataStoreLink.getTypeNames();

                SimpleFeatureSource sourceLink = dataStoreLink.getFeatureSource(typeNameLink[0]);

                SimpleFeatureCollection featuresLink = sourceLink.getFeatures();

                FeatureIterator iteratorLink = featuresLink.features();

                int linkID = 0;
                while (iteratorLink.hasNext())

                {
                    Feature feature = iteratorLink.next();
                    Geometry test = (Geometry) feature.getDefaultGeometryProperty().getValue();
                    Coordinate[] coords = test.getCoordinates();
                    // ArrayList<Vertex> pointList = new ArrayList<Vertex>();
                    // ArrayList<Vertex> pointListBA = new ArrayList<Vertex>();
                    int fromNodeID = -1;
                    int toNodeID = -1;
                    double length = -1;
                    double capacity = -1;
                    String turnLanes = "";
                    double exitLanes = -1;
                    double maxSpeed = -1;
                    int lanes = -1;
                    double capacityBA = -1;
                    String turnLanesBA = "";
                    double exitLanesBA = -1;
                    double maxSpeedBA = -1;
                    int direction = -1;
                    int lanesBA = -1;
                    String typologyName = "road";
                    String linkType = "ANODE";
                    Property property = null;
                    property = feature.getProperty(linkType);
                    boolean voedingsLinkAB = false;
                    boolean voedingsLinkBA = false;
                    int node;
                    node = Integer.parseInt(property.getValue().toString());
                    if (node < 20) {
                        voedingsLinkAB = true;
                        voedingsLinkBA = true;
                    }

                    /*
                     * linkType = "LINKTYPEBA"; property = feature.getProperty(linkType); if
                     * (property.getValue().toString().equals("Voedingslink")) { voedingsLinkBA = true; }
                     */
                    // if (!propertyDirection.equals("")) {
                    // property = feature.getProperty(propertyDirection);
                    // direction = Integer.parseInt(property.getValue().toString());
                    // }
                    //
                    // if (!propertyFromNode.equals("")) {
                    // property = feature.getProperty(propertyFromNode);
                    // fromNodeID = Integer.parseInt(property.getValue().toString());
                    // }
                    // if (!propertyToNode.equals("")) {
                    // property = feature.getProperty(propertyToNode);
                    // toNodeID = Integer.parseInt(property.getValue().toString());
                    // }
                    // if (direction == valueDirectionAB || direction == valueDirectionABBA) {
                    // for (int i = 1; i < coords.length - 1; i++) {
                    // Vertex v = new Vertex(coords[i]);
                    // pointList.add(v);
                    // }
                    //
                    // }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            }
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
