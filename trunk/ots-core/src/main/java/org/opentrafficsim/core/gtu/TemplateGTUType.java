package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <ID> the ID-type of the GTU, e.g. String or a certain Enum type.
 */
public class TemplateGTUType<ID>
{
    /** */
    private static final long serialVersionUID = 20141230L;
    
    /** The type of the GTU. */
    private final GTUType<ID> gtuType;

    /** distribution of the length of the GTU. */
    private final DistContinuousDoubleScalar.Rel<LengthUnit> lengthDist;

    /** distribution of the width of the GTU. */
    private final DistContinuousDoubleScalar.Rel<LengthUnit> widthDist;

    /** distribution of the maximum speed of the GTU. */
    private final DistContinuousDoubleScalar.Abs<SpeedUnit> maximumSpeedDist;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param lengthDist the length of the GTU type (parallel with driving direction).
     * @param widthDist the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedDist the maximum speed of the GTU type (in the driving direction).
     * @param simulator the simulator.
     */
    public TemplateGTUType(final ID id, final DistContinuousDoubleScalar.Rel<LengthUnit> lengthDist,
        final DistContinuousDoubleScalar.Rel<LengthUnit> widthDist,
        final DistContinuousDoubleScalar.Abs<SpeedUnit> maximumSpeedDist, final OTSDEVSSimulatorInterface simulator)
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
    public final boolean isCompatible(final LaneType<?> laneType)
    {
        return laneType.isCompatible(this.getGtuType());
    }

    /**
     * @return length.
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.lengthDist.draw();
    }

    /**
     * @return width.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return this.widthDist.draw();
    }

    /**
     * @return maximumVelocity.
     */
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
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
    public final GTUType<ID> getGtuType()
    {
        return this.gtuType;
    }
}
