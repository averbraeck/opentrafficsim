package org.opentrafficsim.road.network.factory.osm.events;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 20.03.2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class ProgressListenerImpl implements ProgressListener, Serializable
{
    /** */
    private static final long serialVersionUID = 20150320L;

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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ProgressListenerImpl [currentProgress=" + this.currentProgress + ", log=" + this.log + "]";
    }
}
