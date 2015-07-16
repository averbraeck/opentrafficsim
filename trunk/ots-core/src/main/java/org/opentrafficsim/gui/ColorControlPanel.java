package org.opentrafficsim.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;

/**
 * Let the user select what the colors in the animation mean.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 27 mei 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ColorControlPanel extends JPanel implements ActionListener
{
    /** */
    private static final long serialVersionUID = 20150527L;

    /** The combo box that sets the coloring for the GTUs. */
    private JComboBox<GTUColorer> comboBoxGTUColor;

    /** The panel that holds the legend for the currently selected GTUColorer. */
    private final JPanel legendPanel;

    /** The GTUColorer that is currently active. */
    private GTUColorer gtuColorer;

    /** The SwitchableGTUColorer that is controlled by this ColorControlPanel, if available. */
    private final SwitchableGTUColorer switchableGTUColorer;

    /**
     * Add a ColorControlPanel to an AnimationPanel. Initially the ColorControlPanel will have no items. Items are added
     * with the <code>addItem</code> method. The first item added automatically becomes the active one.
     * @param gtuColorer SwitchableGTUColorer; the switchable GTU colorer that will be controlled by this
     *            ColorControlPanel
     */
    public ColorControlPanel(final GTUColorer gtuColorer)
    {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.legendPanel = new JPanel(new FlowLayout());

        if (gtuColorer instanceof SwitchableGTUColorer)
        {
            this.switchableGTUColorer = (SwitchableGTUColorer) gtuColorer;
            this.comboBoxGTUColor = new JComboBox<GTUColorer>();
            this.add(this.comboBoxGTUColor);
            this.comboBoxGTUColor.addActionListener(this);

            for (GTUColorer colorer : ((SwitchableGTUColorer) gtuColorer).getColorers())
            {
                addItem(colorer);
            }
        }
        else
        {
            this.gtuColorer = gtuColorer;
            this.switchableGTUColorer = null;
            rebuildLegend();
        }

        this.add(this.legendPanel);
    }

    /**
     * Add one item to this ColorControlPanel. The <cite>getName</cite> method of the
     * @param colorer GTUColorer; the GTUColorer that will be added
     */
    public final void addItem(final GTUColorer colorer)
    {
        this.comboBoxGTUColor.addItem(colorer);
        // The first item added automatically becomes the current one and triggers a call to actionPerformed.
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent e)
    {
        GTUColorer newColorer = (GTUColorer) this.comboBoxGTUColor.getSelectedItem();
        if (null != newColorer)
        {
            this.gtuColorer = newColorer;
            rebuildLegend();

            if (this.switchableGTUColorer != null)
            {
                this.switchableGTUColorer.setGTUColorer(this.comboBoxGTUColor.getSelectedIndex());
            }
        }
    }

    /**
     * Build or rebuild the legend on the screen.
     */
    private void rebuildLegend()
    {
        this.legendPanel.removeAll();
        for (GTUColorer.LegendEntry legendEntry : this.gtuColorer.getLegend())
        {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel colorBox = new JLabel("     ");
            colorBox.setOpaque(true); // By default, the label is transparant
            colorBox.setBackground(legendEntry.getColor());
            Border border = LineBorder.createBlackLineBorder();
            colorBox.setBorder(border);
            panel.add(colorBox, BorderLayout.LINE_START);
            JLabel name = new JLabel(" " + legendEntry.getName().trim());
            panel.add(name, BorderLayout.CENTER);
            panel.setToolTipText(legendEntry.getDescription());
            this.legendPanel.add(panel);
        }
        this.legendPanel.validate();
        Container parentPanel = this.getParent();

        if (parentPanel != null)
        {
            parentPanel.repaint();
        }
    }

}
