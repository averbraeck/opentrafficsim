package org.opentrafficsim.draw.colorer;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opentrafficsim.draw.Colors;

/**
 * Color object based on their id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public class IdColorer<T> extends AbstractLegendColorer<T, String>
{

    /**
     * Constructor.
     * @param valueFunction value function
     */
    public IdColorer(final Function<? super T, String> valueFunction)
    {
        super(valueFunction, (id) -> Colors.getIdColor(id, Colors.ENUMERATE),
                IntStream.range(0, Colors.ENUMERATE.length).mapToObj(
                        (i) -> new LegendEntry(Colors.ENUMERATE[i], Colors.ENUMERATE_NAMES[i], Colors.ENUMERATE_NAMES[i]))
                        .collect(Collectors.toList()));
    }

    @Override
    public final String getName()
    {
        return "ID";
    }

}
