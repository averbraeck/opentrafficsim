package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.io.URLResource;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 06, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo2 extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20161118L;

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
                    TrafCODDemo2 model = new TrafCODDemo2();
                    // 1 hour simulation run for testing
                    model.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND), new Duration(60.0,
                            TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** TrafCOD controller display. */
    private JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TrafCOD demonstration 2";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TrafCOD demonstration";
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        JScrollPane scrollPane = new JScrollPane(TrafCODDemo2.this.controllerDisplayPanel);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane);
        addTab(getTabCount() - 1, "TrafCOD display", wrapper);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new TrafCODModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-110, -110, 220, 220);
    }

    /**
     * The simulation model.
     */
    class TrafCODModel extends EventProducer implements OTSModelInterface, EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** The TrafCOD evaluator. */
        private TrafCOD trafCOD;

        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            try
            {
                URL url = URLResource.getResource("/TrafCODDemo2/Network.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) theSimulator);
                OTSNetwork network = nlp.build(url);
                Length sinkPosition = new Length(10, LengthUnit.METER); // These lanes have reverse direction
                for (int laneNumber = 1; laneNumber <= 3; laneNumber++)
                {
                    for (String direction : new String[] { "N", "E", "S", "W" })
                    {
                        Lane l =
                                (Lane) ((CrossSectionLink) network.getLink(direction, direction + "C"))
                                        .getCrossSectionElement("REVERSE" + laneNumber);
                        new SinkSensor(l, sinkPosition, (OTSDEVSSimulatorInterface) theSimulator);
                    }
                }
                /*-
                Lane laneNX = (Lane) ((CrossSectionLink) network.getLink("N", "X")).getCrossSectionElement("FORWARD");
                Lane laneWX = (Lane) ((CrossSectionLink) network.getLink("W", "X")).getCrossSectionElement("FORWARD");
                Lane laneXE = (Lane) ((CrossSectionLink) network.getLink("X", "E")).getCrossSectionElement("FORWARD");
                new SinkSensor(laneXE, new Length(90, LengthUnit.METER), (OTSDEVSSimulatorInterface) theSimulator);
                Set<TrafficLight> trafficLights = new HashSet<>();
                trafficLights.add(new SimpleTrafficLight("TL08", laneWX, new Length(296, LengthUnit.METER),
                        (OTSDEVSSimulatorInterface) theSimulator));
                trafficLights.add(new SimpleTrafficLight("TL11", laneNX, new Length(296, LengthUnit.METER),
                        (OTSDEVSSimulatorInterface) theSimulator));
                Set<TrafficLightSensor> sensors = new HashSet<>();
                sensors.add(new TrafficLightSensor("D081", laneWX, new Length(292, LengthUnit.METER), laneWX, new Length(294,
                        LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (OTSDEVSSimulatorInterface) theSimulator));
                sensors.add(new TrafficLightSensor("D082", laneWX, new Length(260, LengthUnit.METER), laneWX, new Length(285,
                        LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (OTSDEVSSimulatorInterface) theSimulator));
                sensors.add(new TrafficLightSensor("D111", laneNX, new Length(292, LengthUnit.METER), laneNX, new Length(294,
                        LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (OTSDEVSSimulatorInterface) theSimulator));
                sensors.add(new TrafficLightSensor("D112", laneNX, new Length(260, LengthUnit.METER), laneNX, new Length(285,
                        LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (OTSDEVSSimulatorInterface) theSimulator));
                String controllerName = "Simple TrafCOD controller";
                // this.trafCOD =
                // new TrafCOD(controllerName, "file:///d:/cppb/trafcod/otsim/simpleTest.tfc", trafficLights, sensors,
                // (DEVSSimulator<Time, Duration, OTSSimTimeDouble>) theSimulator,
                // TrafCODDemo.this.controllerDisplayPanel);
                this.trafCOD =
                        new TrafCOD(controllerName, URLResource.getResource("/TrafCODDemo1/simpleTest.tfc"),
                                trafficLights, sensors,
                                (DEVSSimulator<Time, Duration, OTSSimTimeDouble>) theSimulator,
                                TrafCODDemo2.this.controllerDisplayPanel);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                // Subscribe the TrafCOD machine to trace command events that we emit
                addListener(this.trafCOD, TrafficController.TRAFFICCONTROL_SET_TRACING);
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 8, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "XR1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TD1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TL", 11, true});
                // System.out.println("demo: emitting a SET TRACING event for all variables related to stream 11");
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] { controllerName, "", 11, true });

                // this.trafCOD.traceVariablesOfStream(TrafficController.NO_STREAM, true);
                // this.trafCOD.traceVariablesOfStream(11, true);
                // this.trafCOD.traceVariable("MRV", 11, true);
                 */
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.trafCOD.getSimulator();
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            EventType type = event.getType();
            Object[] payload = (Object[]) event.getContent();
            if (TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING.equals(type))
            {
                // System.out.println("Evalution starts at " + getSimulator().getSimulatorTime().getTime());
                return;
            }
            else if (TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED.equals(type))
            {
                System.out.println("Conflict group changed from " + ((String) payload[1]) + " to " + ((String) payload[2]));
            }
            else if (TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED.equals(type))
            {
                System.out.println(String.format("Variable changed %s <- %d   %s", payload[1], payload[4], payload[5]));
            }
            else if (TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING.equals(type))
            {
                System.out.println("Warning " + payload[1]);
            }
            else
            {
                System.out.print("TrafCODDemo received event of type " + event.getType() + ", payload [");
                String separator = "";
                for (Object o : payload)
                {
                    System.out.print(separator + o);
                    separator = ",";
                }
                System.out.println("]");
            }
        }

    }

}
