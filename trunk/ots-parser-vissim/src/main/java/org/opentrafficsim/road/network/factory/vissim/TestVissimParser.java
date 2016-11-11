package org.opentrafficsim.road.network.factory.vissim;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opengis.feature.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

public class TestVissimParser extends AbstractWrappableAnimation {

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
                    TestVissimParser xmlModel = new TestVissimParser();
                    // 1 hour simulation run for testing
                    xmlModel.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND), new Duration(
                        60.0, TimeUnit.MINUTE), new ArrayList<org.opentrafficsim.base.modelproperties.Property<?>>(), null,
                        true);
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
    protected final JPanel makeCharts(final SimpleSimulatorInterface simulator) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) {
        return new VissimImport();
    }

    /** {@inheritDoc} */
    @Override
    protected final java.awt.geom.Rectangle2D.Double makeAnimationRectangle() {
        // return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
        return new Rectangle2D.Double(162000, 384500, 2000, 2000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "TestVissimParser []";
    }

    /**
     * Model to test the Vissim File Format parser.
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
    class VissimImport implements OTSModelInterface {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** The network. */
        private OTSNetwork network = new OTSNetwork("test Vissim network");

        /** {@inheritDoc} */
        @Override
        public final void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException {

            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

            ClassLoader classLoader = getClass().getClassLoader();

            File inputFile = new File(classLoader.getResource("ehv_eisen1_VA.inpx").getFile());
            URL inputUrl = null;
            try {
                inputUrl = new URL(classLoader.getResource("ehv_eisen1_VA.inpx").toString());
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            File outputFile = new File(classLoader.getResource("testEindhoven1.xml").getFile());

            // URL inputUrl = URLResource.getResource(
            // "C:/Projecten/OTS/ots-parser-vissim/src/main/resources/ehv_eisen1_VA.inpx");

            // URL url = URLResource.getResource("C:/Projecten/OTS/ots-parser-vissim/src/main/resources/Test-netwerk-12.inpx");
            // URL url =
            // URLResource.getResource("C:/Projecten/OTS/ots-parser-vissim/src/main/resources/OTS-Tester-Vissimnetwerk.inpx");
            VissimNetworkLaneParser nlp = new VissimNetworkLaneParser(this.simulator);

            try {
                this.network = nlp.build(inputUrl, outputFile, network);
            } catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                | GTUException | OTSGeometryException exception) {
                exception.printStackTrace();
            }

        }

        /**
         * @param property
         * @return a double
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
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString() {
            return "TestVissimParser [simulator=" + this.simulator + "]";
        }

    }

}
