package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleInsets;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager.PredicatedColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.ColorbarColorer;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.draw.colorer.LegendColorer.LegendEntry;

/**
 * Let the user select what the GTU color in the animation means.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsGtuColorPanel extends JPanel implements ActionListener, AppearanceControl
{
    /** */
    private static final long serialVersionUID = 20150527L;

    /** Combo box that sets the coloring for the GTUs. */
    private final JComboBox<PredicatedColorer> comboBoxGTUColor = new AppearanceControlComboBox<>();

    /** Panel that holds the legend for the currently selected GTU colorer. */
    private final JPanel legendPanel;

    /** GTU colorer manager. */
    private final GtuColorerManager gtuColorerManager = new GtuColorerManager(Color.WHITE);

    /** GTU colorer that is currently active. */
    private Colorer<? super Gtu> gtuColorer;

    /**
     * Constructor.
     */
    public OtsGtuColorPanel()
    {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        this.add(this.comboBoxGTUColor);
        this.add(this.legendPanel);
        this.comboBoxGTUColor.addActionListener(this);
    }

    /**
     * Add GTU colorer.
     * @param colorer the GTU colorer to add
     */
    public void addGtuColorer(final Colorer<? super Gtu> colorer)
    {
        if (this.gtuColorer == null)
        {
            this.gtuColorer = colorer;
        }
        Predicate<Gtu> predicate = (gtu) -> colorer.equals(OtsGtuColorPanel.this.gtuColorer);
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
    public void actionPerformed(final ActionEvent e)
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
        if (this.gtuColorer instanceof ColorbarColorer<?> colorbarColorer)
        {
            NumberAxis scaleAxis = new NumberAxis("");
            scaleAxis.setNumberFormatOverride(colorbarColorer.getNumberFormat());
            // reduce tick insets from [t=2.0,l=4.0,b=2.0,r=4.0] to cramp legend in small ColorControlPanel
            scaleAxis.setTickLabelInsets(new RectangleInsets(0.0, 4.0, 0.0, 4.0));
            scaleAxis.setTickLabelPaint(getForeground());
            PaintScaleLegend colorbar = new PaintScaleLegend(colorbarColorer.getBoundsPaintScale(), scaleAxis);
            // 13px is all that fits without increasing the height of the ColorControlPanel, 1px margin for other computers
            colorbar.setStripWidth(12.0);
            colorbar.setSubdivisionCount(256);
            colorbar.setPadding(0.0, 25.0, 0.0, 25.0); // horizontal padding for numbers (and units) centered at extreme ticks
            colorbar.setBackgroundPaint(new Color(0, 0, 0, 0));
            JPanel panel = new JPanel()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void paint(final Graphics g)
                {
                    colorbar.draw((Graphics2D) g, getBounds());
                }
            };
            panel.setPreferredSize(new Dimension(400, 26));
            this.legendPanel.add(panel);
        }
        else if (this.gtuColorer instanceof LegendColorer<?> legendColorer)
        {
            for (LegendEntry legendEntry : legendColorer.getLegend())
            {
                JPanel panel = new JPanel(new BorderLayout());
                /** ColorBox for AppearanceControl. */
                class ColorBox extends JLabel implements AppearanceControl
                {
                    /** */
                    private static final long serialVersionUID = 20180206L;

                    /** Constructor. */
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
        }
        this.legendPanel.revalidate();

        Container parentPanel = this.getParent();
        if (parentPanel != null)
        {
            parentPanel.getParent().repaint();
        }
    }

    @Override
    public boolean isBackground()
    {
        return true;
    }

    @Override
    public boolean isForeground()
    {
        return true;
    }

    @Override
    public void setForeground(final Color fg)
    {
        super.setForeground(fg);
        if (this.legendPanel != null)
        {
            // update tick label color which is based on the foreground color
            rebuildLegend();
        }
    }

    @Override
    public String toString()
    {
        return "OtsGtucolorPanel [gtuColorer=" + this.gtuColorer + "]";
    }

}
