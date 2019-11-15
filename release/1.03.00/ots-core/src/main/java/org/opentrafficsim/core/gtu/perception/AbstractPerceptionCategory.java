package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public abstract class AbstractPerceptionCategory<G extends GTU, P extends Perception<G>>
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
     * @throws GTUException if the GTU has not been initialized
     */
    @SuppressWarnings("checkstyle:designforextension")
    public G getGtu() throws GTUException
    {
        return this.perception.getGtu();
    }

    /**
     * Returns the current time.
     * @return current time
     * @throws GTUException if the GTU has not been initialized
     */
    public final Time getTimestamp() throws GTUException
    {
        if (getGtu() == null)
        {
            throw new GTUException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return getGtu().getSimulator().getSimulatorTime();
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
