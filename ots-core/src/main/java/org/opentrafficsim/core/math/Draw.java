package org.opentrafficsim.core.math;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to draw from a collection.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class Draw
{

    /** Constructor. */
    private Draw()
    {
        // no instance
    }

    /**
     * Returns a randomly drawn element using draw weights.
     * @param map Map&lt;E, ? extends Double&gt;; map of elements and respective weights
     * @param stream StreamInterface; random number stream
     * @param <E> element type
     * @return E; randomly drawn element
     */
    public static <E> E drawWeighted(final Map<E, ? extends Double> map, final StreamInterface stream)
    {
        Throw.whenNull(map, "Map may not be null.");
        Throw.whenNull(stream, "Stream may not be null.");
        Throw.when(map.isEmpty(), IllegalArgumentException.class, "Map may not be empty.");
        double sumProb = 0.0;
        for (Entry<E, ? extends Double> e : map.entrySet())
        {
            double w = e.getValue();
            Throw.when(w < 0.0, IllegalArgumentException.class, "Probabilities should be at least 0.0.");
            sumProb += w;
        }
        Throw.when(sumProb == 0.0, IllegalArgumentException.class, "Probabilities should not add to 0.0.");

        if (map.size() == 1)
        {
            return map.keySet().iterator().next();
        }
        double r = stream.nextDouble() * sumProb;
        sumProb = 0.0;
        E last = null;
        for (Entry<E, ? extends Double> e : map.entrySet())
        {
            double f = e.getValue();
            if (f == 0)
            {
                continue; // do not assign the key of this one to last
            }
            if (r <= sumProb + f)
            {
                return e.getKey();
            }
            sumProb += f;
            last = e.getKey();
        }
        return last; // rounding error
    }

    /**
     * Returns a randomly drawn element using uniform weights.
     * @param collection Collection&lt;E&gt;; collection of elements
     * @param stream StreamInterface; random number stream
     * @param <E> element type
     * @return E; randomly drawn element
     */
    public static <E> E draw(final Collection<E> collection, final StreamInterface stream)
    {
        Throw.whenNull(collection, "Collection may not be null.");
        Throw.whenNull(stream, "Stream may not be null.");
        Throw.when(collection.isEmpty(), IllegalArgumentException.class, "Collection may not be empty.");
        int n = (int) (collection.size() * stream.nextDouble());
        Iterator<E> it = collection.iterator();
        int m = 0;
        while (m < n)
        {
            m++;
            it.next();
        }
        return it.next();
    }

}
