package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.AbstractPerception;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * The perception module of a GTU based on lanes. It is responsible for perceiving (sensing) the environment of the GTU, which
 * includes the locations of other GTUs. Perception is done at a certain time, and the perceived information might have a
 * limited validity. In that sense, Perception is stateful. Information can be requested as often as needed, but will only be
 * recalculated when asked explicitly. This abstract class provides the building blocks for lane-based perception. <br>
 * Perception for lane-based GTUs involves information about GTUs in front of the owner GTU on the same lane (the 'leader' GTU),
 * parallel vehicles (important if we want to change lanes), distance to other vehicles on parallel lanes, as well in front as
 * to the back (important if we want to change lanes), and information about obstacles, traffic lights, speed signs, and ending
 * lanes.
 * <p>
 * This class allows {@code PerceptionCategory}s that are either eager or lazy. All categories will have the {@code updateAll}
 * method invoked prior to an operational plan being determined. Categories may ignore this and instead evaluate results only
 * when the tactical planner requests them.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractLanePerception extends AbstractPerception<LaneBasedGtu> implements LanePerception
{

    /** */
    private static final long serialVersionUID = 20151128L;

    /** Perception parameter type. */
    protected static final ParameterTypeLength PERCEPTION = ParameterTypes.PERCEPTION;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Look back parameter type. */
    protected static final ParameterTypeLength LOOKBACK = ParameterTypes.LOOKBACK;

    /** Lane structure to perform the perception with. */
    private LaneStructure laneStructure = null;

    /** Most recent update time of lane structure. */
    private Time updateTime = null;

    /** Mental module. */
    private Mental mental;

    /**
     * Create a new LanePerception module without mental module.
     * @param gtu LaneBasedGtu; GTU
     */
    public AbstractLanePerception(final LaneBasedGtu gtu)
    {
        super(gtu);
        this.mental = null;
    }

    /**
     * Create a new LanePerception module with mental module.
     * @param gtu LaneBasedGtu; GTU
     * @param mental Mental; mental module
     */
    public AbstractLanePerception(final LaneBasedGtu gtu, final Mental mental)
    {
        super(gtu);
        this.mental = mental;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneStructure getLaneStructure() throws ParameterException
    {

        if (this.laneStructure == null || this.updateTime.lt(getGtu().getSimulator().getSimulatorAbsTime()))
        {
            if (this.laneStructure == null)
            {
                // downstream structure length
                Length down = getGtu().getParameters().getParameter(PERCEPTION);
                // upstream structure length
                Length up = getGtu().getParameters().getParameter(LOOKBACK);
                // structure length downstream of split on link not on route
                Length lookAhead = getGtu().getParameters().getParameter(LOOKAHEAD);
                // structure length upstream of merge on link not on route
                Length upMerge = Length.max(up, lookAhead);
                // negative values for upstream
                up = up.neg();
                upMerge = upMerge.neg();
                this.laneStructure = new RollingLaneStructure(lookAhead, down, up, lookAhead, upMerge, getGtu());
            }
            LanePosition dlp;
            try
            {
                dlp = getGtu().getReferencePosition();
                this.laneStructure.update(dlp, getGtu().getStrategicalPlanner().getRoute(), getGtu().getType());
            }
            catch (GtuException exception)
            {
                exception.printStackTrace();
                throw new RuntimeException("Error while updating the lane map.", exception);
            }
            this.updateTime = getGtu().getSimulator().getSimulatorAbsTime();
        }
        return this.laneStructure;
    }

    /** {@inheritDoc} */
    @Override
    public Mental getMental()
    {
        return this.mental;
    }

    /** {@inheritDoc} */
    @Override
    public void perceive() throws GtuException, NetworkException, ParameterException
    {
        if (this.mental != null)
        {
            this.mental.apply(this);
        }
        super.perceive();
    }

}
