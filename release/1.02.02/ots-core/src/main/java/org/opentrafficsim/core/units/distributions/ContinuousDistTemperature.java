package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.TemperatureUnit;
import org.djunits.value.vdouble.scalar.Temperature;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed temperature.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistTemperature extends ContinuousDistDoubleScalar.Rel<Temperature, TemperatureUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit TemperatureUnit; units
     */
    public ContinuousDistTemperature(final DistContinuous distribution, final TemperatureUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Temperature draw()
    {
        return new Temperature(getDistribution().draw(), (TemperatureUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistTemperature []";
    }

}
