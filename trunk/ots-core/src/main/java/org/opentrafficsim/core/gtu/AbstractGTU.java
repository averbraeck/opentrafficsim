package org.opentrafficsim.core.gtu;

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

}
