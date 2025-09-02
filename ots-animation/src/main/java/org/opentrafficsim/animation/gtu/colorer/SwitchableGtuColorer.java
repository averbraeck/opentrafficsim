package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.colorer.LegendColorer;

/**
 * GTU colorer that uses a coloring method that can be switched by the user of the program.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SwitchableGtuColorer implements LegendColorer<Gtu>, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The currently active GtuColorer. */
    private Colorer<? super Gtu> activeColorer;

    /** The list of included colorers. */
    private List<Colorer<? super Gtu>> colorers = new ArrayList<>();

    /**
     * Empty constructor for the builder.
     */
    SwitchableGtuColorer()
    {
        //
    }

    /**
     * Construct a new Switchable GtuColorer based on a list of colorers.
     * @param activeIndex the index of the initially active colorer in the list (0-based).
     * @param colorers the list of GtuColorer. List cannot be empty.
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SafeVarargs
    @SuppressWarnings("checkstyle:redundantthrows")
    public SwitchableGtuColorer(final int activeIndex, final Colorer<? super Gtu>... colorers) throws IndexOutOfBoundsException
    {
        this.colorers.addAll(Arrays.asList());
        setGtuColorer(activeIndex);
    }

    /**
     * Replace the currently active GtuColorer.
     * @param activeIndex the index of the new active colorer in the list (0-based).
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public final void setGtuColorer(final int activeIndex) throws IndexOutOfBoundsException
    {
        this.activeColorer = this.colorers.get(activeIndex);
    }

    @Override
    public final Color getColor(final Gtu gtu)
    {
        return this.activeColorer.getColor(gtu);
    }

    @Override
    public final List<LegendEntry> getLegend()
    {
        return this.activeColorer instanceof LegendColorer<?> legendColorer ? legendColorer.getLegend()
                : Collections.emptyList();
    }

    @Override
    public final String getName()
    {
        return "Switchable GTU Colorer";
    }

    /**
     * Return list of colorers.
     * @return the list of colorers.
     */
    public final List<Colorer<? super Gtu>> getColorers()
    {
        return this.colorers;
    }

    /**
     * Returns a builder for SwitchableGtuColorer.
     * @return builder for SwitchableGtuColorer
     */
    public static final Builder builder()
    {
        return new Builder();
    }

    /**
     * Builder for SwitchableGtuColorer.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static final class Builder
    {
        /** The list of included colorers. */
        private List<Colorer<? super Gtu>> preColorers = new ArrayList<>();

        /** The currently active GtuColorer. */
        private Colorer<? super Gtu> preActiveColorer;

        /**
         * Constructor.
         */
        public Builder()
        {
            //
        }

        /**
         * Adds a colorer.
         * @param colorer colorer
         * @return this builder for method chaining
         */
        public Builder addColorer(final Colorer<? super Gtu> colorer)
        {
            this.preColorers.add(colorer);
            return this;
        }

        /**
         * Adds a colorer, make it selected.
         * @param colorer colorer
         * @return this builder for method chaining
         */
        public Builder addActiveColorer(final Colorer<? super Gtu> colorer)
        {
            this.preColorers.add(colorer);
            this.preActiveColorer = colorer;
            return this;
        }

        /**
         * Builds the colorer.
         * @return colorer
         */
        @SuppressWarnings("synthetic-access")
        public Colorer<? super Gtu> build()
        {
            Throw.whenNull(this.preActiveColorer, "No active colorer was defined.");
            SwitchableGtuColorer colorer = new SwitchableGtuColorer();
            colorer.colorers = this.preColorers;
            colorer.activeColorer = this.preActiveColorer;
            return colorer;
        }

        @Override
        public String toString()
        {
            return "Builder [preColorers=" + this.preColorers + ", preActiveColorer=" + this.preActiveColorer + "]";
        }

    }

}
