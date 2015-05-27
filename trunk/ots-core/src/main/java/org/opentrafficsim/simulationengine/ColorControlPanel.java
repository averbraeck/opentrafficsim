package org.opentrafficsim.simulationengine;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;

import org.opentrafficsim.core.gtu.GTU;

/**
 * Let the user select what the colors in the animation mean.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 27 mei 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ColorControlPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20150527L;
    
    /** The combo box that sets the coloring for the GTUs. */
    final JComboBox<String> comboBoxGTUColor;
    
    /**
     * Add a ColorControlPanel to an AnimationPanel.
     * @param animationPanel AnimationPanel; the animationPanel that will be augmented with the new ColorControlPanel
     */
    public ColorControlPanel (AnimationPanel animationPanel)
    {
        this.comboBoxGTUColor = new JComboBox<String>();
        this.comboBoxGTUColor.addItem("Vehicle id");
        this.comboBoxGTUColor.addItem("Destination id");
        this.comboBoxGTUColor.addItem("Lane change urgency");
        this.comboBoxGTUColor.addItem("Speed");
        this.comboBoxGTUColor.addItem("Acceleration");
        this.add(this.comboBoxGTUColor);
        animationPanel.add(this, BorderLayout.NORTH);
    }
    
    /**
     * Return the fill color for a GTU.
     * @param gtu GTU&lt;?&gt;; the GTU
     * @return Color
     */
    public Color getColor(GTU<?> gtu)
    {
        return Color.red;
    }
    
}
