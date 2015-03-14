package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.network.Route;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractGTU<ID> implements GTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** the id of the GTU, could be String or Integer. */
    private final ID id;

    /** the type of GTU, e.g. TruckType, CarType, BusType. */
    private final GTUType<?> gtuType;
    
    /** Route of the gtu to follow. */
    private Route route = null;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     */
    public AbstractGTU(final ID id, final GTUType<?> gtuType)
    {
        super();
        this.id = id;
        this.gtuType = gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final ID getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public GTUType<?> getGTUType()
    {
        return this.gtuType;
    }

    /**
     * @return route.
     */
    public final Route getRoute()
    {
        return this.route;
    }

    /**
     * @param route set route.
     */
    public final void setRoute(final Route route)
    {
        this.route = route;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getReference()
    {
        return RelativePosition.REFERENCE_POSITION;
    }

}
