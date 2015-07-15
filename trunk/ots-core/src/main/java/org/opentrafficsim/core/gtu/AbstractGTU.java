package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;

import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
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

    /** Total traveled distance. */
    protected DoubleScalar.Abs<LengthUnit> odometer = new DoubleScalar.Abs<LengthUnit>(0, LengthUnit.SI);

    /**
     * @param id the id of the GTU, could be String or Integer
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param route Route; the route that the GTU will take
     * @throws GTUException when route is null
     */
    public AbstractGTU(final ID id, final GTUType<?> gtuType, final Route route) throws GTUException
    {
        super();
        this.id = id;
        this.gtuType = gtuType;
        if (null == route)
        {
            throw new GTUException("route may not be null");
        }
        this.route = route;
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

    /**
     * Retrieve the odometer value.
     * @return DoubleScalar.Abs&lt;LengthUnit&gt;; the current odometer value
     * @throws RemoteException on communications failure
     */
    public abstract DoubleScalar.Abs<LengthUnit> getOdometer() throws RemoteException;

}
