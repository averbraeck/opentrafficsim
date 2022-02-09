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
 * HTMLToolkit.java. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings("restriction")
public class HTMLToolkit extends Toolkit
{
    /** the queue of AWT events to process. */
    EventQueue eventQueue = new EventQueue();

    /**
     * 
     */
    public HTMLToolkit()
    {
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getScreenSize() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenSize()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getScreenResolution() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenResolution()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public ColorModel getColorModel() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getColorModel()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String[] getFontList()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontList()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public FontMetrics getFontMetrics(Font font)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontMetrics()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void sync()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.sync()");
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage(String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage(URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean prepareImage(Image image, int width, int height, ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.prepareImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int checkImage(Image image, int width, int height, ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.checkImage()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(ImageProducer producer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(byte[] imagedata, int imageoffset, int imagelength)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getPrintJob()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void beep()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.beep()");
    }

    /** {@inheritDoc} */
    @Override
    public Clipboard getSystemClipboard() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getSystemClipboard()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected EventQueue getSystemEventQueueImpl()
    {
        CategoryLogger.filter(Cat.WEB)
                .trace("HTMLToolkit.getSystemEventQueueImpl() -- next event is " + this.eventQueue.peekEvent());
        return this.eventQueue;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isModalityTypeSupported(ModalityType modalityType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalityTypeSupported()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isModalExclusionTypeSupported(ModalExclusionType modalExclusionType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalExclusionTypeSupported()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.mapInputMethodHighlight()");
        return null;
    }

}
