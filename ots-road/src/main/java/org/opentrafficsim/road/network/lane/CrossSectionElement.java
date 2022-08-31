package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.geometry.OTSShape;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Cross section elements are used to compose a CrossSectionLink.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement extends EventProducer implements Locatable, Serializable, Identifiable, Drawable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** The id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink parentLink;

    /** The offsets and widths at positions along the line, relative to the design line of the parent link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final List<CrossSectionSlice> crossSectionSlices;

    /** The length of the line. Calculated once at the creation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Length length;

    /** The center line of the element. Calculated once at the creation. */
    private final OTSLine3D centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final OTSShape contour;

    /** Maximum direction difference w.r.t. node direction at beginning and end of a CrossSectionElement. */
    public static final double MAXIMUMDIRECTIONERROR = Math.toRadians(0.1);

    /**
     * At what fraction of the first segment will an extra point be inserted if the <code>MAXIMUMDIRECTIONERROR</code> is
     * exceeded.
     */
    public static final double FIXUPPOINTPROPORTION = 1.0 / 3;

    /**
     * Construct a new CrossSectionElement. <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative
     * lateral direction, with the direction from the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrossSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; the offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique, or there are multiple slices and the last slice does not
     *             end at the length of the design line.
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id,
            final List<CrossSectionSlice> crossSectionSlices) throws OTSGeometryException, NetworkException
    {
        Throw.when(parentLink == null, NetworkException.class,
                "Constructor of CrossSectionElement for id %s, parentLink cannot be null", id);
        Throw.when(id == null, NetworkException.class, "Constructor of CrossSectionElement -- id cannot be null");
        for (CrossSectionElement cse : parentLink.getCrossSectionElementList())
        {
            Throw.when(cse.getId().equals(id), NetworkException.class,
                    "Constructor of CrossSectionElement -- id %s not unique within the Link", id);
        }
        Throw.whenNull(crossSectionSlices, "crossSectionSlices may not be null");
        this.id = id;
        this.parentLink = parentLink;

        this.crossSectionSlices = new ArrayList<>(crossSectionSlices); // copy of list with immutable slices
        Throw.when(this.crossSectionSlices.size() == 0, NetworkException.class,
                "CrossSectionElement %s is created with zero slices for %s", id, parentLink);
        Throw.when(this.crossSectionSlices.get(0).getRelativeLength().si != 0.0, NetworkException.class,
                "CrossSectionElement %s for %s has a first slice with relativeLength is not equal to 0.0", id, parentLink);
        Throw.when(
                this.crossSectionSlices.size() > 1 && this.crossSectionSlices.get(this.crossSectionSlices.size() - 1)
                        .getRelativeLength().ne(this.parentLink.getLength()),
                NetworkException.class, "CrossSectionElement %s for %s has a last slice with relativeLength is not equal "
                        + "to the length of the parent link",
                id, parentLink);
        OTSLine3D proposedCenterLine = null;
        if (this.crossSectionSlices.size() <= 2)
        {
            proposedCenterLine = fixTightInnerCurve(new double[] { 0.0, 1.0 },
                    new double[] { getDesignLineOffsetAtBegin().getSI(), getDesignLineOffsetAtEnd().getSI() });
        }
        else
        {
            double[] fractions = new double[this.crossSectionSlices.size()];
            double[] offsets = new double[this.crossSectionSlices.size()];
            for (int i = 0; i < this.crossSectionSlices.size(); i++)
            {
                fractions[i] = this.crossSectionSlices.get(i).getRelativeLength().si / this.parentLink.getLength().si;
                offsets[i] = this.crossSectionSlices.get(i).getDesignLineOffset().si;
            }
            proposedCenterLine = fixTightInnerCurve(fractions, offsets);
        }
        // Make positions and directions of begin and end of CrossSection exact
        List<OTSPoint3D> points = new ArrayList<OTSPoint3D>(Arrays.asList(proposedCenterLine.getPoints()));
        // Make position at begin exact
        DirectedPoint linkFrom = Try.assign(() -> parentLink.getStartNode().getLocation(), "Cannot happen");
        double fromDirection = linkFrom.getRotZ();
        points.remove(0);
        points.add(0, new OTSPoint3D(linkFrom.x + getDesignLineOffsetAtBegin().getSI() * Math.cos(fromDirection + Math.PI / 2),
                linkFrom.y + getDesignLineOffsetAtBegin().getSI() * Math.sin(fromDirection + Math.PI / 2)));
        // Make position at end exact
        DirectedPoint linkTo = Try.assign(() -> parentLink.getEndNode().getLocation(), "Cannot happen");
        double toDirection = linkTo.getRotZ();
        points.remove(points.size() - 1);
        points.add(new OTSPoint3D(linkTo.x + getDesignLineOffsetAtEnd().getSI() * Math.cos(toDirection + Math.PI / 2),
                linkTo.y + getDesignLineOffsetAtEnd().getSI() * Math.sin(toDirection + Math.PI / 2)));
        // Check direction at begin
        double direction = points.get(0).horizontalDirectionSI(points.get(1));
        OTSPoint3D extraPointAfterStart = null;
        if (Math.abs(direction - fromDirection) > MAXIMUMDIRECTIONERROR)
        {
            // Insert an extra point to ensure that the new CrossSectionElement starts off in the right direction
            OTSPoint3D from = points.get(0);
            OTSPoint3D next = points.get(1);
            double distance =
                    Math.min(from.horizontalDistanceSI(next) * FIXUPPOINTPROPORTION, crossSectionSlices.get(0).getWidth().si);
            extraPointAfterStart = new OTSPoint3D(from.x + Math.cos(fromDirection) * distance,
                    from.y + Math.sin(fromDirection) * distance, from.z + FIXUPPOINTPROPORTION * (next.z - from.z));
            // Do not insert it yet because that could cause a similar point near the end to be put at the wrong distance
        }
        // Check direction at end
        int pointCount = points.size();
        direction = points.get(pointCount - 2).horizontalDirectionSI(points.get(pointCount - 1));
        if (Math.abs(direction - toDirection) > MAXIMUMDIRECTIONERROR)
        {
            // Insert an extra point to ensure that the new CrossSectionElement ends in the right direction
            OTSPoint3D to = points.get(pointCount - 1);
            OTSPoint3D before = points.get(pointCount - 2);
            double distance = Math.min(before.horizontalDistanceSI(to) * FIXUPPOINTPROPORTION,
                    crossSectionSlices.get(Math.max(0, crossSectionSlices.size() - 2)).getWidth().si);
            points.add(pointCount - 1, new OTSPoint3D(to.x - Math.cos(toDirection) * distance,
                    to.y - Math.sin(toDirection) * distance, to.z - FIXUPPOINTPROPORTION * (before.z - to.z)));
        }
        if (null != extraPointAfterStart)
        {
            points.add(1, extraPointAfterStart);
        }
        this.centerLine = new OTSLine3D(points);
        this.length = this.centerLine.getLength();
        this.contour = constructContour(this);
        this.parentLink.addCrossSectionElement(this);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrossSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; width at start, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; width at end, positioned <i>symmetrically around</i> the design line
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtBegin,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth,
            final boolean fixGradualLateralOffset) throws OTSGeometryException, NetworkException
    {
        this(parentLink, id, fixLateralOffset(parentLink, lateralOffsetAtBegin, lateralOffsetAtEnd, beginWidth, endWidth,
                fixGradualLateralOffset));
    }

    /**
     * Construct a list of cross section slices, using sinusoidal interpolation for changing lateral offset.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; width at start, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; width at end, positioned <i>symmetrically around</i> the design line
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @return List&ltCrossSectionSlice&gt;; the cross section slices
     */
    private static List<CrossSectionSlice> fixLateralOffset(final CrossSectionLink parentLink,
            final Length lateralOffsetAtBegin, final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth,
            final boolean fixGradualLateralOffset)
    {
        List<CrossSectionSlice> result = new ArrayList<>();
        int numPoints = !fixGradualLateralOffset ? 2 : lateralOffsetAtBegin.equals(lateralOffsetAtEnd) ? 2 : 16;
        Length parentLength = parentLink.getLength();
        for (int index = 0; index < numPoints; index++)
        {
            double fraction = index * 1.0 / (numPoints - 1);
            Length lengthAtCrossSection = parentLength.times(fraction);
            double relativeOffsetAtFraction = (1 + Math.sin((fraction - 0.5) * Math.PI)) / 2;
            Length offsetAtFraction = Length.interpolate(lateralOffsetAtBegin, lateralOffsetAtEnd, relativeOffsetAtFraction);
            result.add(new CrossSectionSlice(lengthAtCrossSection, offsetAtFraction,
                    Length.interpolate(beginWidth, endWidth, fraction)));
        }
        return result;
    }

    /**
     * Returns the center line for this cross section element by adhering to the given offsets relative to the link design line.
     * This method will create a Bezier curve, ignoring the link design line, if the offset at any vertex is larger than the
     * radius, and on the inside of the curve.
     * @param fractions double[]; length fractions of offsets
     * @param offsets double[]; offsets
     * @return OTSPoint3D; center line
     * @throws OTSGeometryException index out of bounds
     */
    private OTSLine3D fixTightInnerCurve(final double[] fractions, final double[] offsets) throws OTSGeometryException
    {
        OTSLine3D linkCenterLine = getParentLink().getDesignLine();
        for (int i = 1; i < linkCenterLine.size() - 1; i++)
        {
            double fraction = linkCenterLine.getVertexFraction(i);
            int index = 0;
            while (index < fractions.length - 2 && fraction > fractions[index + 1])
            {
                index++;
            }
            double w = (fraction - fractions[index]) / (fractions[index + 1] - fractions[index]);
            double offset = (1.0 - w) * offsets[index] + w * offsets[index + 1];
            double radius = 1.0;
            try
            {
                radius = linkCenterLine.getProjectedVertexRadius(i).si;
            }
            catch (Exception e)
            {
                CategoryLogger.always().error(e, "fixTightInnerCurve.getVertexFraction for " + linkCenterLine);
            }
            if ((!Double.isNaN(radius))
                    && ((radius < 0.0 && offset < 0.0 && offset < radius) || (radius > 0.0 && offset > 0.0 && offset > radius)))
            {
                double offsetStart = getDesignLineOffsetAtBegin().getSI();
                double offsetEnd = getDesignLineOffsetAtEnd().getSI();
                DirectedPoint start = linkCenterLine.getLocationFraction(0.0);
                DirectedPoint end = linkCenterLine.getLocationFraction(1.0);
                start = new DirectedPoint(start.x - Math.sin(start.getRotZ()) * offsetStart,
                        start.y + Math.cos(start.getRotZ()) * offsetStart, start.z, start.getRotX(), start.getRotY(),
                        start.getRotZ());
                end = new DirectedPoint(end.x - Math.sin(end.getRotZ()) * offsetEnd,
                        end.y + Math.cos(end.getRotZ()) * offsetEnd, end.z, end.getRotX(), end.getRotY(), end.getRotZ());
                while (this.crossSectionSlices.size() > 2)
                {
                    this.crossSectionSlices.remove(1);
                }
                return Bezier.cubic(start, end);
            }
        }
        if (this.crossSectionSlices.size() <= 2)
        {
            OTSLine3D designLine = this.getParentLink().getDesignLine();
            if (designLine.size() > 2)
            {
                // TODO: this produces near-duplicate points on lane 925_J1.FORWARD1 in the Aimsun network
                // hack: clean nearby points
                OTSLine3D line =
                        designLine.offsetLine(getDesignLineOffsetAtBegin().getSI(), getDesignLineOffsetAtEnd().getSI());
                List<OTSPoint3D> points = new ArrayList<>(Arrays.asList(line.getPoints()));
                Iterator<OTSPoint3D> it = points.iterator();
                OTSPoint3D prevPoint = null;
                while (it.hasNext())
                {
                    OTSPoint3D point = it.next();
                    if (prevPoint != null && prevPoint.distance(point).si < 1e-4)
                    {
                        it.remove();
                    }
                    prevPoint = point;
                }
                return new OTSLine3D(points);
            }
            else
            {
                DirectedPoint refStart = getParentLink().getStartNode().getLocation();
                double startRot = refStart.getRotZ();
                double startOffset = this.crossSectionSlices.get(0).getDesignLineOffset().si;
                OTSPoint3D start = new OTSPoint3D(refStart.x - Math.sin(startRot) * startOffset,
                        refStart.y + Math.cos(startRot) * startOffset, refStart.z);
                DirectedPoint refEnd = getParentLink().getEndNode().getLocation();
                double endRot = refEnd.getRotZ();
                double endOffset = this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getDesignLineOffset().si;
                OTSPoint3D end = new OTSPoint3D(refEnd.x - Math.sin(endRot) * endOffset,
                        refEnd.y + Math.cos(endRot) * endOffset, refEnd.z);
                return new OTSLine3D(start, end);
            }
        }
        else
        {
            for (int i = 0; i < this.crossSectionSlices.size(); i++)
            {
                fractions[i] = this.crossSectionSlices.get(i).getRelativeLength().si / this.parentLink.getLength().si;
                offsets[i] = this.crossSectionSlices.get(i).getDesignLineOffset().si;
            }
            return this.getParentLink().getDesignLine().offsetLine(fractions, offsets);
        }
    }

    /**
     * Clone a CrossSectionElement for a new network.
     * @param newCrossSectionLink CrossSectionLink; the new link to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @param cse CrossSectionElement; the element to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected CrossSectionElement(final CrossSectionLink newCrossSectionLink,
            final OTSSimulatorInterface newSimulator, final CrossSectionElement cse) throws NetworkException
    {
        this.id = cse.id;
        this.parentLink = newCrossSectionLink;
        this.centerLine = cse.centerLine;
        this.length = this.centerLine.getLength();
        this.contour = cse.contour;
        this.crossSectionSlices = new ArrayList<>(cse.crossSectionSlices);
        newCrossSectionLink.addCrossSectionElement(this);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrosssSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id, final Length lateralOffset,
            final Length width) throws OTSGeometryException, NetworkException
    {
        this(parentLink, id,
                Arrays.asList(new CrossSectionSlice[] { new CrossSectionSlice(Length.ZERO, lateralOffset, width) }));
    }

    /**
     * @return parentLink.
     */
    public final CrossSectionLink getParentLink()
    {
        return this.parentLink;
    }

    /**
     * @return the road network to which the lane belongs
     */
    public final RoadNetwork getNetwork()
    {
        return this.parentLink.getNetwork();
    }

    /**
     * Calculate the slice the fractional position is in.
     * @param fractionalPosition double; the fractional position between 0 and 1 compared to the design line
     * @return int; the lower slice number between 0 and number of slices - 1.
     */
    private int calculateSliceNumber(final double fractionalPosition)
    {
        double linkLength = this.parentLink.getLength().si;
        for (int i = 0; i < this.crossSectionSlices.size() - 1; i++)
        {
            if (fractionalPosition >= this.crossSectionSlices.get(i).getRelativeLength().si / linkLength
                    && fractionalPosition <= this.crossSectionSlices.get(i + 1).getRelativeLength().si / linkLength)
            {
                return i;
            }
        }
        return this.crossSectionSlices.size() - 2;
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition double; fractional longitudinal position on this Lane
     * @return Length; the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final double fractionalPosition)
    {
        if (this.crossSectionSlices.size() == 1)
        {
            return this.getDesignLineOffsetAtBegin();
        }
        if (this.crossSectionSlices.size() == 2)
        {
            return Length.interpolate(this.getDesignLineOffsetAtBegin(), this.getDesignLineOffsetAtEnd(), fractionalPosition);
        }
        int sliceNr = calculateSliceNumber(fractionalPosition);
        return Length.interpolate(this.crossSectionSlices.get(sliceNr).getDesignLineOffset(),
                this.crossSectionSlices.get(sliceNr + 1).getDesignLineOffset(), fractionalPosition
                        - this.crossSectionSlices.get(sliceNr).getRelativeLength().si / this.parentLink.getLength().si);
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition Length; the longitudinal position on this Lane
     * @return Length; the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final Length longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition Length; the longitudinal position
     * @return Length; the width of this CrossSectionElement at the specified longitudinal position.
     */
    public final Length getWidth(final Length longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return Length; the width of this CrossSectionElement at the specified fractional longitudinal position.
     */
    public final Length getWidth(final double fractionalPosition)
    {
        if (this.crossSectionSlices.size() == 1)
        {
            return this.getBeginWidth();
        }
        if (this.crossSectionSlices.size() == 2)
        {
            return Length.interpolate(this.getBeginWidth(), this.getEndWidth(), fractionalPosition);
        }
        int sliceNr = calculateSliceNumber(fractionalPosition);
        return Length.interpolate(this.crossSectionSlices.get(sliceNr).getWidth(),
                this.crossSectionSlices.get(sliceNr + 1).getWidth(), fractionalPosition
                        - this.crossSectionSlices.get(sliceNr).getRelativeLength().si / this.parentLink.getLength().si);
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return Length; the length of this CrossSectionElement
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * Retrieve the offset from the design line at the begin of the parent link.
     * @return Length; the offset of this CrossSectionElement at the begin of the parent link
     */
    public final Length getDesignLineOffsetAtBegin()
    {
        return this.crossSectionSlices.get(0).getDesignLineOffset();
    }

    /**
     * Retrieve the offset from the design line at the end of the parent link.
     * @return Length; the offset of this CrossSectionElement at the end of the parent link
     */
    public final Length getDesignLineOffsetAtEnd()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getDesignLineOffset();
    }

    /**
     * Retrieve the width at the begin of the parent link.
     * @return Length; the width of this CrossSectionElement at the begin of the parent link
     */
    public final Length getBeginWidth()
    {
        return this.crossSectionSlices.get(0).getWidth();
    }

    /**
     * Retrieve the width at the end of the parent link.
     * @return Length; the width of this CrossSectionElement at the end of the parent link
     */
    public final Length getEndWidth()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getWidth();
    }

    /**
     * Retrieve the Z offset (used to determine what covers what when drawing).
     * @return double; the Z-offset for drawing (what's on top, what's underneath).
     */
    @Override
    public abstract double getZ();

    /**
     * Retrieve the center line of this CrossSectionElement.
     * @return OTSLine3D; the center line of this CrossSectionElement
     */
    public final OTSLine3D getCenterLine()
    {
        return this.centerLine;
    }

    /**
     * Retrieve the contour of this CrossSectionElement.
     * @return OTSShape; the contour of this CrossSectionElement
     */
    public final OTSShape getContour()
    {
        return this.contour;
    }

    /**
     * Retrieve the id of this CrossSectionElement.
     * @return String; the id of this CrossSectionElement
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the id of this CrossSectionElement.
     * @return String; the id of this CrossSectionElement
     */
    public final String getFullId()
    {
        return getParentLink().getId() + "." + this.id;
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return Length
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final double fractionalLongitudinalPosition)
    {
        Length designLineOffset;
        Length halfWidth;
        if (this.crossSectionSlices.size() <= 2)
        {
            designLineOffset = Length.interpolate(getDesignLineOffsetAtBegin(), getDesignLineOffsetAtEnd(),
                    fractionalLongitudinalPosition);
            halfWidth = Length.interpolate(getBeginWidth(), getEndWidth(), fractionalLongitudinalPosition).times(0.5);
        }
        else
        {
            int sliceNr = calculateSliceNumber(fractionalLongitudinalPosition);
            double startFractionalPosition =
                    this.crossSectionSlices.get(sliceNr).getRelativeLength().si / this.parentLink.getLength().si;
            designLineOffset = Length.interpolate(this.crossSectionSlices.get(sliceNr).getDesignLineOffset(),
                    this.crossSectionSlices.get(sliceNr + 1).getDesignLineOffset(),
                    fractionalLongitudinalPosition - startFractionalPosition);
            halfWidth = Length.interpolate(this.crossSectionSlices.get(sliceNr).getWidth(),
                    this.crossSectionSlices.get(sliceNr + 1).getWidth(),
                    fractionalLongitudinalPosition - startFractionalPosition).times(0.5);
        }

        switch (lateralDirection)
        {
            case LEFT:
                return designLineOffset.minus(halfWidth);
            case RIGHT:
                return designLineOffset.plus(halfWidth);
            default:
                throw new Error("Bad switch on LateralDirectionality " + lateralDirection);
        }
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param longitudinalPosition Length; the position along the length of this CrossSectionElement
     * @return Length
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final Length longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Construct a buffer geometry by offsetting the linear geometry line with a distance and constructing a so-called "buffer"
     * around it.
     * @param cse CrossSectionElement; the cross section element to construct the contour for
     * @return OTSShape; the geometry belonging to this CrossSectionElement.
     * @throws OTSGeometryException when construction of the geometry fails
     * @throws NetworkException when the resulting contour is degenerate (cannot happen; we hope)
     */
    public static OTSShape constructContour(final CrossSectionElement cse) throws OTSGeometryException, NetworkException
    {
        OTSPoint3D[] result = null;

        if (cse.crossSectionSlices.size() <= 2)
        {
            OTSLine3D crossSectionDesignLine = cse.centerLine;
            OTSLine3D rightBoundary =
                    crossSectionDesignLine.offsetLine(-cse.getBeginWidth().getSI() / 2, -cse.getEndWidth().getSI() / 2);
            OTSLine3D leftBoundary =
                    crossSectionDesignLine.offsetLine(cse.getBeginWidth().getSI() / 2, cse.getEndWidth().getSI() / 2);
            result = new OTSPoint3D[rightBoundary.size() + leftBoundary.size() + 1];
            int resultIndex = 0;
            for (int index = 0; index < rightBoundary.size(); index++)
            {
                result[resultIndex++] = rightBoundary.get(index);
            }
            for (int index = leftBoundary.size(); --index >= 0;)
            {
                result[resultIndex++] = leftBoundary.get(index);
            }
            result[resultIndex] = rightBoundary.get(0); // close the contour
        }
        else
        {
            List<OTSPoint3D> resultList = new ArrayList<>();
            List<OTSPoint3D> rightBoundary = new ArrayList<>();
            for (int i = 0; i < cse.crossSectionSlices.size() - 1; i++)
            {
                double plLength = cse.getParentLink().getLength().si;
                double so = cse.crossSectionSlices.get(i).getDesignLineOffset().si;
                double eo = cse.crossSectionSlices.get(i + 1).getDesignLineOffset().si;
                double sw2 = cse.crossSectionSlices.get(i).getWidth().si / 2.0;
                double ew2 = cse.crossSectionSlices.get(i + 1).getWidth().si / 2.0;
                double sf = cse.crossSectionSlices.get(i).getRelativeLength().si / plLength;
                double ef = cse.crossSectionSlices.get(i + 1).getRelativeLength().si / plLength;
                OTSLine3D crossSectionDesignLine =
                        cse.getParentLink().getDesignLine().extractFractional(sf, ef).offsetLine(so, eo);
                resultList.addAll(Arrays.asList(crossSectionDesignLine.offsetLine(-sw2, -ew2).getPoints()));
                rightBoundary.addAll(Arrays.asList(crossSectionDesignLine.offsetLine(sw2, ew2).getPoints()));
            }
            for (int index = rightBoundary.size(); --index >= 0;)
            {
                resultList.add(rightBoundary.get(index));
            }
            // close the contour (might not be needed)
            resultList.add(resultList.get(0));
            result = resultList.toArray(new OTSPoint3D[] {});
        }
        return OTSShape.createAndCleanOTSShape(result);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        DirectedPoint centroid = this.contour.getLocation();
        return new DirectedPoint(centroid.x, centroid.y, getZ());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return this.contour.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return this; // TODO: for now, lane object is returned as source. See if this can be an id / simple object
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("CSE offset %.2fm..%.2fm, width %.2fm..%.2fm", getDesignLineOffsetAtBegin().getSI(),
                getDesignLineOffsetAtEnd().getSI(), getBeginWidth().getSI(), getEndWidth().getSI());
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
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
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

    /**
     * Clone the CrossSectionElement for e.g., copying a network.
     * @param newParentLink CrossSectionLink; the new link to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public abstract CrossSectionElement clone(CrossSectionLink newParentLink, OTSSimulatorInterface newSimulator)
            throws NetworkException;
}
