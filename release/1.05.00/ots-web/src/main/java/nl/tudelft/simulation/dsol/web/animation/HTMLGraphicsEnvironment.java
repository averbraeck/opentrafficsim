package nl.tudelft.simulation.dsol.web.animation;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.logger.Cat;


/**
 * HTMLGraphicsEnvironment.java. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HTMLGraphicsEnvironment extends GraphicsEnvironment
{
    /** the (dummy) device to use in the graphics environment. */
    HTMLDevice htmlDevice;

    /** the canvas to draw on. */
    HTMLGraphics2D graphics2D;

    /** the (dummy) configuration to use. */
    HTMLGraphicsConfiguration graphicsConfiguration;

    /**
     * 
     */
    public HTMLGraphicsEnvironment()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.<init>");
        this.graphics2D = new HTMLGraphics2D();
        this.graphicsConfiguration = new HTMLGraphicsConfiguration();
        this.htmlDevice = new HTMLDevice(this.graphicsConfiguration);
        this.graphicsConfiguration.setDevice(this.htmlDevice);
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.getScreenDevices()");
        return new GraphicsDevice[] {this.htmlDevice};
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.getDefaultScreenDevice()");
        return this.htmlDevice;
    }

    /** {@inheritDoc} */
    @Override
    public Graphics2D createGraphics(BufferedImage img)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.createGraphics()");
        return this.graphics2D;
    }

    /** {@inheritDoc} */
    @Override
    public Font[] getAllFonts()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.getAllFonts()");
        return new Font[] {};
    }

    /** {@inheritDoc} */
    @Override
    public String[] getAvailableFontFamilyNames()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

    /** {@inheritDoc} */
    @Override
    public String[] getAvailableFontFamilyNames(Locale l)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

}
