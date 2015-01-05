package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
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
public class TemplateGTUType<ID> extends GTUType<ID>
{
    /** */
    private static final long serialVersionUID = 20141230L;

    /** the length of the GTU (parallel with driving direction). */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** the width of the GTU (perpendicular to driving direction). */
    private final DoubleScalar.Rel<LengthUnit> width;

    /** the maximum speed of the GTU (in the driving direction). */
    private final DoubleScalar.Abs<SpeedUnit> maximumVelocity;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param length the length of the GTU type (parallel with driving direction).
     * @param width the width of the GTU type (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU type (in the driving direction).
     * @param simulator the simulator.
     */
    public TemplateGTUType(final ID id, final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Rel<LengthUnit> width,
        final DoubleScalar.Abs<SpeedUnit> maximumVelocity, final OTSDEVSSimulatorInterface simulator)
    {
        super(id);
        this.length = length;
        this.width = width;
        this.maximumVelocity = maximumVelocity;
        this.simulator = simulator;
    }

    /**
     * @param laneType lane type to look for compatibility.
     * @return whether the GTUType is compatible with the lane type.
     */
    public final boolean isCompatible(final LaneType<?> laneType)
    {
        return laneType.isCompatible(this);
    }

    /**
     * @return length.
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /**
     * @return width.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return this.width;
    }

    /**
     * @return maximumVelocity.
     */
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
    {
        return this.maximumVelocity;
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }
}
