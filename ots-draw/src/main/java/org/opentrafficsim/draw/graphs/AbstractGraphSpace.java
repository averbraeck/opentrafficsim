package org.opentrafficsim.draw.graphs;

import java.util.Iterator;
import java.util.List;

/**
 * Common functionality between a cross-section and a path.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <S> underlying type of path sections
 */
public abstract class AbstractGraphSpace<S> implements Iterable<S>
{

    /** Series names. */
    private final List<String> seriesNames;

    /**
     * Constructor.
     * @param seriesNames names of series
     */
    public AbstractGraphSpace(final List<String> seriesNames)
    {
        this.seriesNames = seriesNames;
    }

    /**
     * Returns the name of the series.
     * @param series series
     * @return name of the series
     */
    public String getName(final int series)
    {
        return this.seriesNames.get(series);
    }

    /**
     * Returns the number of series.
     * @return number of series
     */
    public int getNumberOfSeries()
    {
        return this.seriesNames.size();
    }

    /**
     * Returns an iterator over the sources on the given series.
     * @param series number of the series
     * @return iterator over the sources on the given series
     */
    abstract Iterator<S> iterator(int series);

}
