package org.opentrafficsim.importexport.osm.events;

import java.util.EventObject;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class ProgressEvent extends EventObject
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Time of first created progress event. */
    private static Long first = null;

    /** Textual description of the progress made. */
    private final String progressInformation;

    /** Time when the event occurred. */
    private final long when;

    /**
     * Construct a new ProgressEvent.
     * @param source Object; the object from which the event originates
     * @param description The progress information in a String
     */
    public ProgressEvent(final Object source, final String description)
    {
        super(source);
        this.progressInformation = description;
        this.when = System.currentTimeMillis();
        if (null == first)
        {
            first = this.when;
        }
    }

    /**
     * @return Progress Information in a String.
     */
    public final String getProgress()
    {
        return String.format("%8d ", (this.when - first) % 100000000L) + this.progressInformation;
    }
}
