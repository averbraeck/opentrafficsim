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
 * HTMLGraphicsEnvironment.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HtmlGraphicsEnvironment extends GraphicsEnvironment
{
    /** the (dummy) device to use in the graphics environment. */
    HtmlDevice htmlDevice;

    /** the canvas to draw on. */
    HtmlGraphics2d graphics2D;

    /** the (dummy) configuration to use. */
    HtmlGraphicsConfiguration graphicsConfiguration;

    /**
     * 
     */
    public HtmlGraphicsEnvironment()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphicsEnvironment.<init>");
        this.graphics2D = new HtmlGraphics2d();
        this.graphicsConfiguration = new HtmlGraphicsConfiguration();
        this.htmlDevice = new HtmlDevice(this.graphicsConfiguration);
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
