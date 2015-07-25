package org.opentrafficsim.importexport.osm.events;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class ProgressListenerImpl implements ProgressListener
{
    /** Most recent Progress. */
    private String currentProgress;

    /** Log. */
    private String log;

    /** {@inheritDoc} */
    @Override
    public final void progress(final ProgressEvent progressEvent)
    {
        this.currentProgress = progressEvent.getProgress();
        this.log += this.currentProgress + "\n";
        System.out.println(this.currentProgress);
    }

    /**
     * @return Current Progress as String.
     */
    public final String getCurrentProgress()
    {
        return this.currentProgress;
    }

    /**
     * @return Log as String
     */
    public final String getLog()
    {
        return this.log;
    }
}
