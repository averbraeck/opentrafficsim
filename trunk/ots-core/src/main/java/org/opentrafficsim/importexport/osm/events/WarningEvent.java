package org.opentrafficsim.importexport.osm.events;

import java.util.EventObject;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class WarningEvent extends EventObject
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Object which caused the warning. */

    /** Textual description of the warning. */
    private String warning;

    /**
     * @param source of the WarningEvent.
     * @param warn Textual description of the warning.
     */
    public WarningEvent(final Object source, final String warn)
    {
        super(source);
        this.warning = warn;
    }

    /**
     * @return Get the textual description of the warning.
     */
    public final String getWarning()
    {
        return this.warning;
    }
}
