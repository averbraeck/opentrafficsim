package org.opentrafficsim.web.animation;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

import org.opentrafficsim.base.logger.Logger;

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
        Logger.ots().trace("HTMLGraphicsEnvironment.<init>");
        this.graphics2D = new HtmlGraphics2d();
        this.graphicsConfiguration = new HtmlGraphicsConfiguration();
        this.htmlDevice = new HtmlDevice(this.graphicsConfiguration);
        this.graphicsConfiguration.setDevice(this.htmlDevice);
    }

    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.getScreenDevices()");
        return new GraphicsDevice[] {this.htmlDevice};
    }

    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.getDefaultScreenDevice()");
        return this.htmlDevice;
    }

    @Override
    public Graphics2D createGraphics(final BufferedImage img)
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.createGraphics()");
        return this.graphics2D;
    }

    @Override
    public Font[] getAllFonts()
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.getAllFonts()");
        return new Font[] {};
    }

    @Override
    public String[] getAvailableFontFamilyNames()
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

    @Override
    public String[] getAvailableFontFamilyNames(final Locale l)
    {
        Logger.ots().trace("HTMLGraphicsEnvironment.getAvailableFontFamilyNames()");
        return new String[] {};
    }

}
