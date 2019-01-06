package org.opentrafficsim.road.network.factory.osm.events;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class WarningListenerImpl implements WarningListener, Serializable
{
    /** */
    private static final long serialVersionUID = 20150320L;

    /** Textual description of current warning. */
    private String currentWarning;

    /** Log. */
    private String warningLog;

    /** {@inheritDoc} */
    @Override
    public final void warning(final WarningEvent warningEvent)
    {
        this.currentWarning = warningEvent.getWarning();
        this.warningLog += this.currentWarning + "\n";
        System.out.println(this.currentWarning);
    }

    /**
     * @return Current Warning as String.
     */
    public final String getCurrentWarning()
    {
        return this.currentWarning;
    }

    /**
     * @return WarningLog as String.
     */
    public final String getWarningLog()
    {
        return this.warningLog;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "WarningListenerImpl [currentWarning=" + this.currentWarning + ", warningLog=" + this.warningLog + "]";
    }
}
