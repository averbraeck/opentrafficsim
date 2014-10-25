package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractGTU<ID> implements GTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** the id of the GTU, could be String or Integer. */
    private final ID id;

    /** the type of GTU, e.g. TruckType, CarType, BusType. */
    private final GTUType<?> gtuType;

    /** the maximum length of the GTU (parallel with driving direction). */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** the maximum width of the GTU (perpendicular to driving direction). */
    private final DoubleScalar.Rel<LengthUnit> width;

    /** the maximum speed of the GTU (in the driving direction). */
    private final DoubleScalar.Abs<SpeedUnit> maximumVelocity;
    
    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;
    
    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     * @param simulator a simulator to schedule time on.
     */
    public AbstractGTU(final ID id, final GTUType<?> gtuType, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final OTSDEVSSimulatorInterface simulator)
    {
        super();
        this.id = id;
        this.gtuType = gtuType;
        this.length = length;
        this.width = width;
        this.maximumVelocity = maximumVelocity;
        this.simulator = simulator;
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final ID getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final GTUType<?> getGTUType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
    {
        return this.maximumVelocity;
    }
}
