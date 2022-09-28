package org.opentrafficsim.swing.gui;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * JPanel with an outline and a name.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LabeledPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20141222L;

    /**
     * Create a JPanel with border and caption.
     * @param caption String; the caption of the LabeledPanel
     */
    public LabeledPanel(final String caption)
    {
        setBorder(new TitledBorder(null, caption, TitledBorder.LEADING, TitledBorder.TOP, null, null));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LabeledPanel [caption=" + ((TitledBorder) getBorder()).getTitle() + "]";
    }
}
