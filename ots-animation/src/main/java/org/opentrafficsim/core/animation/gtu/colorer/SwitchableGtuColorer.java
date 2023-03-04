package org.opentrafficsim.core.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * GTU colorer that uses a coloring method that can be switched by the user of the program.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SwitchableGtuColorer implements GtuColorer, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The currently active GtuColorer. */
    private GtuColorer activeColorer;

    /** The list of included colorers. */
    private List<GtuColorer> colorers = new ArrayList<>();

    /**
     * Empty constructor for the builder.
     */
    SwitchableGtuColorer()
    {
        //
    }

    /**
     * Construct a new Switchable GtuColorer based on a list of colorers.
     * @param activeIndex int; the index of the initially active colorer in the list (0-based).
     * @param colorers GtuColorer...; the list of GtuColorer. List cannot be empty.
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public SwitchableGtuColorer(final int activeIndex, final GtuColorer... colorers) throws IndexOutOfBoundsException
    {
        this.colorers.addAll(Arrays.asList(colorers));
        setGtuColorer(activeIndex);
    }

    /**
     * Replace the currently active GtuColorer.
     * @param activeIndex int; the index of the new active colorer in the list (0-based).
     * @throws IndexOutOfBoundsException when activeIndex &lt; 0 or larger than or equal to the number of colorers.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public final void setGtuColorer(final int activeIndex) throws IndexOutOfBoundsException
    {
        this.activeColorer = this.colorers.get(activeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final Gtu gtu)
    {
        return this.activeColorer.getColor(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return this.activeColorer.getLegend();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Switchable GTU Colorer";
    }

    /**
     * @return the list of colorers.
     */
    public final List<GtuColorer> getColorers()
    {
        return this.colorers;
    }

    /**
     * Returns a builder for SwitchableGtuColorer.
     * @return Builder; builder for SwitchableGtuColorer
     */
    public static final Builder builder()
    {
        return new Builder();
    }

    /**
     * Builder for SwitchableGtuColorer.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static final class Builder
    {
        /** The list of included colorers. */
        private List<GtuColorer> preColorers = new ArrayList<>();

        /** The currently active GtuColorer. */
        private GtuColorer preActiveColorer;

        /**
         * Adds a colorer.
         * @param colorer GtuColorer; colorer
         * @return Builder; this builder for method chaining
         */
        public Builder addColorer(final GtuColorer colorer)
        {
            this.preColorers.add(colorer);
            return this;
        }

        /**
         * Adds a colorer, make it selected.
         * @param colorer GtuColorer; colorer
         * @return Builder; this builder for method chaining
         */
        public Builder addActiveColorer(final GtuColorer colorer)
        {
            this.preColorers.add(colorer);
            this.preActiveColorer = colorer;
            return this;
        }

        /**
         * Builds the colorer.
         * @return SwitchableGtuColorer; colorer
         */
        @SuppressWarnings("synthetic-access")
        public GtuColorer build()
        {
            Throw.whenNull(this.preActiveColorer, "No active colorer was defined.");
            SwitchableGtuColorer colorer = new SwitchableGtuColorer();
            colorer.colorers = this.preColorers;
            colorer.activeColorer = this.preActiveColorer;
            return colorer;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Builder [preColorers=" + this.preColorers + ", preActiveColorer=" + this.preActiveColorer + "]";
        }

    }

}
