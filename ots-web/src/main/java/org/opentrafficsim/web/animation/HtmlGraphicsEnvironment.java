package org.opentrafficsim.web.animation;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

import org.djutils.logger.CategoryLogger;

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
    private HtmlDevice htmlDevice;

    /** the canvas to draw on. */
    private HtmlGraphics2d graphics2D;

    /** the (dummy) configuration to use. */
    private HtmlGraphicsConfiguration graphicsConfiguration;

    /**
     * Constructor.
     */
    public HtmlGraphicsEnvironment()
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.<init>");
        this.graphics2D = new HtmlGraphics2d();
        this.graphicsConfiguration = new HtmlGraphicsConfiguration();
        this.htmlDevice = new HtmlDevice(this.graphicsConfiguration);
        this.graphicsConfiguration.setDevice(this.htmlDevice);
    }

    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.getScreenDevices()");
        return new GraphicsDevice[] {this.htmlDevice};
    }

    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.getDefaultScreenDevice()");
        return this.htmlDevice;
    }

    @Override
    public Graphics2D createGraphics(final BufferedImage img)
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.createGraphics()");
        return this.graphics2D;
    }

    @Override
    public Font[] getAllFonts()
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.getAllFonts()");
        return new Font[] {};
    }

    @Override
    public String[] getAvailableFontFamilyNames()
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

    @Override
    public String[] getAvailableFontFamilyNames(final Locale l)
    {
        CategoryLogger.always().trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

}
