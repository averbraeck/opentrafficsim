package org.opentrafficsim.swing.gui;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import org.djutils.exceptions.Throw;
import org.djutils.swing.multislider.LinearMultiSlider;

/**
 * Editor for a distribution of probabilities of all possible categories. The probabilities must sum to 1.0.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> category type
 */
public class ProbabilityDistributionEditor<T> extends LinearMultiSlider<Double>
{

    /** */
    private static final long serialVersionUID = 20250916L;

    /** Number of values the slider allows per percent. */
    private final int valuesPerPercent;

    /** Categories. */
    private final List<T> categories;

    /** Label function. */
    private BiFunction<T, Double, String> labelFunction = (t, p) -> String.format("%s: %.1f%%", t, p * 100.0);

    /** Category font size. */
    private float categoryFontSize = 10.0f;

    /**
     * Constructor.
     * @param categories categories
     * @param probabilities probabilities
     * @param valuesPerPercent number of values the slider allows per percent
     */
    public ProbabilityDistributionEditor(final List<T> categories, final double[] probabilities, final int valuesPerPercent)
    {
        super(0.0, 1.0, 100 * valuesPerPercent + 1, checkValues(probabilities));
        Throw.whenNull(categories, "categories");
        Throw.whenNull(probabilities, "probabilities");
        Throw.when(categories.size() != new LinkedHashSet<>(categories).size(), IllegalArgumentException.class,
                "The categories are not unique.");
        Throw.when(valuesPerPercent < 1, IllegalArgumentException.class, "valuesPerPercent should be at least 1.");
        this.categories = new ArrayList<>(categories);
        this.valuesPerPercent = 100 * valuesPerPercent;
        // create default %-labels, although not shown by default
        setLabelTable(new Hashtable<Integer, JLabel>(IntStream.range(0, 11).collect(() -> new LinkedHashMap<>(),
                (m, i) -> m.put(i * 10 * valuesPerPercent, new JLabel((i * 10) + "%")), (m1, m2) -> m1.putAll(m2))));
        setMajorTickSpacing(5 * valuesPerPercent);
        setPaintTicks(true);
        setOverlap(true);
        setPaintTrack(false);
        this.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                // need to update the labels as we drag, otherwise the thumbs erase (part of) the labels
                repaint();
            }
        });
    }

    /**
     * Check values are positive and add up to one.
     * @param probabilities probabilities
     * @return cumulative result of probabilities
     */
    private static Double[] checkValues(final double[] probabilities)
    {
        Double[] out = new Double[probabilities.length - 1];
        double cumul = 0.0;
        for (int i = 0; i < probabilities.length - 1; i++)
        {
            Throw.when(probabilities[i] < 0.0, IllegalArgumentException.class, "Probabilities should not be negative.");
            cumul += probabilities[i];
            out[i] = cumul;
        }
        Throw.when(Math.abs(cumul + probabilities[probabilities.length - 1] - 1.0) > 1e-9, IllegalArgumentException.class,
                "Probabilities do not add up to one.");
        return out;
    }

    @Override
    protected Double mapIndexToValue(final int index)
    {
        return ((double) index) / this.valuesPerPercent;
    }

    /**
     * Sets the label function. This function receives the category object and the probability in the normalized [0...1] range.
     * @param labelfunction label function receiving the category object and the probability in the normalized [0...1] range
     */
    public void setCategoryLabelFunction(final BiFunction<T, Double, String> labelfunction)
    {
        Throw.whenNull(labelfunction, "labelfunction");
        this.labelFunction = labelfunction;
    }

    /**
     * Set the font size for the category labels.
     * @param categoryFontSize font size for the category labels
     */
    public void setCategoryFontSize(final float categoryFontSize)
    {
        Throw.when(categoryFontSize <= 0.0, IllegalArgumentException.class, "Category font size should be larger than 0.");
        this.categoryFontSize = categoryFontSize;
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics g)
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        // similar code as in AbstractMultiSlider.calculateTrackSize(), but that''s all private
        int trackSizeLoPx = 0;
        JSlider js = getSlider(0);
        BasicSliderUI ui = (BasicSliderUI) js.getUI();
        int loValue = js.getMinimum();
        int w = 0;
        while (w < js.getWidth() && ui.valueForXPosition(w) == loValue)
        {
            trackSizeLoPx = w++;
        }

        // edges between probability bands
        int[] edges = new int[getNumberOfThumbs() + 2];
        edges[0] = trackSizeLoPx;
        for (int i = 0; i < getNumberOfThumbs(); i++)
        {
            edges[i + 1] = thumbPositionPx(i);
        }
        edges[edges.length - 1] = trackSizeLoPx + trackSize();

        // draw labels
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(this.categoryFontSize));
        FontMetrics fontMetric = g2.getFontMetrics();
        for (int i = 0; i < edges.length - 1; i++)
        {
            int x = (edges[i] + edges[i + 1]) / 2;
            String categoryLabel;
            try
            {
                categoryLabel = this.labelFunction.apply(this.categories.get(i), getProbability(i));
            }
            catch (Exception e)
            {
                categoryLabel = this.categories.get(i).toString();
            }
            Rectangle2D d = fontMetric.getStringBounds(categoryLabel, g2);
            int left = (int) (x - d.getWidth() / 2.0);
            if (left < 0)
            {
                g2.drawString(categoryLabel, 0, fontMetric.getHeight());
            }
            else if (left + d.getWidth() > getWidth())
            {
                g2.drawString(categoryLabel, (int) (getWidth() - d.getWidth()), fontMetric.getHeight());
            }
            else
            {
                g2.drawString(categoryLabel, left, fontMetric.getHeight());
            }
        }
    }

    /**
     * Retrieve the current probability values.
     * @return the probability values
     */
    public double[] getProbabilities()
    {
        double[] result = new double[this.categories.size()];
        for (int i = 0; i < this.categories.size(); i++)
        {
            result[i] = getProbability(i);
        }
        return result;
    }

    /**
     * Returns the probability of the given category.
     * @param t category
     * @return the probability of the given category
     * @throws IllegalArgumentException if the category object is not part of the distribution
     */
    public double getProbability(final T t)
    {
        Throw.when(!this.categories.contains(t), IllegalArgumentException.class, "Category {} is not part of the distribution.",
                t);
        return getProbability(this.categories.indexOf(t));
    }

    /**
     * Returns the probability of category with given index.
     * @param i category index
     * @return the probability of category with given index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public double getProbability(final int i)
    {
        Objects.checkIndex(i, this.categories.size());
        if (i == 0)
        {
            return getValue(0);
        }
        else if (i == this.categories.size() - 1)
        {
            return 1.0 - getValue(this.categories.size() - 2);
        }
        return getValue(i) - getValue(i - 1);
    }

}
