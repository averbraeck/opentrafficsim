package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;

/**
 * Color by GTU type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuTypeGtuColorer extends AbstractLegendColorer<Gtu, GtuType> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20180117L;

    /**
     * Constructor.
     * @param gtuTypeColors map of colors
     * @param unknownColor color for unknown GTU types
     */
    public GtuTypeGtuColorer(final Map<GtuType, Color> gtuTypeColors, final Color unknownColor)
    {
        super((gtu) -> gtu.getType(),
                (gtuType) -> gtuTypeColors.containsKey(gtuType) ? gtuTypeColors.get(gtuType) : unknownColor,
                Stream.concat(
                        gtuTypeColors.entrySet().stream()
                                .map((e) -> new LegendEntry(e.getValue(), e.getKey().getId(), e.getKey().getId())),
                        Stream.of(new LegendEntry(unknownColor, "Unknown", "Unknown"))).collect(Collectors.toList()));
    }

    /**
     * Constructor.
     * @param gtuTypes GTU types
     */
    public GtuTypeGtuColorer(final GtuType... gtuTypes)
    {
        this(IntStream.range(0, gtuTypes.length).mapToObj((i) -> new Object[] {gtuTypes[i], Colors.getEnumerated(i)})
                .collect(Collectors.toMap((objs) -> (GtuType) objs[0], (objs) -> (Color) objs[1])), Color.CYAN);
    }

    @Override
    public String getName()
    {
        return "GTU type";
    }

}
