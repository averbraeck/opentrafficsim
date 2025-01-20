package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager.PredicatedColorer;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Let the user select what the colors in the animation mean.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class ColorControlPanel extends JPanel implements ActionListener
{
    /** */
    private static final long serialVersionUID = 20150527L;

    /** The combo box that sets the coloring for the GTUs. */
    private final JComboBox<PredicatedColorer> comboBoxGTUColor = new AppearanceControlComboBox<>();

    /** The panel that holds the legend for the currently selected GtuColorer. */
    private final JPanel legendPanel;

    /** GTU colorer manager. */
    private final GtuColorerManager gtuColorerManager = new GtuColorerManager(Color.WHITE);

    /** The GtuColorer that is currently active. */
    private GtuColorer gtuColorer;

    /**
     * Add a ColorControlPanel to an AnimationPanel. Initially the ColorControlPanel will have no items. Items are added with
     * the <code>addItem</code> method.
     */
    public ColorControlPanel()
    {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.legendPanel = new JPanel(new FlowLayout());
        this.add(this.comboBoxGTUColor);
        this.add(this.legendPanel);
        this.comboBoxGTUColor.addActionListener(this);
    }

    /**
     * Add one item to this ColorControlPanel.
     * @param colorer the GtuColorer that will be added
     */
    public final void addItem(final GtuColorer colorer)
    {
        if (this.gtuColorer == null)
        {
            this.gtuColorer = colorer;
        }
        Predicate<Gtu> predicate = (gtu) -> colorer.equals(ColorControlPanel.this.gtuColorer);
        this.comboBoxGTUColor.addItem(new PredicatedColorer(predicate, colorer));
        this.gtuColorerManager.add(predicate, colorer);
        rebuildLegend();
    }

    /**
     * Returns the GTU colorer manager.
     * @return GTU colorer manager
     */
    public GtuColorerManager getGtuColorerManager()
    {
        return this.gtuColorerManager;
    }

    @Override
    public final void actionPerformed(final ActionEvent e)
    {
        PredicatedColorer newColorerWrapper = (PredicatedColorer) this.comboBoxGTUColor.getSelectedItem();
        if (null != newColorerWrapper)
        {
            this.gtuColorer = newColorerWrapper.colorer();
            rebuildLegend();
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

                @Override
                public String toString()
                {
                    return "ColorBox []";
                }

            }
            ColorBox colorBox = new ColorBox();
            colorBox.setOpaque(true); // By default, the label is transparent
            colorBox.setBackground(legendEntry.color());
            Border border = LineBorder.createBlackLineBorder();
            colorBox.setBorder(border);
            panel.add(colorBox, BorderLayout.LINE_START);
            JLabel name = new JLabel(" " + legendEntry.name().trim());
            panel.add(name, BorderLayout.CENTER);
            name.setOpaque(true);
            name.setForeground(getForeground());
            name.setBackground(getBackground());
            panel.setToolTipText(legendEntry.description());
            this.legendPanel.add(panel);
        }
        this.legendPanel.revalidate();

        Container parentPanel = this.getParent();

        if (parentPanel != null)
        {
            parentPanel.repaint();
        }
    }

    @Override
    public final String toString()
    {
        return "ColorControlPanel [gtuColorer=" + this.gtuColorer + "]";
    }

}
