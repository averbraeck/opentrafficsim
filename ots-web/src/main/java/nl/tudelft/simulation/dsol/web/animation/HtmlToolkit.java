package nl.tudelft.simulation.dsol.web.animation;

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

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.logger.Cat;

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
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenSize()");
        return null;
    }

    @Override
    public int getScreenResolution() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenResolution()");
        return 0;
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getColorModel()");
        return null;
    }

    @Override
    public String[] getFontList()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontList()");
        return null;
    }

    @Override
    public FontMetrics getFontMetrics(final Font font)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontMetrics()");
        return null;
    }

    @Override
    public void sync()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.sync()");
    }

    @Override
    public Image getImage(final String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    @Override
    public Image getImage(final URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    @Override
    public Image createImage(final String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public Image createImage(final URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public boolean prepareImage(final Image image, final int width, final int height, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.prepareImage()");
        return false;
    }

    @Override
    public int checkImage(final Image image, final int width, final int height, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.checkImage()");
        return 0;
    }

    @Override
    public Image createImage(final ImageProducer producer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public Image createImage(final byte[] imagedata, final int imageoffset, final int imagelength)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    @Override
    public PrintJob getPrintJob(final Frame frame, final String jobtitle, final Properties props)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getPrintJob()");
        return null;
    }

    @Override
    public void beep()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.beep()");
    }

    @Override
    public Clipboard getSystemClipboard() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getSystemClipboard()");
        return null;
    }

    @Override
    protected EventQueue getSystemEventQueueImpl()
    {
        CategoryLogger.filter(Cat.WEB)
                .trace("HTMLToolkit.getSystemEventQueueImpl() -- next event is " + this.eventQueue.peekEvent());
        return this.eventQueue;
    }

    @Override
    public boolean isModalityTypeSupported(final ModalityType modalityType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalityTypeSupported()");
        return false;
    }

    @Override
    public boolean isModalExclusionTypeSupported(final ModalExclusionType modalExclusionType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalExclusionTypeSupported()");
        return false;
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(final InputMethodHighlight highlight) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.mapInputMethodHighlight()");
        return null;
    }

}
