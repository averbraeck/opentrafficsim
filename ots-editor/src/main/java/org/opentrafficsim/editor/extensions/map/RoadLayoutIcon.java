package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.Transform2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.Straight2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.eval.Eval;
import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.animation.road.CrossSectionElementAnimation;
import org.opentrafficsim.animation.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.animation.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.animation.road.LaneAnimation;
import org.opentrafficsim.animation.road.LaneAnimation.LaneData;
import org.opentrafficsim.animation.road.StripeAnimation;
import org.opentrafficsim.animation.road.StripeAnimation.StripeData;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.core.geometry.CurveFlattener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.CrossSectionGeometry;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * Renders an icon that represents a defined road layout. The icon is drawn with the same animations that are used for the map;
 * only the geometry is a short, vertical, straight link, scaled to icon size.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class RoadLayoutIcon
{

    /** Icon width in pixels. */
    private static final int WIDTH = 50;

    /** Icon height in pixels. */
    private static final int HEIGHT = 16;

    /** Default length of the synthetic link in meters when no dashed stripe pattern is present. */
    private static final double DEFAULT_LINK_LENGTH = 30.0;

    /** Lateral scale in pixels per meter. */
    private static final double LATERAL_SCALE = 2.0;

    /** Shortest dash pattern that is drawn on a 10m link. */
    private static final double SHORT_PATTERN_LENGTH = 2.0;

    /** Longest dash pattern that is drawn on a 30m link. */
    private static final double LONG_PATTERN_LENGTH = 12.0;

    /** Link length in meters for a short dash pattern. */
    private static final double SHORT_LINK_LENGTH = 10.0;

    /** Link length in meters for a long dash pattern. */
    private static final double LONG_LINK_LENGTH = 30.0;

    /** Direction of the synthetic link; straight up. */
    private static final Direction UP = Direction.ofSI(Math.PI / 2.0);

    /** Direction of the relative cross-section axis used by the animations. */
    private static final Direction EAST = Direction.ofSI(0.0);

    /** Background color of the icon. */
    private static final Color BACKGROUND = new Color(0x606060);

    /** Transform from world coordinates to coordinates relative to the synthetic design line. */
    private static final Transform2d DESIGN_LINE_TO_RELATIVE = new Transform2d().rotation(-UP.si);

    /**
     * Constructor.
     */
    private RoadLayoutIcon()
    {
        //
    }

    /**
     * Creates an icon for the given road layout data. The data is obtained from a {@link RoadLayoutListener}, which uses
     * {@code RoadLayoutOffsets.calculateOffsets()} to determine the effective offsets of the individual elements.
     * @param cseData cross-section data of the road layout
     * @param eval expression evaluator
     * @return icon, or {@code null} if the layout cannot be rendered
     */
    static Icon create(final Map<XsdTreeNode, CseData> cseData, final Eval eval)
    {
        if (cseData == null || cseData.isEmpty())
        {
            return null;
        }
        Map<XsdTreeNode, StripeInfo> stripeInfos = new LinkedHashMap<>();
        double patternLength = gatherStripeInfosAndLength(cseData, eval, stripeInfos);
        double linkLength = getLinkLength(patternLength);
        double longitudinalScale = HEIGHT / linkLength;
        double verticalCenter = HEIGHT - longitudinalScale * linkLength / 2.0;
        double lateralScale = LATERAL_SCALE;
        LateralBounds bounds = new LateralBounds(0.0, 0.0);
        // Fixed-point iteration: s = WIDTH / bounds(s); stripe widths depend on s through the 1.5 px minimum.
        for (int iteration = 0; iteration < 8; iteration++)
        {
            bounds = getLateralBounds(cseData, stripeInfos, lateralScale);
            double nextScale = getLateralScale(bounds);
            if (Math.abs(nextScale - lateralScale) < 1e-6)
            {
                lateralScale = nextScale;
                break;
            }
            lateralScale = nextScale;
        }
        bounds = getLateralBounds(cseData, stripeInfos, lateralScale);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        OffsetCurve2d designLine = new Straight2d(new DirectedPoint2d(0.0, -linkLength / 2.0, UP.si), linkLength);
        CurveFlattener flattener = new CurveFlattener(64);
        ContextInterface context = new JvmContext("road-layout-icon");
        Contextualized contextualized = () -> context;
        List<Renderable2d<?>> animations = new ArrayList<>();
        List<Renderable2d<?>> stripes = new ArrayList<>();
        try
        {
            for (Entry<XsdTreeNode, CseData> entry : cseData.entrySet())
            {
                XsdTreeNode node = entry.getKey();
                CseData data = entry.getValue();
                if (data.centerOffsetStart == null || data.centerOffsetEnd == null || data.widthStart == null
                        || data.widthEnd == null)
                {
                    continue;
                }
                ContinuousPiecewiseLinearFunction offsetFunc =
                        ContinuousPiecewiseLinearFunction.of(0.0, data.centerOffsetStart.si, 1.0, data.centerOffsetEnd.si);
                Renderable2d<?> animation;
                switch (node.getNodeName())
                {
                    case "Stripe":
                    {
                        animation = createStripe(node, designLine, flattener, offsetFunc, stripeInfos.get(node), lateralScale,
                                linkLength, contextualized);
                        break;
                    }
                    case "Lane":
                    {
                        ContinuousPiecewiseLinearFunction widthFunc =
                                ContinuousPiecewiseLinearFunction.of(0.0, data.widthStart.si, 1.0, data.widthEnd.si);
                        CrossSectionGeometry geometry = CrossSectionGeometry.of(designLine, flattener, offsetFunc, widthFunc);
                        animation = new LaneAnimation(new IconLaneData(node.getId(), geometry), contextualized,
                                Color.GRAY.brighter());
                        break;
                    }
                    case "Shoulder":
                    {
                        ContinuousPiecewiseLinearFunction widthFunc =
                                ContinuousPiecewiseLinearFunction.of(0.0, data.widthStart.si, 1.0, data.widthEnd.si);
                        CrossSectionGeometry geometry = CrossSectionGeometry.of(designLine, flattener, offsetFunc, widthFunc);
                        animation = new CrossSectionElementAnimation<>(new IconShoulderData(node.getId(), geometry),
                                contextualized, Color.DARK_GRAY);
                        break;
                    }
                    case "NoTrafficLane":
                    {
                        ContinuousPiecewiseLinearFunction widthFunc =
                                ContinuousPiecewiseLinearFunction.of(0.0, data.widthStart.si, 1.0, data.widthEnd.si);
                        CrossSectionGeometry geometry = CrossSectionGeometry.of(designLine, flattener, offsetFunc, widthFunc);
                        animation = new CrossSectionElementAnimation<>(new IconCrossSectionData(node.getId(), geometry),
                                contextualized, Color.DARK_GRAY);
                        break;
                    }
                    default:
                    {
                        continue;
                    }
                }
                if (animation != null)
                {
                    if (node.getNodeName().equals("Stripe"))
                    {
                        stripes.add(animation);
                    }
                    else
                    {
                        animations.add(animation);
                    }
                }
            }
            if (animations.isEmpty() && stripes.isEmpty())
            {
                return null;
            }
            Graphics2D graphics = image.createGraphics();
            try
            {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, WIDTH, HEIGHT);
                graphics.translate(WIDTH / 2.0 + lateralScale * bounds.center(), verticalCenter);
                graphics.scale(lateralScale, longitudinalScale);
                graphics.rotate(-Math.PI / 2.0);
                for (Renderable2d<?> animation : animations)
                {
                    animation.paint(graphics, null);
                }
                for (Renderable2d<?> stripe : stripes)
                {
                    stripe.paint(graphics, null);
                }
            }
            finally
            {
                graphics.dispose();
            }
            return new IconUtil.InterpolatedImageIcon(image);
        }
        catch (RuntimeException exception)
        {
            return null;
        }
        finally
        {
            for (Renderable2d<?> animation : animations)
            {
                animation.destroy(contextualized);
            }
            for (Renderable2d<?> stripe : stripes)
            {
                stripe.destroy(contextualized);
            }
        }
    }

    /**
     * Gather stripe info and return the pattern length.
     * @param cseData cross-section element data
     * @param eval eval
     * @param stripeInfos map to place stripe info's in
     * @return length of pattern
     */
    private static double gatherStripeInfosAndLength(final Map<XsdTreeNode, CseData> cseData, final Eval eval,
            final Map<XsdTreeNode, StripeInfo> stripeInfos)
    {
        double patternLength = 0.0;
        for (XsdTreeNode node : cseData.keySet())
        {
            StripeInfo info = getStripeInfo(node, eval);
            stripeInfos.put(node, info);
            for (StripeElement element : info.elements())
            {
                if (element.dashes() != null)
                {
                    double sum = 0.0;
                    for (double value : element.dashes().getValuesSI())
                    {
                        sum += value;
                    }
                    patternLength = Math.max(patternLength, sum);
                }
            }
        }
        return patternLength;
    }

    /**
     * Returns the link length for the largest dash pattern on the layout. A 2m pattern is drawn on a 10m link, a 12m pattern on
     * a 30m link, with linear interpolation in between.
     * @param patternLength largest dash pattern length in meters
     * @return link length in meters
     */
    private static double getLinkLength(final double patternLength)
    {
        if (patternLength <= 0.0)
        {
            return DEFAULT_LINK_LENGTH;
        }
        if (patternLength <= SHORT_PATTERN_LENGTH)
        {
            return SHORT_LINK_LENGTH;
        }
        if (patternLength >= LONG_PATTERN_LENGTH)
        {
            return LONG_LINK_LENGTH;
        }
        return SHORT_LINK_LENGTH + (patternLength - SHORT_PATTERN_LENGTH) / (LONG_PATTERN_LENGTH - SHORT_PATTERN_LENGTH)
                * (LONG_LINK_LENGTH - SHORT_LINK_LENGTH);
    }

    /**
     * Returns the stripe definition for a stripe node.
     * @param node stripe node
     * @param eval expression evaluator
     * @return stripe definition, or an empty definition if it cannot be resolved
     */
    private static StripeInfo getStripeInfo(final XsdTreeNode node, final Eval eval)
    {
        if (!node.getNodeName().equals("Stripe"))
        {
            return new StripeInfo(List.of(), Length.ZERO);
        }
        XsdTreeNode stripeNode = MapStripeDefinitionUtil.resolveStripeNode(node);
        if (stripeNode == null)
        {
            return new StripeInfo(List.of(), Length.ZERO);
        }
        StripeDefinition stripeData = MapStripeDefinitionUtil.collectStripeData(stripeNode, null, eval);
        return new StripeInfo(MapStripeDefinitionUtil.createStripeElements(stripeData.elementNodes(), eval),
                stripeData.dashOffset());
    }

    /**
     * Returns the lateral bounds of the cross-section, so that only relative offsets determine the icon content.
     * @param cseData cross-section data of the road layout
     * @param stripeInfos stripe definitions of the road layout
     * @param lateralScale lateral scale in pixels per meter
     * @return lateral bounds of the cross-section in meters
     */
    private static LateralBounds getLateralBounds(final Map<XsdTreeNode, CseData> cseData,
            final Map<XsdTreeNode, StripeInfo> stripeInfos, final double lateralScale)
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Entry<XsdTreeNode, CseData> entry : cseData.entrySet())
        {
            XsdTreeNode node = entry.getKey();
            CseData data = entry.getValue();
            if (data.centerOffsetStart == null || data.centerOffsetEnd == null)
            {
                continue;
            }
            double halfWidthStart;
            double halfWidthEnd;
            if (node.getNodeName().equals("Stripe"))
            {
                double width = 0.0;
                for (StripeElement element : stripeInfos.get(node).elements())
                {
                    width += getStripeModelWidth(element, lateralScale);
                }
                halfWidthStart = 0.5 * width;
                halfWidthEnd = halfWidthStart;
            }
            else
            {
                if (data.widthStart == null || data.widthEnd == null)
                {
                    continue;
                }
                halfWidthStart = 0.5 * data.widthStart.si;
                halfWidthEnd = 0.5 * data.widthEnd.si;
            }
            min = Math.min(min, data.centerOffsetStart.si - halfWidthStart);
            max = Math.max(max, data.centerOffsetStart.si + halfWidthStart);
            min = Math.min(min, data.centerOffsetEnd.si - halfWidthEnd);
            max = Math.max(max, data.centerOffsetEnd.si + halfWidthEnd);
        }
        return min <= max ? new LateralBounds(min, max) : new LateralBounds(0.0, 0.0);
    }

    /**
     * Returns the lateral scale to use for an icon.
     * @param bounds lateral bounds of the cross-section
     * @return lateral scale in pixels per meter
     */
    private static double getLateralScale(final LateralBounds bounds)
    {
        return bounds.width() > 0.0 ? Math.min(LATERAL_SCALE, WIDTH / bounds.width()) : LATERAL_SCALE;
    }

    /**
     * Returns the width of a stripe element in model meters. The painted width does not follow the lateral scale of the rest of
     * the icon; it is at least 1.5 pixels, and otherwise uses the width at the default lateral scale.
     * @param element stripe element
     * @param lateralScale lateral scale in pixels per meter
     * @return width of the stripe element in model meters
     */
    private static double getStripeModelWidth(final StripeElement element, final double lateralScale)
    {
        return Math.max(1.5, element.width().si * LATERAL_SCALE) / lateralScale;
    }

    /**
     * Stripe definition for an icon.
     * @param elements stripe elements
     * @param dashOffset dash offset
     */
    private record StripeInfo(List<StripeElement> elements, Length dashOffset)
    {
    }

    /**
     * Lateral bounds of a cross-section in meters.
     * @param min left-most edge
     * @param max right-most edge
     */
    private record LateralBounds(double min, double max)
    {
        /**
         * Returns the center of the bounds.
         * @return center of the bounds
         */
        double center()
        {
            return 0.5 * (this.min + this.max);
        }

        /**
         * Returns the width of the bounds.
         * @return width of the bounds
         */
        double width()
        {
            return this.max - this.min;
        }
    }

    /**
     * Creates a stripe animation with a minimum painted width of 1.5 pixels.
     * @param node stripe node
     * @param designLine synthetic design line
     * @param flattener flattener
     * @param offsetFunc offset function
     * @param stripeInfo stripe definition
     * @param lateralScale lateral scale in pixels per meter
     * @param linkLength synthetic link length in meters
     * @param contextualized context provider
     * @return stripe animation, or {@code null} if the stripe definition is empty
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Renderable2d<?> createStripe(final XsdTreeNode node, final OffsetCurve2d designLine,
            final CurveFlattener flattener, final ContinuousPiecewiseLinearFunction offsetFunc, final StripeInfo stripeInfo,
            final double lateralScale, final double linkLength, final Contextualized contextualized)
    {
        List<StripeElement> elements = new ArrayList<>();
        double width = 0.0;
        for (StripeElement element : stripeInfo.elements())
        {
            double elementWidth = getStripeModelWidth(element, lateralScale);
            width += elementWidth;
            Length modelWidth = Length.ofSI(elementWidth);
            if (element.isContinuous())
            {
                elements.add(StripeElement.continuous(modelWidth, element.color()));
            }
            else if (element.isGap())
            {
                elements.add(StripeElement.gap(modelWidth));
            }
            else
            {
                elements.add(StripeElement.dashed(modelWidth, element.color(), element.dashes()));
            }
        }
        if (elements.isEmpty())
        {
            return null;
        }
        ContinuousPiecewiseLinearFunction widthFunc = ContinuousPiecewiseLinearFunction.of(0.0, width, 1.0, width);
        CrossSectionGeometry geometry = CrossSectionGeometry.of(designLine, flattener, offsetFunc, widthFunc);
        return new StripeAnimation(
                new IconStripeData(node.getId(), geometry, elements, stripeInfo.dashOffset(), linkLength, width),
                contextualized);
    }

    /**
     * Cross-section data for an icon, based on the geometry of a synthetic link.
     */
    private static class IconCrossSectionData implements CrossSectionElementData
    {

        /** Id of the element. */
        private final String id;

        /** Geometry. */
        private final CrossSectionGeometry geometry;

        /**
         * Constructor.
         * @param id id of the element
         * @param geometry geometry
         */
        IconCrossSectionData(final String id, final CrossSectionGeometry geometry)
        {
            this.id = id;
            this.geometry = geometry;
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return this.geometry.getLocation();
        }

        @Override
        public Polygon2d getAbsoluteContour()
        {
            return this.geometry.absoluteContour();
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            return new Polygon2d(0.0, DESIGN_LINE_TO_RELATIVE.transform(this.geometry.absoluteContour().iterator()));
        }

        @Override
        public PolyLine2d getCenterLine()
        {
            return this.geometry.centerLine();
        }

        @Override
        public String getLinkId()
        {
            return this.id;
        }

    }

    /**
     * Lane data for an icon.
     */
    private static final class IconLaneData extends IconCrossSectionData implements LaneData
    {

        /** Id of the lane. */
        private final String id;

        /**
         * Constructor.
         * @param id id of the lane
         * @param geometry geometry
         */
        IconLaneData(final String id, final CrossSectionGeometry geometry)
        {
            super(id, geometry);
            this.id = id;
        }

        @Override
        public String getId()
        {
            return this.id;
        }

        @Override
        public boolean hasNextLane()
        {
            return false;
        }

        @Override
        public boolean hasPrevLane()
        {
            return false;
        }

    }

    /**
     * Shoulder data for an icon.
     */
    private static final class IconShoulderData extends IconCrossSectionData implements ShoulderData
    {

        /**
         * Constructor.
         * @param id id of the shoulder
         * @param geometry geometry
         */
        IconShoulderData(final String id, final CrossSectionGeometry geometry)
        {
            super(id, geometry);
        }

    }

    /**
     * Stripe data for an icon.
     */
    private static final class IconStripeData extends IconCrossSectionData implements StripeData
    {

        /** Geometry of the stripe. */
        private final CrossSectionGeometry geometry;

        /** Center line with directions. */
        private final DirectionalPolyLine centerLine;

        /** Stripe elements. */
        private final List<StripeElement> elements;

        /** Dash offset. */
        private final Length dashOffset;

        /** Total stripe width. */
        private final Length width;

        /**
         * Constructor.
         * @param id id of the stripe
         * @param geometry geometry
         * @param elements stripe elements
         * @param dashOffset dash offset
         * @param linkLength synthetic link length in meters
         * @param width total stripe width
         */
        IconStripeData(final String id, final CrossSectionGeometry geometry, final List<StripeElement> elements,
                final Length dashOffset, final double linkLength, final double width)
        {
            super(id, geometry);
            this.geometry = geometry;
            double offsetStart = this.geometry.offset().get(0.0);
            double offsetEnd = this.geometry.offset().get(1.0);
            Direction direction = Direction.ofSI(Math.atan2(offsetEnd - offsetStart, linkLength));
            PolyLine2d line =
                    new PolyLine2d(new Point2d(-linkLength / 2.0, offsetStart), new Point2d(linkLength / 2.0, offsetEnd));
            this.centerLine = new DirectionalPolyLine(line, direction, direction);
            this.elements = elements;
            this.dashOffset = dashOffset;
            this.width = Length.ofSI(width);
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return new DirectedPoint2d(0.0, 0.0, EAST.si);
        }

        @Override
        public DirectionalPolyLine getCenterLine()
        {
            return this.centerLine;
        }

        @Override
        public PolyLine2d getReferenceLine()
        {
            return this.centerLine;
        }

        @Override
        public PolyLine2d getLine()
        {
            return this.centerLine;
        }

        @Override
        public List<StripeElement> getElements()
        {
            return this.elements;
        }

        @Override
        public Length getDashOffset()
        {
            return this.dashOffset;
        }

        @Override
        public Length getWidth(final Length position)
        {
            return this.width;
        }

        @Override
        public boolean isPartialDashAtStartSkipped()
        {
            return false;
        }

        @Override
        public boolean isPartialDashAtEndSkipped()
        {
            return false;
        }

    }

}
