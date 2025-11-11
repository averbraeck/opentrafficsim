package org.opentrafficsim.web.animation;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.opentrafficsim.base.logger.Logger;

/**
 * HTMLToolkit.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HtmlToolkit extends Toolkit
{
    /** the queue of AWT events to process. */
    private EventQueue eventQueue = new EventQueue();

    /**
     * Constructor.
     */
    public HtmlToolkit()
    {
    }

    @Override
    public Dimension getScreenSize() throws HeadlessException
    {
        Logger.ots().trace("HTMLToolkit.getScreenSize()");
        return null;
    }

    @Override
    public int getScreenResolution() throws HeadlessException
    {
        Logger.ots().trace("HTMLToolkit.getScreenResolution()");
        return 0;
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException
    {
        Logger.ots().trace("HTMLToolkit.getColorModel()");
        return null;
    }

    @Override
    public String[] getFontList()
    {
        Logger.ots().trace("HTMLToolkit.getFontList()");
        return null;
    }

    @Override
    public FontMetrics getFontMetrics(final Font font)
    {
        Logger.ots().trace("HTMLToolkit.getFontMetrics()");
        return null;
    }

    @Override
    public void sync()
    {
        Logger.ots().trace("HTMLToolkit.sync()");
    }

    @Override
    public Image getImage(final String filename)
    {
        Logger.ots().trace("HTMLToolkit.getImage()");
        return null;
    }

    @Override
    public Image getImage(final URL url)
    {
        Logger.ots().trace("HTMLToolkit.getImage()");
        return null;
    }

    @Override
    public Image createImage(final String filename)
    {
        Logger.ots().trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public Image createImage(final URL url)
    {
        Logger.ots().trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public boolean prepareImage(final Image image, final int width, final int height, final ImageObserver observer)
    {
        Logger.ots().trace("HTMLToolkit.prepareImage()");
        return false;
    }

    @Override
    public int checkImage(final Image image, final int width, final int height, final ImageObserver observer)
    {
        Logger.ots().trace("HTMLToolkit.checkImage()");
        return 0;
    }

    @Override
    public Image createImage(final ImageProducer producer)
    {
        Logger.ots().trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public Image createImage(final byte[] imagedata, final int imageoffset, final int imagelength)
    {
        Logger.ots().trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public PrintJob getPrintJob(final Frame frame, final String jobtitle, final Properties props)
    {
        Logger.ots().trace("HTMLToolkit.getPrintJob()");
        return null;
    }

    @Override
    public void beep()
    {
        Logger.ots().trace("HTMLToolkit.beep()");
    }

    @Override
    public Clipboard getSystemClipboard() throws HeadlessException
    {
        Logger.ots().trace("HTMLToolkit.getSystemClipboard()");
        return null;
    }

    @Override
    protected EventQueue getSystemEventQueueImpl()
    {
        Logger.ots().trace("HTMLToolkit.getSystemEventQueueImpl() -- next event is " + this.eventQueue.peekEvent());
        return this.eventQueue;
    }

    @Override
    public boolean isModalityTypeSupported(final ModalityType modalityType)
    {
        Logger.ots().trace("HTMLToolkit.isModalityTypeSupported()");
        return false;
    }

    @Override
    public boolean isModalExclusionTypeSupported(final ModalExclusionType modalExclusionType)
    {
        Logger.ots().trace("HTMLToolkit.isModalExclusionTypeSupported()");
        return false;
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(final InputMethodHighlight highlight) throws HeadlessException
    {
        Logger.ots().trace("HTMLToolkit.mapInputMethodHighlight()");
        return null;
    }

}
