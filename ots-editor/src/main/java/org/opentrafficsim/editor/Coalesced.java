package org.opentrafficsim.editor;

import javax.swing.SwingUtilities;

import org.djutils.exceptions.Throw;

/**
 * Coalesced running. Upon a {@link #request}, execution of the runnable is scheduled using {@link SwingUtilities#invokeLater}.
 * If multiple requests arrive before execution, then they are coalesced in to a single execution.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class Coalesced
{

    /** Whether running is currently scheduled. */
    private boolean scheduled;

    /** Runnable. */
    private final Runnable runnable;

    /**
     * Constructor.
     * @param runnable runnable
     */
    public Coalesced(final Runnable runnable)
    {
        Throw.whenNull(runnable, "runnable");
        this.runnable = runnable;
    }

    /**
     * Request execution of runnable.
     */
    public void request()
    {
        if (this.scheduled)
        {
            return;
        }
        this.scheduled = true;
        SwingUtilities.invokeLater(() ->
        {
            this.scheduled = false;
            this.runnable.run();
        });
    }

}
