package org.opentrafficsim.importexport.osm.events;

import java.util.EventObject;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class ProgressEvent extends EventObject
{
    /** */
    private static final long serialVersionUID = 1L;
    
    /** Time of first created progress event. */
    private static Long first = null;
    
    /** Textual description of the progress made. */
    private String progressInformation;
    
    /** Time when the event occurred. */
    private final long when;
    
    /**
     * @param source Object from which the event originates.
     * @param progInfo The progress information in a String.
     */
    public ProgressEvent(final Object source, final String progInfo)
    {
        super(source);
        this.progressInformation = progInfo;
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
