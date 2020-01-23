package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.VolumeUnit;
import org.djunits.value.vdouble.scalar.Volume;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed volume.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistVolume extends DiscreteDistDoubleScalar.Rel<Volume, VolumeUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit VolumeUnit; units
     */
    public DiscreteDistVolume(final DistDiscrete distribution, final VolumeUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Volume draw()
    {
        return new Volume(getDistribution().draw(), (VolumeUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistVolume []";
    }

}
