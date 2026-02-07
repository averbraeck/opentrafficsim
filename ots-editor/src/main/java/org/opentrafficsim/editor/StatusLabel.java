package org.opentrafficsim.editor;

import java.awt.Color;

import javax.swing.JLabel;

import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.swing.gui.AppearanceControl;

/**
 * Status label at bottom of the screen with font color interpolated between background and foreground colors in GUI appearance.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StatusLabel extends JLabel implements AppearanceControl
{

    /** */
    private static final long serialVersionUID = 20231017L;

    /** Standard background color in appearance. */
    private Color appearanceBackgroundColor;

    /** Standard foreground color in appearance. */
    private Color appearanceForegroundColor;

    /**
     * Constructor.
     */
    public StatusLabel()
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBackground()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isForeground()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFont()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setBackground(final Color bg)
    {
        this.appearanceBackgroundColor = bg;
        super.setBackground(bg);
        updateForgroundColor();
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(final Color fg)
    {
        this.appearanceForegroundColor = fg;
        super.setForeground(fg);
        updateForgroundColor();
    }

    /**
     * Updates the foreground color by reducing the contrast with the background color.
     */
    private void updateForgroundColor()
    {
        if (this.appearanceForegroundColor != null && this.appearanceBackgroundColor != null)
        {
            super.setForeground(
                    ColorInterpolator.interpolateColor(this.appearanceForegroundColor, this.appearanceBackgroundColor, 0.7));
        }
    }

}
