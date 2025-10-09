package org.opentrafficsim.web.animation;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

import org.djutils.logger.CategoryLogger;

/**
 * HTMLDevice.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HtmlDevice extends GraphicsDevice
{
    /** the GraphicsConfigurations for this HTMLDevice. */
    private GraphicsConfiguration[] htmlGraphicsConfigurations;

    /**
     * Constructor.
     * @param htmlGraphicsConfiguration the GraphicsConfiguration to add to the HTMLDevice
     */
    public HtmlDevice(final GraphicsConfiguration htmlGraphicsConfiguration)
    {
        CategoryLogger.always().trace("HTMLDevice.<init>");
        this.htmlGraphicsConfigurations = new GraphicsConfiguration[] {htmlGraphicsConfiguration};
    }

    @Override
    public int getType()
    {
        CategoryLogger.always().trace("HTMLDevice.getType()");
        return GraphicsDevice.TYPE_RASTER_SCREEN;
    }

    @Override
    public String getIDstring()
    {
        CategoryLogger.always().trace("HTMLDevice.getIDString()");
        return "HTMLDevice";
    }

    @Override
    public GraphicsConfiguration[] getConfigurations()
    {
        CategoryLogger.always().trace("HTMLDevice.getConfiguration()");
        return this.htmlGraphicsConfigurations;
    }

    @Override
    public GraphicsConfiguration getDefaultConfiguration()
    {
        CategoryLogger.always().trace("HTMLDevice.getDefaultConfiguration()");
        return this.htmlGraphicsConfigurations[0];
    }

}
