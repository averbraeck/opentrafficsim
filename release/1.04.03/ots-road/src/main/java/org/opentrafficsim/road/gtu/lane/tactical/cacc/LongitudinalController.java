package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import static org.opentrafficsim.base.parameters.constraint.ConstraintInterface.POSITIVE;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LongitudinalController
{

    /** Parameter for sensor range. */
    ParameterTypeLength SENSOR_RANGE =
            new ParameterTypeLength("Sensor range", "Maximum sensor range", new Length(300.0, LengthUnit.SI), POSITIVE);

    /**
     * Calculates the acceleration.
     * @param gtu LaneBasedGTU; the GTU
     * @return Acceleration from controller.
     * @throws OperationalPlanException
     * @throws ParameterException
     */
    Acceleration calculateAcceleration(LaneBasedGTU gtu) throws OperationalPlanException, ParameterException;

}
