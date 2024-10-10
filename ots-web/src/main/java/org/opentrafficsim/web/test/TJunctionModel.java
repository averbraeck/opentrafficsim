package org.opentrafficsim.web.test;

import java.net.URL;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * TJunctionModel.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TJunctionModel extends AbstractOtsModel
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /** The network. */
    private RoadNetwork network;

    /**
     * @param simulator the simulator for this model
     */
    public TJunctionModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            URL xmlURL = URLResource.getResource("/resources/xml/TJunction.xml");
            this.network = new RoadNetwork("TJunction", getSimulator());
            new XmlParser(this.network).setUrl(xmlURL).build();

            // add conflicts
            // ((CrossSectionLink) this.network.getLink("SCEC")).setPriority(Priority.STOP);
            // ((CrossSectionLink) this.network.getLink("SCWC")).setPriority(Priority.STOP);
            ConflictBuilder.buildConflicts(this.network, this.simulator,
                    new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

            // add trafficlight after
            Lane lane = ((CrossSectionLink) this.network.getLink("ECE")).getLanes().get(0);
            TrafficLight trafficLight = new TrafficLight("light", lane, new Length(50.0, LengthUnit.SI));
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
     * @param trafficLight traffic light
     * @throws SimRuntimeException scheduling error
     */
    private void changePhase(final TrafficLight trafficLight) throws SimRuntimeException
    {
        switch (trafficLight.getTrafficLightColor())
        {
            case RED:
            {
                trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
                this.simulator.scheduleEventRel(new Duration(30.0, DurationUnit.SECOND), this, "changePhase",
                        new Object[] {trafficLight});
                break;
            }
            case YELLOW:
            {
                trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                this.simulator.scheduleEventRel(new Duration(56.0, DurationUnit.SECOND), this, "changePhase",
                        new Object[] {trafficLight});
                break;
            }
            case GREEN:
            {
                trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                this.simulator.scheduleEventRel(new Duration(4.0, DurationUnit.SECOND), this, "changePhase",
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
    public RoadNetwork getNetwork()
    {
        return this.network;
    }
}
