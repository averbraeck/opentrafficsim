package org.opentrafficsim.swing.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.djunits.locale.DefaultLocale;

import com.bric.multislider.MultiThumbSlider;
import com.bric.multislider.MultiThumbSliderUi;
import com.bric.multislider.MultiThumbSliderUi.Thumb;

/**
 * Wrapper for Jeremy Wood's MultiThumbSlider.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ProbabilityDistributionEditor extends JPanel
{
    /** */
    private static final long serialVersionUID = 20141222L;

    /** The internal MultiThumbSlider. */
    private MultiThumbSlider<String> slider;

    /** The JLabels that indicate the current values. */
    private JLabel[] labels;

    /**
     * Construct a graphical ProbabilityDistributioneEditor.
     * @param elementNames String[]; the names of the elements of the probability distribution
     * @param values Double[]; the initial values of the probabilities (should add up to 1.0 and should have same length as
     *            <cite>elementNames</cite>)
     */
    public ProbabilityDistributionEditor(final String[] elementNames, final Double[] values)
    {
        super(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        float[] initialValues = new float[values.length - 1];
        double sum = 0;
        String[] reducedNames = new String[elementNames.length - 1];
        for (int i = 0; i < values.length - 1; i++)
        {
            sum += values[i];
            initialValues[i] = (float) sum;
            reducedNames[i] = elementNames[i];
        }
        this.slider = new MultiThumbSlider<String>(MultiThumbSlider.HORIZONTAL, initialValues, reducedNames);
        this.slider.setThumbOverlap(true);
        this.slider.putClientProperty(MultiThumbSliderUi.THUMB_SHAPE_PROPERTY, Thumb.Hourglass);
        this.slider.setThumbOverlap(true);
        this.slider.setAutoAdding(false);
        this.slider.setPreferredSize(new Dimension(250, 50));
        add(this.slider, gbc);
        gbc.gridwidth = 1;
        this.labels = new JLabel[values.length];
        for (int i = 0; i < values.length; i++)
        {
            gbc.gridy++;
            gbc.gridx = 0;
            JLabel caption = new JLabel(elementNames[i] + ": ");
            caption.setHorizontalAlignment(SwingConstants.TRAILING);
            add(caption, gbc);
            JLabel value = new JLabel("");
            value.setHorizontalAlignment(SwingConstants.LEADING);
            gbc.gridx = 1;
            add(value, gbc);
            this.labels[i] = value;
        }
    }

    /**
     * Retrieve the current probability values.
     * @return Double[]; the probability values
     */
    public final Double[] getProbabilities()
    {
        float[] positions = this.slider.getThumbPositions();
        Double[] result = new Double[positions.length + 1];
        double previous = 0;
        for (int i = 0; i < result.length - 1; i++)
        {
            double thisValue = positions[i];
            result[i] = new Double(thisValue - previous);
            previous = thisValue;
        }
        result[positions.length] = 1 - previous;
        for (int i = 0; i < result.length; i++)
        {
            this.labels[i].setText(String.format(Locale.getDefault(), "%.3f", result[i]));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addPropertyChangeListener(final PropertyChangeListener pcl)
    {
        this.slider.addPropertyChangeListener(pcl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void removePropertyChangeListener(final PropertyChangeListener pcl)
    {
        this.slider.removePropertyChangeListener(pcl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addPropertyChangeListener(final String key, final PropertyChangeListener pcl)
    {
        this.slider.addPropertyChangeListener(key, pcl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void removePropertyChangeListener(final String key, final PropertyChangeListener pcl)
    {
        this.slider.removePropertyChangeListener(key, pcl);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ProbabilityDistributionEditor [slider=" + this.slider + ", labels=" + Arrays.toString(this.labels) + "]";
    }

}
