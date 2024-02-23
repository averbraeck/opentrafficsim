package org.opentrafficsim.swing.gui;

import javax.swing.JComboBox;

/**
 * ComboBox for AppearanceControl.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> generic type of the ComboBox
 */
public class AppearanceControlComboBox<T> extends JComboBox<T> implements AppearanceControl
{

    /** */
    private static final long serialVersionUID = 20231016L;

    /** {@inheritDoc} */
    @Override
    public boolean isFont()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AppearanceControlComboBox []";
    }

}
