package org.opentrafficsim.road.gtu.lane;

import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.route.CompleteLaneBasedRouteNavigator;

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
     * @param gtuFollowingModel the following model, including a reference to the simulator
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param routeNavigator RouteNavigator; the navigator that the GTU will use
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     */
    public AbstractLaneBasedTemplateGTU(final String id, final TemplateGTUType templateGTUType,
        final GTUFollowingModel gtuFollowingModel, final Map<Lane, Length.Rel> initialLongitudinalPositions,
        final Speed.Abs initialSpeed, final CompleteLaneBasedRouteNavigator routeNavigator) throws
        NetworkException, SimRuntimeException, GTUException
    {
        super(id, templateGTUType.getGtuType(), gtuFollowingModel, null /* LaneChangeModel */, initialLongitudinalPositions,
            initialSpeed, routeNavigator, templateGTUType.getSimulator());
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
    public final Speed.Abs getMaximumVelocity()
    {
        return this.templateGTUType.getMaximumVelocity();
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.templateGTUType.getSimulator();
    }

}
