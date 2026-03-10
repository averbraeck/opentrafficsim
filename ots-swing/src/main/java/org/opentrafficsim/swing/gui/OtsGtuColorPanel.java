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
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
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

    /** GTU colorer from properties. */
    private String lastGtuColorer;

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
        // remember this state, as adding each colorer triggers an event that overwrites this property
        this.lastGtuColorer = OtsSimulationPanel.PROPERTIES.getProperty("gtuColorer");
    }

    /**
     * Add GTU colorer.
     * @param colorer the GTU colorer to add
     */
    public void addGtuColorer(final Colorer<? super Gtu> colorer)
    {
        Predicate<Gtu> predicate = (gtu) -> colorer.equals(OtsGtuColorPanel.this.gtuColorer);
        PredicatedColorer predicatedColorer = new PredicatedColorer(predicate, colorer);
        this.comboBoxGTUColor.addItem(predicatedColorer);
        this.gtuColorerManager.add(predicate, colorer);
        if (this.gtuColorer == null || colorer.getName().equals(this.lastGtuColorer))
        {
            this.gtuColorer = colorer;
            this.comboBoxGTUColor.setSelectedItem(predicatedColorer);
        }
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
            OtsSimulationPanel.PROPERTIES.setProperty("gtuColorer", this.gtuColorer.getName());
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
            NumberAxis scaleAxis = new DivisibleNumberAxis();
            scaleAxis.setNumberFormatOverride(colorbarColorer.getNumberFormat());
            // reduce tick insets from [t=2.0,l=4.0,b=2.0,r=4.0] to cramp legend in small ColorControlPanel
            scaleAxis.setTickLabelInsets(new RectangleInsets(0.0, 4.0, 0.0, 4.0));
            scaleAxis.setTickLabelPaint(getForeground());
            scaleAxis.setStandardTickUnits(new CustomTickUnitSource());
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

    /**
     * Tick unit logic that follows non-default steps per decade.
     */
    private static class CustomTickUnitSource implements TickUnitSource
    {

        /** Steps per decade. */
        private static final double[] STEPS = {1.0, 2.0, 3.0, 4.0, 5.0, 10.0};

        /**
         * Constructor.
         */
        CustomTickUnitSource()
        {
            //
        }

        @Override
        public TickUnit getLargerTickUnit(final TickUnit unit)
        {
            double next = nextCeil(unit.getSize(), false);
            return new NumberTickUnit(next, NumberFormat.getNumberInstance());
        }

        @Override
        public TickUnit getCeilingTickUnit(final double size)
        {
            double next = nextCeil(size, true);
            return new NumberTickUnit(next, NumberFormat.getNumberInstance());
        }

        @Override
        public TickUnit getCeilingTickUnit(final TickUnit unit)
        {
            return getCeilingTickUnit(unit.getSize());
        }

        /**
         * Returns the next tick size.
         * @param size current size
         * @param equalOk whether candidate may be equal to size
         * @return the next tick size
         */
        private static double nextCeil(final double size, final boolean equalOk)
        {
            int exp = (int) Math.floor(Math.log10(size));
            double pow = Math.pow(10.0, exp);

            for (double step : STEPS)
            {
                double candidate = step * pow;
                if (candidate > size || (equalOk && candidate == size))
                {
                    return candidate;
                }
            }
            // If none in this decade is strictly greater, go to the next decade.
            return Math.pow(10.0, exp + 1);
        }

    }

    /**
     * NumberAxis that prefers auto tick units which divide the current range length. It defers to the normal JFreeChart
     * algorithm to satisfy label spacing, then walks upward through available tick units until it finds one that results in an
     * integer number of intervals across the axis range, with a minimum number of accepted intervals.
     */
    private static class DivisibleNumberAxis extends NumberAxis
    {

        /** */
        private static final long serialVersionUID = 1L;

        /** Integer interval margin. */
        private static final double EPS = 1e-9;

        /** Minimum number of major ticks we'll tolerate after promotion. */
        private int minTicks = 4;

        /** Maximum number of major ticks we'll tolerate after promotion. */
        private int maxTicks = 15;

        /**
         * Constructor.
         */
        DivisibleNumberAxis()
        {
            super("");
        }

        @Override
        protected void selectAutoTickUnit(final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge)
        {
            // Let JFreeChart do its normal auto-selection first (respects label sizes/orientation).
            super.selectAutoTickUnit(g2, dataArea, edge);

            // Then "nudge" to a unit that divides the range if possible.
            Range r = getRange();
            double length = r.getLength();
            if (!(length > 0))
            {
                return;
            }

            NumberTickUnit current = getTickUnit();
            double baseSize = current.getSize();

            TickUnitSource source = getStandardTickUnits();

            // Search upward through available units for the first one that:
            // (1) is >= the base size (to keep label-fit constraints), and
            // (2) divides the range length (within EPS), and
            // (3) yields a reasonable number of ticks.
            NumberTickUnit best = null;
            int bestScore = -1;

            NumberTickUnit candidate = current;
            final int maxHops = 10;
            for (int i = 0; i < maxHops; i++)
            {
                double size = candidate.getSize();
                if (size + EPS < baseSize)
                {
                    candidate = (NumberTickUnit) source.getLargerTickUnit(candidate);
                    continue;
                }

                double q = length / size;
                int approxTicks = (int) Math.round(q);
                boolean dividesRange = nearlyInteger(q);
                boolean ticksOk = approxTicks >= this.minTicks && approxTicks <= this.maxTicks;

                if (dividesRange && ticksOk)
                {
                    int score = 1;

                    // Bonus if the lower bound is (almost) a multiple of the candidate size,
                    // so ticks line up nicely with the lower edge.
                    double lower = r.getLowerBound();
                    double k = lower / size;
                    if (nearlyInteger(k))
                    {
                        score = 2;
                    }

                    if (score > bestScore)
                    {
                        bestScore = score;
                        best = candidate;
                        // If we found the highest score possible, we can stop early.
                        if (score == 2)
                        {
                            break;
                        }
                    }
                }

                candidate = (NumberTickUnit) source.getLargerTickUnit(candidate);
            }

            if (best != null && best.getSize() >= baseSize - EPS)
            {
                // Don't notify; we're still inside the selection pass.
                setTickUnit(best, false, false);
            }
        }

        /**
         * Returns {@code true} when the value is nearly an integer.
         * @param x value
         * @return {@code true} when the value is nearly an integer
         */
        private boolean nearlyInteger(final double x)
        {
            return Math.abs(x - Math.rint(x)) <= EPS * Math.max(1.0, Math.abs(x));
        }

    }

}
