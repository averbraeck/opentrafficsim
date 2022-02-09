/**
 * 
 */
package org.opentrafficsim.water.demand;

import java.io.Serializable;

import org.opentrafficsim.water.role.Company;
import org.opentrafficsim.water.transfer.Terminal;

/**
 * A number of similar (empty/full, 20/40 ft, same owner) containers that has to travel / travels between an origin terminal and
 * a destination terminal.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class PartialLoad implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** origin terminal. */
    private Terminal terminalFrom;

    /** destination terminal. */
    private Terminal terminalTo;

    /** number of containers. */
    private int number;

    /** 20 ft? */
    private boolean twentyFt;

    /** empty? */
    private boolean empty;

    /** who owns the container. */
    private Company owner;

    /**
     * @param terminalFrom Terminal; origin terminal
     * @param terminalTo Terminal; destination terminal
     * @param number int; number of containers
     * @param twentyFt boolean; 20 ft?
     * @param empty boolean; empty?
     * @param owner Company; who owns the container
     */
    public PartialLoad(final Terminal terminalFrom, final Terminal terminalTo, final int number, final boolean twentyFt,
            final boolean empty, final Company owner)
    {
        this.terminalFrom = terminalFrom;
        this.terminalTo = terminalTo;
        this.number = number;
        this.twentyFt = twentyFt;
        this.empty = empty;
        this.owner = owner;
    }

    /**
     * @return the terminalFrom
     */
    public final Terminal getTerminalFrom()
    {
        return this.terminalFrom;
    }

    /**
     * @return the terminalTo
     */
    public final Terminal getTerminalTo()
    {
        return this.terminalTo;
    }

    /**
     * @return the number
     */
    public final int getNumber()
    {
        return this.number;
    }

    /**
     * @return the twentyFt
     */
    public final boolean isTwentyFt()
    {
        return this.twentyFt;
    }

    /**
     * @return the empty
     */
    public final boolean isEmpty()
    {
        return this.empty;
    }

    /**
     * @return the owner
     */
    public final Company getOwner()
    {
        return this.owner;
    }

}
