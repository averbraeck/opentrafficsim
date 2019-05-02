package org.opentrafficsim.web.test;

import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.jetty.sse.OTSWebServer;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TJunctionDemo extends OTSWebServer
{
    /**
     * Create a T-Junction demo.
     * @param title String; the tile for the model
     * @param simulator DEVSRealTimeClock.TimeDouble; the simulator
     * @param model the model
     * @throws Exception on jetty error
     */
    public TJunctionDemo(final String title, final OTSSimulatorInterface simulator, final OTSModelInterface model)
            throws Exception
    {
        super(title, simulator, new Rectangle2D.Double(-200, -200, 400, 400));
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), simulator, new DefaultSwitchableGTUColorer());
    }

    /**
     * Start the demo.
     * @param args args
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSAnimator simulator = new OTSAnimator();
        simulator.setAnimation(false);
        TJunctionModel junctionModel = new TJunctionModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), junctionModel);
        new TJunctionDemo("T-Junction demo", simulator, junctionModel);
    }

    /**
     * The simulation model.
     */
    static class TJunctionModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        TJunctionModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                InputStream stream = URLResource.getResourceAsStream("/xml/TJunction.xml");
                this.network = new OTSRoadNetwork("TJunction", true);
                XmlNetworkLaneParser.build(stream, this.network, getSimulator());

                // add conflicts
                // ((CrossSectionLink) this.network.getLink("SCEC")).setPriority(Priority.STOP);
                // ((CrossSectionLink) this.network.getLink("SCWC")).setPriority(Priority.STOP);
                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                // add trafficlight after
                Lane lane = ((CrossSectionLink) this.network.getLink("ECE")).getLanes().get(0);
                SimpleTrafficLight trafficLight =
                        new SimpleTrafficLight("light", lane, new Length(50.0, LengthUnit.SI), this.simulator);

                try
                {
                    new TrafficLightAnimation(trafficLight, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }
                trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                changePhase(trafficLight);

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Changes color of traffic light.
         * @param trafficLight SimpleTrafficLight; traffic light
         * @throws SimRuntimeException scheduling error
         */
        private void changePhase(final SimpleTrafficLight trafficLight) throws SimRuntimeException
        {
            switch (trafficLight.getTrafficLightColor())
            {
                case RED:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
                    this.simulator.scheduleEventRel(new Duration(30.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] {trafficLight});
                    break;
                }
                case YELLOW:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                    this.simulator.scheduleEventRel(new Duration(56.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] {trafficLight});
                    break;
                }
                case GREEN:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                    this.simulator.scheduleEventRel(new Duration(4.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] {trafficLight});
                    break;
                }
                default:
                {
                    //
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}
