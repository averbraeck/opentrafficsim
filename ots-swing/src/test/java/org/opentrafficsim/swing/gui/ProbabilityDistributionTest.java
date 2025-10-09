package org.opentrafficsim.swing.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;

import org.djutils.swing.multislider.AbstractMultiSlider.FinalValueChangeListener;
import org.djutils.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Tests for and small demo of a {@code ProbabilityDistributionEditor}.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ProbabilityDistributionTest
{

    /**
     * Constructor.
     */
    private ProbabilityDistributionTest()
    {
        //
    }

    /**
     * Tests editor.
     */
    @Test
    public void testProbabilityEditor()
    {
        List<String> cat = List.of("A", "B", "C");
        double[] probs = new double[] {0.1, 0.3, 0.6};
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(null, probs, 2), NullPointerException.class);
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(cat, null, 2), NullPointerException.class);
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(cat, probs, 0), IllegalArgumentException.class);
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(cat, new double[] {0.1, 0.3, 0.5}, 2),
                IllegalArgumentException.class);
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(cat, new double[] {-0.1, 0.3, 0.7}, 2),
                IllegalArgumentException.class);
        UnitTest.testFail(() -> new ProbabilityDistributionEditor<>(List.of("A", "B", "B"), probs, 2),
                IllegalArgumentException.class);

        ProbabilityDistributionEditor<String> edit = new ProbabilityDistributionEditor<>(cat, probs, 2);
        edit.setCategoryFontSize(11.0f);
        edit.setCategoryLabelFunction((t, p) -> "");
        UnitTest.testFail(() -> edit.setCategoryLabelFunction(null), NullPointerException.class);
        UnitTest.testFail(() -> edit.setCategoryFontSize(0.0f), IllegalArgumentException.class);
        double[] probsOut = edit.getProbabilities();
        for (int i = 0; i < probs.length; i++)
        {
            assertEquals(probs[i], probsOut[i], 1e-9, "Output probabilities not correct.");
            assertEquals(probs[i], edit.getProbability(i), 1e-9, "Output probability not correct.");
            assertEquals(probs[i], edit.getProbability(cat.get(i)), 1e-9, "Output probability not correct.");
        }
        UnitTest.testFail(() -> edit.getProbability(-1), IndexOutOfBoundsException.class);
        UnitTest.testFail(() -> edit.getProbability(cat.size()), IndexOutOfBoundsException.class);
        UnitTest.testFail(() -> edit.getProbability("D"), IllegalArgumentException.class);

        ProbabilityDistributionEditor<Integer> edit2 = new ProbabilityDistributionEditor<>(List.of(1, 2, 3), probs, 2);
        edit2.getProbability(1);
    }

    /**
     * Main.
     * @param args arguments (ignored)
     */
    public static void main(final String[] args)
    {
        // frame
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(600, 200));

        // distribution editor
        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);
        ProbabilityDistributionEditor<GtuType> slider =
                new ProbabilityDistributionEditor<>(List.of(DefaultsNl.CAR, DefaultsNl.VAN, DefaultsNl.BUS, DefaultsNl.TRUCK),
                        new double[] {0.4, 0.3, 0.25, 0.05}, 2);
        slider.setCategoryFontSize(11.0f);
        slider.setCategoryLabelFunction(
                (g, p) -> String.format("%s %.1f%%", g.getId().replace("NL.", "").toLowerCase(), p * 100.0));
        // slider.setPaintTicks(false);
        // slider.setPaintLabels(true);
        panel.add(slider, BorderLayout.PAGE_START);

        // labels outside of slider
        JPanel labelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        labelsPanel.setPreferredSize(new Dimension(600, 60));
        panel.add(labelsPanel, BorderLayout.PAGE_END);
        JLabel label1 = new JLabel();
        labelsPanel.add(label1);
        JLabel label2 = new JLabel();
        labelsPanel.add(label2);
        JLabel label3 = new JLabel();
        labelsPanel.add(label3);
        JLabel label4 = new JLabel();
        labelsPanel.add(label4);
        JLabel label5 = new JLabel();
        labelsPanel.add(label5);

        // listener to update labels outside of slider
        FinalValueChangeListener listener = new FinalValueChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                label1.setText(String.format("Car: %.1f", slider.getProbability(DefaultsNl.CAR) * 100.0));
                label2.setText(String.format("Van: %.1f", slider.getProbability(DefaultsNl.VAN) * 100.0));
                label3.setText(String.format("Bus: %.1f", slider.getProbability(DefaultsNl.BUS) * 100.0));
                label4.setText(String.format("Truck: %.1f", slider.getProbability(DefaultsNl.TRUCK) * 100.0));
                double[] p = slider.getProbabilities();
                label5.setText(
                        String.format("All: [%.1f, %.1f, %.1f, %.1f]", p[0] * 100.0, p[1] * 100.0, p[2] * 100.0, p[3] * 100.0));
            }
        };
        slider.addFinalValueChangeListener(listener);

        // finalization
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        listener.stateChanged(null);
        frame.setVisible(true);
    }

}
