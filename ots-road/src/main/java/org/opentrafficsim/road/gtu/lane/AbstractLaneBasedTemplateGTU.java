package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTemplateGTU extends AbstractLaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** The TemplateGTUType. */
    private TemplateGTUType templateGTUType;

    /**
     * @param id the id of the GTU
     * @param templateGTUType the TemplateGTUType, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     * @throws InstantiationException in case Perception or StrategicPlanner instantiation fails
     * @throws IllegalAccessException in case Perception or StrategicPlanner constructor is not public
     */
    public AbstractLaneBasedTemplateGTU(final String id, final LaneBasedTemplateGTUType templateGTUType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed)
        throws NetworkException, SimRuntimeException, GTUException, InstantiationException, IllegalAccessException
    {
        super(id, templateGTUType.getGtuType(), initialLongitudinalPositions, initialSpeed, templateGTUType
            .getSimulator(), templateGTUType.instantiateStrategicalPlanner(), templateGTUType.instantiatePerception());
        this.templateGTUType = templateGTUType;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public GTUType getGTUType()
    {
        return super.getGTUType();
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getLength()
    {
        return this.templateGTUType.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getWidth()
    {
        return this.templateGTUType.getWidth();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getMaximumVelocity()
    {
        return this.templateGTUType.getMaximumVelocity();
    }

}
