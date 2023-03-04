package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public abstract class AbstractPerceptionCategory<G extends Gtu, P extends Perception<G>>
        extends Type<AbstractPerceptionCategory<G, P>> implements Serializable, PerceptionCategory<G, P>
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Connected perception. */
    private final P perception;

    /**
     * Constructor setting the perception.
     * @param perception P; perception
     */
    public AbstractPerceptionCategory(final P perception)
    {
        this.perception = perception;
    }

    /**
     * Returns the connected perception.
     * @return connected perception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public P getPerception()
    {
        return this.perception;
    }

    /**
     * Returns the connected GTU.
     * @return connected GTU
     * @throws GtuException if the GTU has not been initialized
     */
    @SuppressWarnings("checkstyle:designforextension")
    public G getGtu() throws GtuException
    {
        return this.perception.getGtu();
    }

    /**
     * Returns the current time.
     * @return current time
     * @throws GtuException if the GTU has not been initialized
     */
    public final Time getTimestamp() throws GtuException
    {
        if (getGtu() == null)
        {
            throw new GtuException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return getGtu().getSimulator().getSimulatorAbsTime();
    }

    /**
     * Returns the object inside a time stamped object, or {@code null} if it's null.
     * @param object TimeStampedObject&lt;T&gt;; time stamped object
     * @param <T> type of time stamped object
     * @return the object inside a time stamped object, or {@code null} if it's null
     */
    protected final <T> T getObjectOrNull(final TimeStampedObject<T> object)
    {
        if (object == null)
        {
            return null;
        }
        return object.getObject();
    }

}
