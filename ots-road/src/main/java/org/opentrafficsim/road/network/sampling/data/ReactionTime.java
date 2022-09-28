package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeDuration;
import org.opentrafficsim.road.network.sampling.GtuData;

/**
 * Leader id in trajectory information.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 mrt. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ReactionTime extends ExtendedDataTypeDuration<GtuData>
{

    /**
     * Constructor.
     */
    public ReactionTime()
    {
        super("Tr");
    }

    /** {@inheritDoc} */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        try
        {
            return new FloatDuration(gtu.getGtu().getParameters().getParameter(ParameterTypes.TR).getSI(), DurationUnit.SI);
        }
        catch (ParameterException exception)
        {
            return FloatDuration.NaN;
        }
    }

}
