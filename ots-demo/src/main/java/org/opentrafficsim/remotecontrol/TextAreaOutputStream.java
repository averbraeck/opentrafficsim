package org.opentrafficsim.remotecontrol;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Output stream that writes to a Swing component. Derived from
 * https://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TextAreaOutputStream extends OutputStream
{
    /** Swing output object to append all output to. */
    private final JTextArea textArea;

    /**
     * Construct a new TextAreaOutputStream object.
     * @param textArea JTextArea; the text area to append the output onto
     */
    TextAreaOutputStream(final JTextArea textArea)
    {
        this.textArea = textArea;
    }

    /**
     * Write to the textArea. May only be called from within the AWT thread!
     * @param bytes byte[]; bytes to write
     * @param offset int; offset within bytes of the first byte to write
     * @param length int; number of bytes to write
     */
    public void awtWrite(final byte[] bytes, final int offset, final int length)
    {
        synchronized (this.textArea)
        {
            for (int index = offset; index < offset + length; index++)
            {
                // redirects data to the text area
                this.textArea.append(String.valueOf((char) (bytes[index])));
            }
            // scrolls the text area to the end of data
            this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
        }
    }

    /**
     * Write to the textArea. May only be called from within the AWT thread!
     * @param b int; byte to write
     */
    public void awtWrite(final int b)
    {
        synchronized (this.textArea)
        {
            // redirects data to the text area
            this.textArea.append(String.valueOf((char) b));
            // scrolls the text area to the end of data
            this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final byte[] bytes, final int offset, final int length)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            awtWrite(bytes, offset, length);
        }
        else
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void run()
                    {
                        awtWrite(bytes, offset, length);
                    }
                });
            }
            catch (InvocationTargetException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final byte[] bytes)
    {
        write(bytes, 0, bytes.length);
    }

    /** {@inheritDoc} */
    @Override
    public void write(final int b)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                /** {@inheritDoc} */
                @Override
                public void run()
                {
                    awtWrite(b);
                }
            });
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
