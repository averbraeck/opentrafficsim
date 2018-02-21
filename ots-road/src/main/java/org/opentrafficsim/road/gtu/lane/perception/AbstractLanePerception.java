package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.AbstractPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLanePerception extends AbstractPerception<LaneBasedGTU> implements LanePerception
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

    /**
     * Create a new LanePerception module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     * @param gtu GTU
     */
    public AbstractLanePerception(final LaneBasedGTU gtu)
    {
        super(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneStructure getLaneStructure() throws ParameterException
    {

        if (this.laneStructure == null || this.updateTime.lt(getGtu().getSimulator().getSimulatorTime().getTime()))
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
                this.laneStructure = new LaneStructure(lookAhead, down, up, lookAhead, upMerge, getGtu().getSimulator());
            }
            DirectedLanePosition dlp;
            try
            {
                dlp = getGtu().getReferencePosition();
                this.laneStructure.update(dlp, getGtu().getStrategicalPlanner().getRoute(), getGtu().getGTUType());
            }
            catch (GTUException exception)
            {
                throw new RuntimeException("Error while updating the lane map.", exception);
            }
            this.updateTime = getGtu().getSimulator().getSimulatorTime().getTime();
        }
        return this.laneStructure;
    }

}
