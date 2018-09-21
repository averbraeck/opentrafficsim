package org.opentrafficsim.base.logger;

import java.util.Random;

/**
 * LogCategory for the CategoryLogger. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LogCategory
{
    /** The category name; can be blank. */
    private final String name;

    /** Cached hashcode for very quick retrieval. */
    private final int hashCode;

    /** Random number to generate fast hashCode. */
    private static Random random = new Random(1L);

    /** The category to indicate that ALL messages need to be logged. */
    public static final LogCategory ALL = new LogCategory("ALL");
    
    /**
     * @param name the category name; can be blank
     */
    public LogCategory(final String name)
    {
        this.name = name == null ? "" : name;
        this.hashCode = calcHashCode();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.hashCode;
    }

    /**
     * Calculate the hashCode. In case of a blank name, use a reproducible random number (so NOT the memory address of the
     * LogCategory object)
     * @return the calculated hash code
     */
    private int calcHashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (this.name == null || this.name.length() == 0) ? random.nextInt() : prime + this.name.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogCategory other = (LogCategory) obj;
        if (this.hashCode != other.hashCode)
            return false;
        return true;
    }

}
