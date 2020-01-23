package org.opentrafficsim.imb.transceiver;

import java.io.Serializable;

import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * Store the results of an IBM message transformation, as the aTByteBuffer message can only be parsed once.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IMBTransformResult implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160911L;

    /** the event content to store. */
    private final Object eventContent;

    /** the event listener to store. */
    private final EventListenerInterface eventListener;

    /**
     * Store the results of an IBM message transformation, as the aTByteBuffer message can only be parsed once.
     * @param eventContent Object; the event content to store
     * @param eventListener EventListenerInterface; the event listener to store
     */
    public IMBTransformResult(Object eventContent, EventListenerInterface eventListener)
    {
        super();
        this.eventContent = eventContent;
        this.eventListener = eventListener;
    }

    /**
     * @return eventContent Object; the stored event content
     */
    public final Object getEventContent()
    {
        return this.eventContent;
    }

    /**
     * @return eventListener EventListenerInterface; the stored event listener
     */
    public final EventListenerInterface getEventListener()
    {
        return this.eventListener;
    }
}
