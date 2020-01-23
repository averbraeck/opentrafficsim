package org.opentrafficsim.draw.core;

/**
 * OTSDrawingException . <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class OTSDrawingException extends Exception
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    public OTSDrawingException()
    {
        //
    }

    /**
     * @param message String; the error message
     */
    public OTSDrawingException(final String message)
    {
        super(message);
    }

    /**
     * @param cause Throwable; the cause of the exception to be included
     */
    public OTSDrawingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message String; the error message
     * @param cause Throwable; the cause of the exception to be included
     */
    public OTSDrawingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
