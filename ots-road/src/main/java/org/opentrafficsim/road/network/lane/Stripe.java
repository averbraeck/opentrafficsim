package org.opentrafficsim.road.network.lane;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.network.lane.StripeData.StripePhaseSync;

/**
 * Stripe road marking. This class only contains functional information. There is no information on how to draw the stripe, i.e.
 * no color and no information on dashes. The stripe types has information on this, but this only serves as a default towards
 * classes that do draw a stripe.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Stripe extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Stripe data. */
    private final StripeData data;

    /** Longitudinal offset of dashes. */
    private Length dashOffset = Length.ZERO;

    /**
     * Constructor specifying geometry. Permeability is set according to the stripe type default.
     * @param id id
     * @param data stripe data, including permeability, stripe elements and dash synchronization
     * @param link link
     * @param geometry geometry
     */
    public Stripe(final String id, final StripeData data, final CrossSectionLink link, final CrossSectionGeometry geometry)
    {
        super(link, id, geometry);
        Throw.whenNull(data, "Type may not be null.");
        this.data = data;
    }

    /**
     * Add lateral permeability for a GTU type. This overrules permeability based on the stripe type, and those set regardless
     * of GTU type. Add NONE to prevent lane changes. Add LEFT or RIGHT, or both in two calls, to enable lane changes.
     * @param gtuType GTU type to add permeability for.
     * @param lateralDirection direction to add compared to the direction of the design line.
     */
    public void addPermeability(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        this.data.addPermeability(gtuType, lateralDirection);
    }

    /**
     * Returns whether the given GTU type is allowed to cross the line in the given lateral direction.
     * @param gtuType GTU type to look for.
     * @param lateralDirection direction to look for (LEFT or RIGHT) compared to the direction of the design line.
     * @return whether the road marker is permeable for the GTU type.
     */
    public final boolean isPermeable(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        return this.data.isPermeable(gtuType, lateralDirection);
    }

    /**
     * Returns the elements.
     * @return elements
     */
    public List<StripeElement> getElements()
    {
        return this.data.getElements();
    }

    /**
     * Sets the dash offset.
     * @param dashOffset dash offset
     */
    public void setDashOffset(final Length dashOffset)
    {
        this.dashOffset = dashOffset;
    }

    /**
     * Returns the dash offset.
     * @return dash offset
     */
    public Length getDashOffset()
    {
        return this.dashOffset;
    }

    /**
     * Sets the lateral synchronization.
     * @param lateralSync lateral synchronization
     */
    public void setLateralSync(final StripeLateralSync lateralSync)
    {
        this.data.setLateralSync(lateralSync);
    }

    /**
     * Returns the lateral synchronization.
     * @return lateral synchronization
     */
    public StripeLateralSync getLateralSync()
    {
        return this.data.getLateralSync();
    }

    /**
     * Sets the phase synchronization.
     * @param phaseSync phase synchronization
     */
    public void setPhaseSync(final StripePhaseSync phaseSync)
    {
        this.data.setPhaseSync(phaseSync);
    }

    /**
     * Returns the phase synchronization.
     * @return phase synchronization
     */
    public StripePhaseSync getPhaseSync()
    {
        return this.data.getPhaseSync();
    }

    /**
     * Returns the period of the common dash pattern.
     * @return period of the common dash pattern
     */
    public double getPeriod()
    {
        return this.data.getPeriod();
    }

    @Override
    public String toString()
    {
        return "Stripe [id=" + this.getFullId() + "]";
    }

    /**
     * Stripe type defines the default permeability, width and elements.
     * @param id id
     * @param left left lane change allowed by default
     * @param right right lane change allowed by default
     * @param width default width
     * @param elements list of default elements
     */
    public record StripeType(String id, boolean left, boolean right, Length width, List<StripeElement> elements)
            implements Type<StripeType>, Identifiable
    {

        /** Standard width. */
        private static final Length WIDTH = Length.instantiateSI(0.2);

        /** Single solid line. */
        public static final StripeType SOLID =
                new StripeType("SOLID", false, false, WIDTH, List.of(StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Line |¦ allow to go to left, but not to right. */
        public static final StripeType LEFT = new StripeType("LEFT", true, false, WIDTH.times(3.0),
                List.of(StripeElement.continuous(WIDTH, Color.WHITE), StripeElement.gap(WIDTH),
                        StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Line ¦| allow to go to right, but not to left. */
        public static final StripeType RIGHT = new StripeType("RIGHT", false, true, Length.instantiateSI(0.6),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3})),
                        StripeElement.gap(WIDTH), StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Dashes ¦ allow to cross in both directions. */
        public static final StripeType DASHED = new StripeType("DASHED", true, true, Length.instantiateSI(0.2),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Double solid line ||, don't cross. */
        public static final StripeType DOUBLE_SOLID = new StripeType("DOUBLE_SOLID", false, false, Length.instantiateSI(0.6),
                List.of(StripeElement.continuous(WIDTH, Color.WHITE), StripeElement.gap(WIDTH),
                        StripeElement.continuous(WIDTH, Color.WHITE)));

        /** Double dashed line ¦¦, cross. */
        public static final StripeType DOUBLE_DASH = new StripeType("DOUBLE_DASHED", true, true, Length.instantiateSI(0.6),
                List.of(StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3})),
                        StripeElement.gap(WIDTH),
                        StripeElement.dashed(WIDTH, Color.WHITE, new LengthVector(new double[] {9, 3}))));

        /** Block : allow to cross in both directions. */
        public static final StripeType BLOCK = new StripeType("BLOCK", true, true, WIDTH.times(2.0),
                List.of(StripeElement.dashed(WIDTH.times(2.0), Color.WHITE, new LengthVector(new double[] {3, 1}))));

        @Override
        public String getId()
        {
            return this.id;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.id);
        }

        @Override
        @SuppressWarnings("needbraces")
        public boolean equals(final Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StripeType other = (StripeType) obj;
            return Objects.equals(this.id, other.id);
        }
    }

}
