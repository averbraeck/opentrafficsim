package org.opentrafficsim.road.gtu.lane;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * Factory for vehicle models.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface VehicleModelFactory
{

    /** No vehicle model. */
    VehicleModelFactory NONE = fixed(VehicleModel.NONE);

    /** Acceleration bounded vehicle model. */
    VehicleModelFactory MINMAX = fixed(VehicleModel.MINMAX);

    /**
     * Factory returning the same instance always.
     * @param vehicleModel VehicleModel; vehicle model
     * @return fixed vehicle model
     */
    static VehicleModelFactory fixed(final VehicleModel vehicleModel)
    {
        return new VehicleModelFactory()
        {
            @Override
            public VehicleModel create(final GTUType gtuType)
            {
                return vehicleModel;
            }
        };
    }

    /**
     * Create next vehicle model for given GTU type.
     * @param gtuType GTUType; GTU type
     * @return next vehicle model for given GTU type
     */
    VehicleModel create(GTUType gtuType);

}
