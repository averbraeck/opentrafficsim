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
    
    /** Textual description of the progress made. */
    private String progressInformation;
    
    /**
     * @param source Object from which the event originates.
     * @param progInfo The progess information in a String.
     */
    public ProgressEvent(final Object source, final String progInfo)
    {
        super(source);
        this.progressInformation = progInfo;
    }
    
    /**
     * @return Progress Information in a String.
     */
    public final String getProgress()
    {
        return this.progressInformation;
    }
}
