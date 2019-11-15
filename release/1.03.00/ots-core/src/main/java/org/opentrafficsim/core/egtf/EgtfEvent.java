package org.opentrafficsim.core.egtf;

import java.util.EventObject;

/**
 * EGTF event with progress and the ability to interrupt calculations.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class EgtfEvent extends EventObject
{

    /** */
    private static final long serialVersionUID = 20181008L;

    /** Progress, a value in the range [0 ... 1]. */
    private final double progress;

    /**
     * Constructor.
     * @param egtf EGTF; egtf
     * @param progress double; progress, a value in the range [0 ... 1]
     */
    EgtfEvent(final EGTF egtf, final double progress)
    {
        super(egtf);
        this.progress = progress;
    }

    /**
     * Returns the progress, a value in the range [0 ... 1].
     * @return double; progress, a value in the range [0 ... 1]
     */
    public final double getProgress()
    {
        return this.progress;
    }

    /**
     * Interrupts the filter. If a {@code filter()} calculation is ongoing, it will stop and return {@code null}.
     */
    public final void interrupt()
    {
        ((EGTF) getSource()).interrupt();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "EgtfEvent [progress=" + this.progress + "]";
    }

}
