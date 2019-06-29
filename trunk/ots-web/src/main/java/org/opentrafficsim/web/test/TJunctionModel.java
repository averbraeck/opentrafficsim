package org.opentrafficsim.web.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * TJunctionModel.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TJunctionModel extends AbstractOTSModel
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /** The network. */
    private OTSRoadNetwork network;

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public TJunctionModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            URL xmlURL = URLResource.getResource("/xml/TJunction.xml");
            this.network = new OTSRoadNetwork("TJunction", true);
            XmlNetworkLaneParser.build(xmlURL, this.network, getSimulator());

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
