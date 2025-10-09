package org.opentrafficsim.web.animation;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;

import org.djutils.logger.CategoryLogger;

/**
 * The <code>HTMLGraphicsConfiguration</code> class describes the characteristics of the HTML canvas in the browser, as a
 * graphics destination to write to. Note that there can be several <code>GraphicsConfiguration</code> objects associated with a
 * single graphics device, representing different drawing modes or capabilities.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HtmlGraphicsConfiguration extends GraphicsConfiguration
{
    /** the {@link HtmlDevice} associated with this <code>HTMLGraphicsConfiguration</code>. */
    HtmlDevice htmlDevice;

    /** the identity AffineTransform. */
    AffineTransform identityTransform = new AffineTransform();

    /** the bounds, TODO: which should be filled in some way by the window size in the browser. */
    Rectangle bounds = new Rectangle(0, 0, 1920, 1080);

    /**
     * Create a graphics configuration for the HTML device.
     */
    public HtmlGraphicsConfiguration()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.<init>");
    }

    @Override
    public GraphicsDevice getDevice()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getDevice()");
        return this.htmlDevice;
    }

    /**
     * Set the {@link HtmlDevice} associated with this <code>HTMLGraphicsConfiguration</code>.
     * @param htmlDevice a &lt;code&gt;GraphicsDevice&lt;/code&gt; object that is associated with this
     *            <code>HTMLGraphicsConfiguration</code>.
     */
    public void setDevice(final HtmlDevice htmlDevice)
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.setDevice()");
        this.htmlDevice = htmlDevice;
    }

    @Override
    public ColorModel getColorModel()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getColorModel()");
        return ColorModel.getRGBdefault();
    }

    @Override
    public ColorModel getColorModel(final int transparency)
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getColorModel()");
        return ColorModel.getRGBdefault();
    }

    @Override
    public AffineTransform getDefaultTransform()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getDefaultTransform()");
        return this.identityTransform;
    }

    @Override
    public AffineTransform getNormalizingTransform()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getNormalizingTransform()");
        return this.identityTransform;
    }

    @Override
    public Rectangle getBounds()
    {
        CategoryLogger.always().trace("HTMLGraphicsConfiguration.getBounds()");
        return this.bounds;
    }

}
