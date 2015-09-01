package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.opentrafficsim.core.OTS_DIST;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.lane.LaneType;

/**
 * TemplateGTUType stores most of the information that is needed to generate a GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TemplateGTUType implements Serializable, OTS_SCALAR, OTS_DIST
{
    /** */
    private static final long serialVersionUID = 20141230L;

    /** The type of the GTU. */
    private final GTUType gtuType;

    /** distribution of the length of the GTU. */
    private final LengthContinuousDist.Rel lengthDist;

    /** distribution of the width of the GTU. */
    private final LengthContinuousDist.Rel widthDist;

    /** distribution of the maximum speed of the GTU. */
    private final SpeedContinuousDist.Abs maximumSpeedDist;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param lengthDist the length of the GTU type (parallel with driving direction).
     * @param widthDist the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedDist the maximum speed of the GTU type (in the driving direction).
     * @param simulator the simulator.
     * @throws GTUException when GTUType defined more than once
     */
    public TemplateGTUType(final String id, final LengthContinuousDist.Rel lengthDist,
        final LengthContinuousDist.Rel widthDist,
        final SpeedContinuousDist.Abs maximumSpeedDist, final OTSDEVSSimulatorInterface simulator)
        throws GTUException
    {
        this.gtuType = GTUType.makeGTUType(id);
        this.lengthDist = lengthDist;
        this.widthDist = widthDist;
        this.maximumSpeedDist = maximumSpeedDist;
        this.simulator = simulator;
    }

    /**
     * @param laneType lane type to look for compatibility.
     * @return whether the GTUType is compatible with the lane type.
     */
    public final boolean isCompatible(final LaneType laneType)
    {
        return laneType.isCompatible(this.getGtuType());
    }

    /**
     * @return length.
     */
    public final Length.Rel getLength()
    {
        return this.lengthDist.draw();
    }

    /**
     * @return width.
     */
    public final Length.Rel getWidth()
    {
        return this.widthDist.draw();
    }

    /**
     * @return maximumVelocity.
     */
    public final Speed.Abs getMaximumVelocity()
    {
        return this.maximumSpeedDist.draw();
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return gtuType.
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }
}
