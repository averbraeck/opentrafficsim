package org.opentrafficsim.kpi.sampling.serialization;

/**
 * TextSerializationException is the exception thrown on errors when (de)serializing objects. <br>
 * <br>
 * Copyright (c) 2020-2023 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TextSerializationException extends Exception
{

    /** */
    private static final long serialVersionUID = 20200302L;

    /**
     * Constructor for TextSerializationException.
     */
    public TextSerializationException()
    {
    }

    /**
     * Constructor for TextSerializationException.
     * @param message String; explanation of the exception
     * @param cause Throwable; underlying exception
     */
    public TextSerializationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor for TextSerializationException.
     * @param message String; explanation of the exception
     */
    public TextSerializationException(final String message)
    {
        super(message);
    }

    /**
     * Constructor for TextSerializationException.
     * @param cause Throwable; underlying exception
     */
    public TextSerializationException(final Throwable cause)
    {
        super(cause);
    }

}
