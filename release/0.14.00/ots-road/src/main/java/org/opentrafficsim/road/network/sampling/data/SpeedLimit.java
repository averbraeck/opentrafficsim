package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.sampling.GtuData;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedLimit extends ExtendedDataTypeSpeed
{

    /**
     * Constructor.
     */
    public SpeedLimit()
    {
        super("SpeedLimit");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatSpeed getValue(final GtuDataInterface gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        Throw.when(!(gtu instanceof GtuData), IllegalArgumentException.class,
                "Extended data type speed limit is only for use with GtuData. The provided data %s is of the wrong type.", gtu);
        LaneBasedGTU laneGtu = ((GtuData) gtu).getGtu();
        try
        {
            return new FloatSpeed(laneGtu.getReferencePosition().getLane().getSpeedLimit(laneGtu.getGTUType()).si,
                    SpeedUnit.SI);
        }
        catch (NetworkException | GTUException exception)
        {
            throw new RuntimeException("Could not obtain speed limit.", exception);
        }
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "SpeedLimit";
    }

}
