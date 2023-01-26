package org.opentrafficsim.kpi.sampling.serialization;

/**
 * ShortSerializer (de)serializes Short objects.<br>
 * <br>
 * Copyright (c) 2020-2023 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ShortSerializer implements TextSerializer<Short>
{
    /** {@inheritDoc} */
    @Override
    public String serialize(final Short value)
    {
        return String.valueOf(value.shortValue());
    }

    /** {@inheritDoc} */
    @Override
    public Short deserialize(final Class<Short> type, final String text, final String unit)
    {
        return Short.valueOf(text);
    }

}
