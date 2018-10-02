package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo extends AbstractWrappableAnimation
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
                    TrafCODDemo model = new TrafCODDemo();
                    // 1 hour simulation run for testing
                    model.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE),
                            new ArrayList<Property<?>>(), null, true);
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

    /** The TrafCOD controller. */
    private TrafCOD trafCOD;

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TrafCOD demonstration 1";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TrafCOD demonstration";
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final OTSSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        JScrollPane scrollPane = new JScrollPane(TrafCODDemo.this.controllerDisplayPanel);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane);
        addTab(getTabCount() - 1, this.trafCOD.getId(), wrapper);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel() throws OTSSimulationException
    {
        return new TrafCODModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /**
     * The simulation model.
     */
    class TrafCODModel extends EventProducer implements OTSModelInterface, EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** the model. */
        private OTSNetwork network;

        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> theSimulator)
                throws SimRuntimeException
        {
            try
            {
                URL url = URLResource.getResource("/TrafCODDemo1/TrafCODDemo1.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSSimulatorInterface) theSimulator);
                this.network = nlp.build(url, true);

                Lane laneNX = (Lane) ((CrossSectionLink) this.network.getLink("N", "X")).getCrossSectionElement("FORWARD");
                Lane laneWX = (Lane) ((CrossSectionLink) this.network.getLink("W", "X")).getCrossSectionElement("FORWARD");
                Set<TrafficLight> trafficLights = new HashSet<>();
                trafficLights.add(new SimpleTrafficLight("TL08", laneWX, new Length(296, LengthUnit.METER),
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator));
                trafficLights.add(new SimpleTrafficLight("TL11", laneNX, new Length(296, LengthUnit.METER),
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator));
                Set<TrafficLightSensor> sensors = new HashSet<>();
                sensors.add(new TrafficLightSensor("D081", laneWX, new Length(292, LengthUnit.METER), laneWX,
                        new Length(294, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator, Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D082", laneWX, new Length(260, LengthUnit.METER), laneWX,
                        new Length(285, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator, Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D111", laneNX, new Length(292, LengthUnit.METER), laneNX,
                        new Length(294, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator, Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D112", laneNX, new Length(260, LengthUnit.METER), laneNX,
                        new Length(285, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR,
                        (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator, Compatible.EVERYTHING));
                String controllerName = "Simple TrafCOD controller";
                TrafCODDemo.this.trafCOD = new TrafCOD(controllerName, URLResource.getResource("/TrafCODDemo1/simpleTest.tfc"),
                        trafficLights, sensors, (DEVSSimulator<Time, Duration, SimTimeDoubleUnit>) theSimulator,
                        TrafCODDemo.this.controllerDisplayPanel);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                TrafCODDemo.this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                // Subscribe the TrafCOD machine to trace command events that we emit
                addListener(TrafCODDemo.this.trafCOD, TrafficController.TRAFFICCONTROL_SET_TRACING);
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 8, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "XR1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TD1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TL", 11, true});
                // System.out.println("demo: emitting a SET TRACING event for all variables related to stream 11");
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] { controllerName, "", 11, true });

                // TrafCODDemo.this.trafCOD.traceVariablesOfStream(TrafficController.NO_STREAM, true);
                // TrafCODDemo.this.trafCOD.traceVariablesOfStream(11, true);
                // TrafCODDemo.this.trafCOD.traceVariable("MRV", 11, true);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return TrafCODDemo.this.trafCOD.getSimulator();
        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            EventType type = event.getType();
            Object[] payload = (Object[]) event.getContent();
            if (TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING.equals(type))
            {
                // System.out.println("Evalution starts at " + getSimulator().getSimulatorTime());
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
