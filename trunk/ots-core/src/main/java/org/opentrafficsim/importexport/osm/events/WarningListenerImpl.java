package org.opentrafficsim.importexport.osm.events;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 0.03.2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class WarningListenerImpl implements WarningListener
{
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
}
