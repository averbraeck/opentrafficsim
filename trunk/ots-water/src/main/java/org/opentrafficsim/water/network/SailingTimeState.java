/**
 * 
 */
package org.opentrafficsim.water.network;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SailingTimeState
{
    /** the sailing time. */
    private Duration sailingTime;

    /** does the ship fit the waterway? */
    private boolean fits;

    /** if not, what is the reason? */
    private String reason;

    /**
     * @param sailingTime Duration; the sailing time
     * @param fits boolean; does the ship fit the waterway?
     * @param reason String; if not, what is the reason?
     */
    public SailingTimeState(final Duration sailingTime, final boolean fits, final String reason)
    {
        super();
        this.sailingTime = sailingTime;
        this.fits = fits;
        this.reason = reason;
    }

    /**
     * @return the sailingTime
     */
    public final Duration getSailingTime()
    {
        return this.sailingTime;
    }

    /**
     * @return the fits
     */
    public final boolean isFits()
    {
        return this.fits;
    }

    /**
     * @return the reason
     */
    public final String getReason()
    {
        return this.reason;
    }

}
