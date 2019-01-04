package org.opentrafficsim.road.network.factory.vissim.demo;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opengis.feature.Property;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.vissim.VissimNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractOTSSwingApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

public class TestVissimParser extends AbstractOTSSwingApplication
{

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TestVissimParser xmlModel = new TestVissimParser();
                    // 1 hour simulation run for testing
                    xmlModel.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE),
                            new ArrayList<org.opentrafficsim.base.modelproperties.InputParameter<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | InputParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final OTSSimulatorInterface simulator)
    {
        return;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
    {
        return new VissimImport();
    }

    /** {@inheritDoc} */
    @Override
    protected final java.awt.geom.Rectangle2D.Double makeAnimationRectangle()
    {
        // return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
        return new Rectangle2D.Double(162000, 384500, 2000, 2000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestVissimParser []";
    }

    /**
     * Model to test the Vissim File Format parser.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
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
    class VissimImport implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSSimulatorInterface simulator;

        /** The network. */
        private OTSNetwork network = new OTSNetwork("test Vissim network");

        /** {@inheritDoc} */
        @Override
        public final void constructModel()
                throws SimRuntimeException
        {

            // OTS network or SmartTraffic??
            boolean OpenTrafficSim = false;
            String sinkKillClassName;
            String sensorClassName;
            String trafficLightName;
            if (OpenTrafficSim)
            {
                sinkKillClassName = "org.opentrafficsim.road.network.lane.object.sensor.SinkSensor";
                sensorClassName = "org.opentrafficsim.road.network.lane.object.sensor.SimpleReportingSensor";
                trafficLightName = "org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight";
            }
            else
            {
                sinkKillClassName = "nl.grontmij.smarttraffic.model.KillSensor";
                sensorClassName = "nl.grontmij.smarttraffic.model.CheckSensor";
                trafficLightName = "org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight";
            }
            this.simulator = (OTSSimulatorInterface) pSimulator;
            ClassLoader classLoader = getClass().getClassLoader();
            URL inputUrl = null;
            try
            {
                inputUrl = new URL(classLoader.getResource("ehv_eisen1_VA.inpx").toString());
            }
            catch (MalformedURLException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            String path = classLoader.getResource("").getPath().toString();
            File outputFile = new File(path, "/testEindhoven.xml");
            try
            {
                outputFile.createNewFile();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            VissimNetworkLaneParser nlp = new VissimNetworkLaneParser(this.simulator);

            try
            {
                this.network = nlp.build(inputUrl, outputFile, network, sinkKillClassName, sensorClassName, trafficLightName);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

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
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return this.simulator;
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
            return "TestVissimParser [simulator=" + this.simulator + "]";
        }

    }

}
