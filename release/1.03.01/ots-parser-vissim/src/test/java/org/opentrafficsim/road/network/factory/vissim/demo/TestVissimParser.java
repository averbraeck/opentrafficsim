package org.opentrafficsim.road.network.factory.vissim.demo;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.vissim.VissimNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * TestVissimParser.java. <br>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TestVissimParser extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestVissimParser(final OTSModelInterface model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
    }

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
                    OTSAnimator simulator = new OTSAnimator();
                    VissimImport model = new VissimImport(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), model);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(model.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, model, DEFAULT_COLORER, model.getNetwork());
                    new TestVissimParser(model, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
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
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class VissimImport extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The network. */
        private OTSRoadNetwork network = new OTSRoadNetwork("test Vissim network", true);

        /**
         * @param simulator the simulator
         */
        VissimImport(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            // OTS network or SmartTraffic??
            String sinkKillClassName;
            String sensorClassName;
            String trafficLightName;
            sinkKillClassName = "org.opentrafficsim.road.network.lane.object.sensor.SinkSensor";
            sensorClassName = "org.opentrafficsim.road.network.lane.object.sensor.SimpleReportingSensor";
            trafficLightName = "org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight";
            ClassLoader classLoader = getClass().getClassLoader();
            URL inputUrl = null;
            try
            {
                inputUrl = new URL(classLoader.getResource("ehv_eisen1_VA.inpx").toString());
            }
            catch (MalformedURLException e1)
            {
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
                this.network =
                        nlp.build(inputUrl, outputFile, this.network, sinkKillClassName, sensorClassName, trafficLightName);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
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
