package nl.tudelft.simulation.dsol.web.animation;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.logger.Cat;

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
     * @param htmlGraphicsConfiguration GraphicsConfiguration; the GraphicsConfiguration to add to the HTMLDevice
     */
    public HtmlDevice(GraphicsConfiguration htmlGraphicsConfiguration)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLDevice.<init>");
        this.htmlGraphicsConfigurations = new GraphicsConfiguration[] {htmlGraphicsConfiguration};
    }

    /** {@inheritDoc} */
    @Override
    public int getType()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLDevice.getType()");
        return GraphicsDevice.TYPE_RASTER_SCREEN;
    }

    /** {@inheritDoc} */
    @Override
    public String getIDstring()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLDevice.getIDString()");
        return "HTMLDevice";
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration[] getConfigurations()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLDevice.getConfiguration()");
        return this.htmlGraphicsConfigurations;
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration getDefaultConfiguration()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLDevice.getDefaultConfiguration()");
        return this.htmlGraphicsConfigurations[0];
    }

}
