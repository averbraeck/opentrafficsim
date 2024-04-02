package org.opentrafficsim.road.network.factory.xml.utils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.eval.Eval;
import org.opentrafficsim.xml.generated.BasicRoadLayout;
import org.opentrafficsim.xml.generated.CseStripe;

/**
 * Utility class to calculate offsets in a road layout. This can be used either for parsing XML, or editing.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadLayoutOffsets
{

    /**
     * Calculate the offsets for the RoadLayout. Note that offsets can be different for begin and end, and that they can be
     * specified from the right, left or center of the lane/stripe.
     * @param elements Iterable&lt;OffsetElement&gt;; offset element
     * @param cseDataList List; the list of offsets and widths for each tag, in order of definition in the
     *            RoadLayout tag
     * @param cseObjectMap Map&lt;Object, Integer&gt;; the map of the tags to the index in the list, to be able to find them
     *            quickly
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static void calculateOffsets(final Iterator<OffsetElement> elements, final List<CseData> cseDataList,
            final Map<Object, Integer> cseObjectMap)
    {
        int nr = 0;
        Length totalWidthStart = Length.ZERO;
        Length totalWidthEnd = Length.ZERO;
        boolean startOffset = false;
        boolean endOffset = false;
        while (elements.hasNext())
        {
            OffsetElement offsetElement = elements.next();
            CseData cseData = new CseData(offsetElement);
            totalWidthStart = totalWidthStart.plus(cseData.widthStart);
            totalWidthEnd = totalWidthEnd.plus(cseData.widthEnd);
            startOffset = startOffset || (offsetElement.getCenterOffsetStart() != null);
            endOffset = endOffset || (offsetElement.getCenterOffsetEnd() != null);
            cseDataList.add(cseData);
            cseObjectMap.put(offsetElement.getObject(), nr);
            nr++;
        }

        if (!startOffset)
        {
            cseDataList.get(0).centerOffsetStart = totalWidthStart.times(-0.5).minus(cseDataList.get(0).widthStart.times(-0.5));
        }
        if (!endOffset)
        {
            cseDataList.get(0).centerOffsetEnd = totalWidthEnd.times(-0.5).minus(cseDataList.get(0).widthEnd.times(-0.5));
        }

        // forward pass
        Length cs = null;
        Length es = null;
        for (CseData cseData : cseDataList)
        {
            if (cseData.centerOffsetStart != null)
            {
                cs = cseData.centerOffsetStart.plus(cseData.widthStart.times(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.plus(cseData.widthStart.times(0.5));
                    cs = cs.plus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.plus(cseData.widthEnd.times(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.plus(cseData.widthEnd.times(0.5));
                    es = es.plus(cseData.widthEnd);
                }
            }
        }

        // backward pass
        cs = null;
        es = null;
        for (int i = cseDataList.size() - 1; i >= 0; i--)
        {
            CseData cseData = cseDataList.get(i);
            if (cseData.centerOffsetStart != null)
            {
                cs = cseData.centerOffsetStart.minus(cseData.widthStart.times(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.minus(cseData.widthStart.times(0.5));
                    cs = cs.minus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.minus(cseData.widthEnd.times(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.minus(cseData.widthEnd.times(0.5));
                    es = es.minus(cseData.widthEnd);
                }
            }
        }

    }

    /**
     * Calculate the offsets for the RoadLayout. Note that offsets can be different for begin and end, and that they can be
     * specified from the right, left or center of the lane/stripe. The overall Link can have an additional start offset and end
     * offset that has to be added to the already calculated offsets.
     * @param roadLayoutTag BasicRoadLayout; the tag for the road layout containing all lanes and stripes
     * @param cseDataList List; the list of offsets and widths for each tag, in order of definition in the
     *            RoadLayout tag
     * @param cseTagMap Map; the map of the tags to the index in the list, to be able to find them
     *            quickly
     * @param eval Eval; expression evaluator.
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static void calculateOffsets(final BasicRoadLayout roadLayoutTag, final List<CseData> cseDataList,
            final Map<Object, Integer> cseTagMap, final Eval eval)
    {
        Iterator<Serializable> tags = roadLayoutTag.getStripeOrLaneOrShoulder().iterator();
        Iterator<OffsetElement> elements = new Iterator<>()
        {
            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                return tags.hasNext();
            }

            /** {@inheritDoc} */
            @Override
            public OffsetElement next()
            {
                Length widthStart;
                Length widthEnd;
                Length offsetStart;
                Length offsetEnd;
                Serializable object = tags.next();
                if (object instanceof CseStripe)
                {
                    CseStripe stripe = (CseStripe) object;
                    widthStart = Length.ZERO;
                    widthEnd = Length.ZERO;
                    if (stripe.getCenterOffset() != null)
                    {
                        offsetStart = stripe.getCenterOffset().get(eval);
                        offsetEnd = offsetStart;
                    }
                    else
                    {
                        offsetStart = stripe.getCenterOffsetStart() != null ? stripe.getCenterOffsetStart().get(eval) : null;
                        offsetEnd = stripe.getCenterOffsetEnd() != null ? stripe.getCenterOffsetEnd().get(eval) : null;
                    }
                }
                else
                {
                    org.opentrafficsim.xml.generated.CrossSectionElement cse =
                            (org.opentrafficsim.xml.generated.CrossSectionElement) object;
                    Length width = cse.getWidth() != null ? cse.getWidth().get(eval) : null;
                    widthStart = cse.getWidth() == null ? cse.getWidthStart().get(eval) : width;
                    Length halfWidthStart = widthStart.times(0.5);
                    widthEnd = cse.getWidth() == null ? cse.getWidthEnd().get(eval) : width;
                    Length halfWidthEnd = widthEnd.times(0.5);

                    if (cse.getCenterOffset() != null)
                    {
                        offsetStart = cse.getCenterOffset().get(eval);
                        offsetEnd = offsetStart;
                    }
                    else if (cse.getLeftOffset() != null)
                    {
                        Length leftOffset = cse.getLeftOffset().get(eval);
                        offsetStart = leftOffset.minus(halfWidthStart);
                        offsetEnd = leftOffset.minus(halfWidthEnd);
                    }
                    else if (cse.getRightOffset() != null)
                    {
                        Length rightOffset = cse.getRightOffset().get(eval);
                        offsetStart = rightOffset.plus(halfWidthStart);
                        offsetEnd = rightOffset.plus(halfWidthEnd);
                    }
                    else
                    {
                        if (cse.getCenterOffsetStart() != null)
                        {
                            offsetStart = cse.getCenterOffsetStart().get(eval);
                        }
                        else if (cse.getLeftOffsetStart() != null)
                        {
                            offsetStart = cse.getLeftOffsetStart().get(eval).minus(halfWidthStart);
                        }
                        else if (cse.getRightOffsetStart() != null)
                        {
                            offsetStart = cse.getRightOffsetStart().get(eval).plus(halfWidthStart);
                        }
                        else
                        {
                            offsetStart = null;
                        }

                        if (cse.getCenterOffsetEnd() != null)
                        {
                            offsetEnd = cse.getCenterOffsetEnd().get(eval);
                        }
                        else if (cse.getLeftOffsetEnd() != null)
                        {
                            offsetEnd = cse.getLeftOffsetEnd().get(eval).minus(halfWidthEnd);
                        }
                        else if (cse.getRightOffsetEnd() != null)
                        {
                            offsetEnd = cse.getRightOffsetEnd().get(eval).plus(halfWidthEnd);
                        }
                        else
                        {
                            offsetEnd = null;
                        }
                    }
                }
                return new OffsetElement(widthStart, widthEnd, offsetStart, offsetEnd, object);
            }
        };
        calculateOffsets(elements, cseDataList, cseTagMap);
    }

    /**
     * A logical wrapper class that can provide width and center offset information.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class OffsetElement
    {
        /** Start width. */
        private final Length widthStart;

        /** End width. */
        private final Length widthEnd;

        /** Start offset. */
        private final Length offsetStart;

        /** End offset. */
        private final Length offsetEnd;

        /** Underlying object providing width and offset. */
        private final Object object;

        /**
         * Constructor.
         * @param widthStart Length; start width.
         * @param widthEnd Length; end width.
         * @param offsetStart Length; start offset.
         * @param offsetEnd Length; end offset.
         * @param object Object; underlying object providing width and offset.
         */
        public OffsetElement(final Length widthStart, final Length widthEnd, final Length offsetStart, final Length offsetEnd,
                final Object object)
        {
            this.widthStart = widthStart;
            this.widthEnd = widthEnd;
            this.offsetStart = offsetStart;
            this.offsetEnd = offsetEnd;
            this.object = object;
        }

        /**
         * Return the start width.
         * @return Length; start width.
         */
        public Length getWidthStart()
        {
            return this.widthStart;
        }

        /**
         * Return the end width.
         * @return Length; end width.
         */
        public Length getWidthEnd()
        {
            return this.widthEnd;
        }

        /**
         * Return the start center offset.
         * @return Length; start center offset.
         */
        public Length getCenterOffsetStart()
        {
            return this.offsetStart;
        }

        /**
         * Return the end center offset.
         * @return Length; end center offset.
         */
        public Length getCenterOffsetEnd()
        {
            return this.offsetEnd;
        }

        /**
         * Returns the underlying object providing the width and offset information.
         * @return Object; underlying object providing the width and offset information, e.g. an XML tag object or tree node.
         */
        public Object getObject()
        {
            return this.object;
        }
    }

    /**
     * This class stores offset and width information in generic form applicable to all cross-section elements.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class CseData
    {
        /** Start width of the element (stripes are defined as 0). */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length widthStart;

        /** End width of the element (stripes are defined as 0). */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length widthEnd;

        /** Start offset of the element. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length centerOffsetStart;

        /** End offset of the element. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length centerOffsetEnd;

        /**
         * Constructor.
         * @param offsetElement OffsetElement; offset element.
         */
        public CseData(final OffsetElement offsetElement)
        {
            this.widthStart = offsetElement.getWidthStart();
            this.widthEnd = offsetElement.getWidthEnd();
            this.centerOffsetStart = offsetElement.getCenterOffsetStart();
            this.centerOffsetEnd = offsetElement.getCenterOffsetEnd();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "CseData [widthStart=" + this.widthStart + ", widthEnd=" + this.widthEnd + ", centerOffsetStart="
                    + this.centerOffsetStart + ", centerOffsetEnd=" + this.centerOffsetEnd + "]";
        }
    }

}
