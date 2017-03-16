package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.sampling.GtuData;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class ReferenceSpeed extends ExtendedDataType<FloatSpeed>
{

    /**
     * 
     */
    public ReferenceSpeed()
    {
        super("referenceSpeed");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatSpeed getValue(final GtuDataInterface gtu)
    {
        Throw.when(!(gtu instanceof GtuData), IllegalArgumentException.class,
                "Extended data type ReferenceSpeed can only be used with GtuData.");
        LaneBasedGTU gtuObj = ((GtuData) gtu).getGtu();
        try
        {
            double v1 = gtuObj.getReferencePosition().getLane().getSpeedLimit(gtuObj.getGTUType()).si;
            double v2 = gtuObj.getMaximumSpeed().si;
            return new FloatSpeed(v1 < v2 ? v1 : v2, SpeedUnit.SI);
        }
        catch (GTUException exception)
        {
            // GTU was destroyed and is without a reference location
            return new FloatSpeed(Double.NaN, SpeedUnit.SI);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain reference speed from GTU " + gtuObj, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String formatValue(final String format, final FloatSpeed value)
    {
        return String.format(format, value.si);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Reference Speed";
    }

}
