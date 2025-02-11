package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.VolumeUnit;
import org.djunits.value.vdouble.scalar.Volume;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed volume.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistVolume extends DiscreteDistDoubleScalar.Rel<Volume, VolumeUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistVolume(final DistDiscrete distribution, final VolumeUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Volume get()
    {
        return new Volume(getDistribution().draw(), (VolumeUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistVolume []";
    }

}
