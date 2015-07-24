package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.geometry.OTSBuffering;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public abstract class CrossSectionElement<LINKID, NODEID> implements LocatableInterface
{
    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink<LINKID, NODEID> parentLink;

    /** The lateral offset from the design line of the parentLink at the start of the parentLink. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DoubleScalar.Rel<LengthUnit> designLineOffsetAtBegin;

    /** The lateral offset from the design line of the parentLink at the end of the parentLink. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DoubleScalar.Rel<LengthUnit> designLineOffsetAtEnd;

    /** Start width, positioned <i>symmetrically around</i> the lateral start position. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DoubleScalar.Rel<LengthUnit> beginWidth;

    /** End width, positioned <i>symmetrically around</i> the lateral end position. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DoubleScalar.Rel<LengthUnit> endWidth;

    /** The length of the line. Calculated once at the creation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final DoubleScalar.Rel<LengthUnit> length;

    /** The center line of the element. Calculated once at the creation. */
    private final OTSLine3D centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final OTSLine3D contour;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at start, positioned <i>symmetrically around</i> the design
     *            line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at end, positioned <i>symmetrically around</i> the design line
     * @throws NetworkException when creation of the geometry fails
     */
    public CrossSectionElement(final CrossSectionLink<LINKID, NODEID> parentLink,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtBegin, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd,
        final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth) throws NetworkException
    {
        super();
        this.parentLink = parentLink;
        this.designLineOffsetAtBegin = lateralOffsetAtBegin;
        this.designLineOffsetAtEnd = lateralOffsetAtEnd;
        this.beginWidth = beginWidth;
        this.endWidth = endWidth;

        this.centerLine = OTSBuffering.offsetGeometry(parentLink.getDesignLine(), lateralOffsetAtBegin.getSI());
        this.length = this.centerLine.getLength();
        this.contour = OTSBuffering.offsetGeometry(this.centerLine, beginWidth.getSI());

        this.parentLink.addCrossSectionElement(this);
    }

    /**
     * @return parentLink.
     */
    public final CrossSectionLink<LINKID, NODEID> getParentLink()
    {
        return this.parentLink;
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition double; fractional longitudinal position on this Lane
     * @return DoubleScalar.Rel&lt;LengthUnit&gt; the lateralCenterPosition at the specified longitudinal position
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralCenterPosition(final double fractionalPosition)
    {
        return DoubleScalar.interpolate(this.designLineOffsetAtBegin, this.designLineOffsetAtEnd, fractionalPosition)
            .immutable();
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position on this Lane
     * @return DoubleScalar.Rel&lt;LengthUnit&gt; the lateralCenterPosition at the specified longitudinal position
     */
    public final DoubleScalar<LengthUnit> getLateralCenterPosition(final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition DoubleScalar&lt;LengthUnit&gt;; the longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified longitudinal position.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth(final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the width of this CrossSectionElement at the specified fractional
     *         longitudinal position.
     */
    public final DoubleScalar.Rel<LengthUnit> getWidth(final double fractionalPosition)
    {
        return DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalPosition).immutable();
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the length of this CrossSectionElement
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /**
     * @return designLineOffsetAtBegin.
     */
    public final DoubleScalar.Rel<LengthUnit> getDesignLineOffsetAtBegin()
    {
        return this.designLineOffsetAtBegin;
    }

    /**
     * @return designLineOffsetAtEnd.
     */
    public final DoubleScalar.Rel<LengthUnit> getDesignLineOffsetAtEnd()
    {
        return this.designLineOffsetAtEnd;
    }

    /**
     * @return beginWidth.
     */
    public final DoubleScalar.Rel<LengthUnit> getBeginWidth()
    {
        return this.beginWidth;
    }

    /**
     * @return endWidth.
     */
    public final DoubleScalar.Rel<LengthUnit> getEndWidth()
    {
        return this.endWidth;
    }

    /**
     * @return the z-offset for drawing (what's on top, what's underneath).
     */
    protected abstract double getZ();
    
    /**
     * @return centerLine.
     */
    public final OTSLine3D getCenterLine()
    {
        return this.centerLine;
    }

    /**
     * @return contour.
     */
    public final OTSLine3D getContour()
    {
        return this.contour;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("CSE offset %.2fm..%.2fm, width %.2fm..%.2fm", this.designLineOffsetAtBegin.getSI(),
            this.designLineOffsetAtEnd.getSI(), this.beginWidth.getSI(), this.endWidth.getSI());
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
        final double fractionalLongitudinalPosition)
    {
        DoubleScalar.Rel<LengthUnit> designLineOffset =
            DoubleScalar.interpolate(this.designLineOffsetAtBegin, this.designLineOffsetAtEnd,
                fractionalLongitudinalPosition).immutable();
        DoubleScalar.Rel<LengthUnit> halfWidth =
            DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalLongitudinalPosition).multiplyBy(0.5)
                .immutable();
        switch (lateralDirection)
        {
            case LEFT:
                return DoubleScalar.minus(designLineOffset, halfWidth).immutable();
            case RIGHT:
                return DoubleScalar.plus(designLineOffset, halfWidth).immutable();
            default:
                throw new Error("Bad switch on LateralDirectionality " + lateralDirection);
        }
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position along the length of this CrossSectionElement
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    public final DoubleScalar.Rel<LengthUnit> getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
        final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation() throws RemoteException
    {
        return this.contour.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds() throws RemoteException
    {
        return this.contour.getBounds();
    }

}
