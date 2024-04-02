package org.opentrafficsim.swing.gui;

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

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SwitchableGtuColorer;

/**
 * Let the user select what the colors in the animation mean.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ColorControlPanel extends JPanel implements ActionListener
{
    /** */
    private static final long serialVersionUID = 20150527L;

    /** The combo box that sets the coloring for the GTUs. */
    private JComboBox<GtuColorer> comboBoxGTUColor;

    /** The panel that holds the legend for the currently selected GtuColorer. */
    private final JPanel legendPanel;

    /** The GtuColorer that is currently active. */
    private GtuColorer gtuColorer;

    /** The SwitchableGtuColorer that is controlled by this ColorControlPanel, if available. */
    private final SwitchableGtuColorer switchableGtuColorer;

    /**
     * Add a ColorControlPanel to an AnimationPanel. Initially the ColorControlPanel will have no items. Items are added with
     * the <code>addItem</code> method. The first item added automatically becomes the active one.
     * @param gtuColorer GtuColorer; the switchable GTU colorer that will be controlled by this ColorControlPanel
     */
    public ColorControlPanel(final GtuColorer gtuColorer)
    {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.legendPanel = new JPanel(new FlowLayout());

        if (gtuColorer instanceof SwitchableGtuColorer)
        {
            this.switchableGtuColorer = (SwitchableGtuColorer) gtuColorer;

            this.comboBoxGTUColor = new AppearanceControlComboBox<>();
            this.add(this.comboBoxGTUColor);
            this.comboBoxGTUColor.addActionListener(this);

            for (GtuColorer colorer : ((SwitchableGtuColorer) gtuColorer).getColorers())
            {
                addItem(colorer);
            }
        }
        else
        {
            this.gtuColorer = gtuColorer;
            this.switchableGtuColorer = null;
            rebuildLegend();
        }

        this.add(this.legendPanel);
    }

    /**
     * Add one item to this ColorControlPanel. The <cite>getName</cite> method of the
     * @param colorer GtuColorer; the GtuColorer that will be added
     */
    public final void addItem(final GtuColorer colorer)
    {
        this.comboBoxGTUColor.addItem(colorer);
        // The first item added automatically becomes the current one and triggers a call to actionPerformed.
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent e)
    {
        GtuColorer newColorer = (GtuColorer) this.comboBoxGTUColor.getSelectedItem();
        if (null != newColorer)
        {
            this.gtuColorer = newColorer;
            rebuildLegend();

            if (this.switchableGtuColorer != null)
            {
                this.switchableGtuColorer.setGtuColorer(this.comboBoxGTUColor.getSelectedIndex());
            }
        }
    }

    /**
     * Build or rebuild the legend on the screen.
     */
    private void rebuildLegend()
    {
        this.legendPanel.removeAll();
        for (GtuColorer.LegendEntry legendEntry : this.gtuColorer.getLegend())
        {
            JPanel panel = new JPanel(new BorderLayout());
            /**
             * ColorBox for AppearanceControl.
             */
            class ColorBox extends JLabel implements AppearanceControl
            {
                /** */
                private static final long serialVersionUID = 20180206L;

                /**
                 * Constructor.
                 */
                ColorBox()
                {
                    super("     ");
                }

                /** {@inheritDoc} */
                @Override
                public String toString()
                {
                    return "ColorBox []";
                }

            }
            ColorBox colorBox = new ColorBox();
            colorBox.setOpaque(true); // By default, the label is transparent
            colorBox.setBackground(legendEntry.getColor());
            Border border = LineBorder.createBlackLineBorder();
            colorBox.setBorder(border);
            panel.add(colorBox, BorderLayout.LINE_START);
            JLabel name = new JLabel(" " + legendEntry.getName().trim());
            panel.add(name, BorderLayout.CENTER);
            name.setOpaque(true);
            name.setForeground(getForeground());
            name.setBackground(getBackground());
            panel.setToolTipText(legendEntry.getDescription());
            this.legendPanel.add(panel);
        }
        this.legendPanel.revalidate();

        Container parentPanel = this.getParent();

        if (parentPanel != null)
        {
            parentPanel.repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ColorControlPanel [gtuColorer=" + this.gtuColorer + "]";
    }

}
