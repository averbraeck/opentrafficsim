package org.opentrafficsim.base;

import java.util.function.Supplier;

import org.djutils.exceptions.Throw;

/**
 * Id generator that produces A, B, C, ... X, Y, Z, AA, AB, AC, ... AX, AY, AZ, BA, BB, BC, etc., with possible prefix.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AlphabeticIdGenerator implements Supplier<String>
{

    /** Prefix. */
    private final String prefix;

    /** Id counter. */
    private int counter = 1;

    /**
     * Constructor setting no prefix.
     */
    public AlphabeticIdGenerator()
    {
        this.prefix = "";
    }

    /**
     * Constructor setting id prefix.
     * @param prefix prefix
     */
    public AlphabeticIdGenerator(final String prefix)
    {
        Throw.whenNull(prefix, "prefix should not be null.");
        this.prefix = prefix;
    }

    @Override
    public String get()
    {
        StringBuilder sb = new StringBuilder();
        int n = this.counter++;
        while (n > 0)
        {
            n--; // make remainder 0..25 map to 'A'..'Z'
            long rem = n % 26;
            sb.append((char) ('A' + rem));
            n /= 26;
        }
        return sb.reverse().insert(0, this.prefix).toString();
    }

}
