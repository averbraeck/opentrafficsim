package org.opentrafficsim.core.network.lane;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.geometry.OTSBuffering;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** the id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink parentLink;

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
     * @param id String; The id of the CrosssSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at start, positioned <i>symmetrically around</i> the design
     *            line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; width at end, positioned <i>symmetrically around</i> the design line
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtBegin, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd,
        final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth)
        throws OTSGeometryException, NetworkException
    {
        super();
        if (id == null)
        {
            throw new NetworkException("Constructor of CrossSectionElement -- id cannot be null");
        }
        for (CrossSectionElement cse : parentLink.getCrossSectionElementList())
        {
            if (cse.getId().equals(id))
            {
                throw new NetworkException("Constructor of CrossSectionElement -- id " + id + " not unique within the Link");
            }
        }
        this.id = id;
        this.parentLink = parentLink;
        this.designLineOffsetAtBegin = lateralOffsetAtBegin;
        this.designLineOffsetAtEnd = lateralOffsetAtEnd;
        this.beginWidth = beginWidth;
        this.endWidth = endWidth;

        this.centerLine =
            OTSBuffering.offsetLine(this.getParentLink().getDesignLine(), this.designLineOffsetAtBegin.getSI(),
                this.designLineOffsetAtEnd.getSI());
        this.length = this.centerLine.getLength();
        this.contour = OTSBuffering.constructContour(this);

        this.parentLink.addCrossSectionElement(this);
    }

    /**
     * @return parentLink.
     */
    public final CrossSectionLink getParentLink()
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
        return DoubleScalar.interpolate(this.designLineOffsetAtBegin, this.designLineOffsetAtEnd, fractionalPosition);
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
        return DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalPosition);
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

    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
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
                fractionalLongitudinalPosition);
        DoubleScalar.Rel<LengthUnit> halfWidth =
            DoubleScalar.interpolate(this.beginWidth, this.endWidth, fractionalLongitudinalPosition).multiplyBy(0.5);
        switch (lateralDirection)
        {
            case LEFT:
                return DoubleScalar.minus(designLineOffset, halfWidth);
            case RIGHT:
                return DoubleScalar.plus(designLineOffset, halfWidth);
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
        DirectedPoint centroid = this.contour.getLocation();
        return new DirectedPoint(centroid.x, centroid.y, getZ());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds() throws RemoteException
    {
        return this.contour.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("CSE offset %.2fm..%.2fm, width %.2fm..%.2fm", this.designLineOffsetAtBegin.getSI(),
            this.designLineOffsetAtEnd.getSI(), this.beginWidth.getSI(), this.endWidth.getSI());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.parentLink == null) ? 0 : this.parentLink.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrossSectionElement other = (CrossSectionElement) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.parentLink == null)
        {
            if (other.parentLink != null)
                return false;
        }
        else if (!this.parentLink.equals(other.parentLink))
            return false;
        return true;
    }

}
