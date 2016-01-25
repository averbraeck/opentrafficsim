package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <br />
 * Copyright (c) 2013-2014 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br />
 * Some parts of the software (c) 2011-2014 TU Delft, Faculty of TBM, Systems & Simulation <br />
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br />
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 * @version SVN $Revision: 31 $ $Author: averbraeck $
 * @date $Date: 2011-08-15 04:38:04 +0200 (Mon, 15 Aug 2011) $
 **/
public class RTICars extends LaneBasedIndividualCar
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    DirectedPoint current = new DirectedPoint(0, 0, 0, 0, 0, 0);

    /**
     * @param valueOf
     * @param carType
     * @param lanepositionSet
     * @param speed
     * @param carLength
     * @param draw
     * @param draw2
     * @param simulator
     * @param sPlanner
     * @param perception
     * @param network
     * @throws GTUException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws NamingException
     * @throws OperationalPlanException
     * @throws OTSGeometryException
     */
    public RTICars(String valueOf, GTUType carType, Set<DirectedLanePosition> lanepositionSet, Speed speed,
        Rel carLength, Rel draw, Speed draw2, OTSDEVSSimulatorInterface simulator,
        LaneBasedStrategicalPlanner sPlanner, LanePerceptionFull perception, final OTSNetwork network)
        throws NamingException, NetworkException, SimRuntimeException, GTUException, OperationalPlanException,
        OTSGeometryException
    {
        super(valueOf, carType, lanepositionSet, speed, carLength, draw, draw2, simulator, sPlanner, perception,
            network);

        this.current = this.getOperationalPlan().getLocation(simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        double x = this.current.x + (0.01 * (Math.cos(this.current.getRotZ())));
        double y = this.current.y + (0.01 * (Math.sin(this.current.getRotZ())));

        this.current.setX(x);
        this.current.setY(y);

        return this.current;
    }
}
