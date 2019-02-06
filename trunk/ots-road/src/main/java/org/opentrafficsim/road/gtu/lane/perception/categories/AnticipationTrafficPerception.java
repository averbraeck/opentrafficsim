package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationSpeed.SpeedSet;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;

/**
 * Traffic perception using neighbors perception. Speed is anticipated as in the LMRS. Density is simply distance of the
 * farthest leader divided by the number of leaders.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AnticipationTrafficPerception extends LaneBasedAbstractPerceptionCategory implements TrafficPerception
{

    /** */
    private static final long serialVersionUID = 20180313L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Last time speed was updated. */
    private Time lastSpeedTime = null;

    /** Anticipated speed by vehicles in the left lane. */
    private Map<RelativeLane, Double> antFromLeft = new HashMap<>();

    /** Anticipated speed by vehicles in the lane. */
    private Map<RelativeLane, Double> antInLane = new HashMap<>();

    /** Anticipated speed by vehicles in the right lane. */
    private Map<RelativeLane, Double> antFromRight = new HashMap<>();

    /** Anticipated speed combined. */
    private Map<RelativeLane, Speed> speed = new HashMap<>();

    /** Anticipated density combined. */
    private Map<RelativeLane, TimeStampedObject<LinearDensity>> density = new HashMap<>();

    /** Density collector. */
    private static final AnticipationDensity DENSITY = new AnticipationDensity();

    /**
     * Constructor.
     * @param perception LanePerception; perception
     */
    public AnticipationTrafficPerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeed(final RelativeLane lane) throws ParameterException
    {
        Time now = Try.assign(() -> getTimestamp(), "");
        if (this.lastSpeedTime == null || this.lastSpeedTime.si < now.si)
        {
            // due to lane interdependency, we clear all
            this.antFromLeft.clear();
            this.antInLane.clear();
            this.antFromRight.clear();
            this.speed.clear();
            this.lastSpeedTime = now;
        }
        Speed vAnt = this.speed.get(lane);
        if (vAnt == null)
        {
            LaneBasedGTU gtu = Try.assign(() -> getPerception().getGtu(), "");
            Speed desiredSpeed = gtu.getDesiredSpeed();
            vAnt = anticipationSpeed(lane, gtu.getParameters(),
                    getPerception().getPerceptionCategoryOrNull(NeighborsPerception.class),
                    getPerception().getPerceptionCategoryOrNull(InfrastructurePerception.class), desiredSpeed);
            this.speed.put(lane, vAnt);
        }
        return vAnt;
    }

    /**
     * Returns the anticipation speed in a lane. GTU's in adjacent lanes with their indicator towards the given lane are
     * included in the evaluation.
     * @param lane RelativeLane; lane to assess
     * @param params Parameters; parameters
     * @param neighbors NeighborsPerception; neighbors perception
     * @param infra InfrastructurePerception; infrastructure perception
     * @param desiredSpeed Speed; desired speed
     * @return Speed; anticipation speed in lane
     * @throws ParameterException on missing parameter
     */
    private Speed anticipationSpeed(final RelativeLane lane, final Parameters params, final NeighborsPerception neighbors,
            final InfrastructurePerception infra, final Speed desiredSpeed) throws ParameterException
    {
        if (!this.antInLane.containsKey(lane))
        {
            anticipateSpeedFromLane(lane, params, neighbors, desiredSpeed);
        }
        double v = this.antInLane.get(lane);
        if (infra.getCrossSection().contains(lane.getLeft()))
        {
            if (!this.antFromLeft.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getLeft(), params, neighbors, desiredSpeed);
            }
            double fromLeft = this.antFromLeft.get(lane);
            v = v < fromLeft ? v : fromLeft;
        }
        if (infra.getCrossSection().contains(lane.getRight()))
        {
            if (!this.antFromRight.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getRight(), params, neighbors, desiredSpeed);
            }
            double fromRight = this.antFromRight.get(lane);
            v = v < fromRight ? v : fromRight;
        }
        return Speed.createSI(v);
    }

    /**
     * Anticipate speed from the GTUs in one lane. This affects up to 3 lanes, all this information is stored.
     * @param lane RelativeLane; lane to assess
     * @param params Parameters; parameters
     * @param neighbors NeighborsPerception; neighbors perception
     * @param desiredSpeed Speed; desired speed
     * @throws ParameterException on missing parameter
     */
    private void anticipateSpeedFromLane(final RelativeLane lane, final Parameters params, final NeighborsPerception neighbors,
            final Speed desiredSpeed) throws ParameterException
    {
        AnticipationSpeed anticipationSpeed = new AnticipationSpeed(desiredSpeed, params.getParameter(LOOKAHEAD), lane);
        SpeedSet speedSet = neighbors.getLeaders(lane).collect(anticipationSpeed);
        this.antFromLeft.put(lane.getRight(), speedSet.getRight().si);
        this.antInLane.put(lane, speedSet.getCurrent().si);
        this.antFromRight.put(lane.getLeft(), speedSet.getLeft().si);
    }

    /** {@inheritDoc} */
    @Override
    public LinearDensity getDensity(final RelativeLane lane)
    {
        Time now = Try.assign(() -> getTimestamp(), "");
        TimeStampedObject<LinearDensity> tK = this.density.get(lane);
        if (tK == null || tK.getTimestamp().si < now.si)
        {
            LinearDensity k =
                    getPerception().getPerceptionCategoryOrNull(NeighborsPerception.class).getLeaders(lane).collect(DENSITY);
            this.density.put(lane, new TimeStampedObject<>(k, now));
            return k;
        }
        else
        {
            return tK.getObject();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        // lazy evaluation
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AnticipationTrafficPerception [lastSpeedTime=" + this.lastSpeedTime + ", speed=" + this.speed + ", density="
                + this.density + "]";
    }

}
