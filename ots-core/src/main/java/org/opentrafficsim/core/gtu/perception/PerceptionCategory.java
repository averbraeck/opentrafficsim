package org.opentrafficsim.core.gtu.perception;

import java.util.concurrent.atomic.AtomicInteger;

import org.opentrafficsim.base.collections.Indexed;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Super interface for all perception categories.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <G> GTU type
 * @param <P> perception type
 * @param <C> category type
 */
public interface PerceptionCategory<G extends Gtu, P extends Perception<G>, C extends PerceptionCategory<?, ?, ?>>
{

    /**
     * Returns the perception category type.
     * @return the perception category type
     */
    Type<C> getType();

    /**
     * Perception category type.
     * @param <C> category class type
     */
    class Type<C extends PerceptionCategory<?, ?, ?>> implements Indexed
    {

        /** Index supplier. */
        private static final AtomicInteger NEXT_INDEX = new AtomicInteger();

        /** Index. */
        private final int index;

        /** Name. */
        private final String name;

        /**
         * Constructor.
         * @param name name
         */
        public Type(final String name)
        {
            this.index = NEXT_INDEX.getAndIncrement();
            this.name = name;
        }

        @Override
        public int index()
        {
            return this.index;
        }

        @Override
        public String toString()
        {
            return "PerceptionCategoryType [name=" + this.name + "]";
        }

    }

}
