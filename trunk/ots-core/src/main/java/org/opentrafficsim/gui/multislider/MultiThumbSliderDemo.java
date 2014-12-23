package org.opentrafficsim.gui.multislider;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MultiThumbSliderDemo extends BricApplet
{
    private static final long serialVersionUID = 1L;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new MultiThumbSliderDemo());

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public MultiThumbSliderDemo()
    {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 10, 10);
        MultiThumbSlider<Character> slider =
                new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL, new float[]{0f, .5f, .75f, 1f},
                        new Character[]{'A', 'B', 'C', 'D'});
        slider.setAutoAdding(false);
        slider.setCrossoverAllowed(false);
        slider.setPaintTicks(true);
        slider.putClientProperty("MultiThumbSlider.indicateSelectedThumb", "false");
        getContentPane().add(slider, c);

        c.gridx = 0;
        c.gridy = 1;
        slider =
                new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL, new float[]{0f, .75f}, new Character[]{'X',
                        'Y'});
        slider.setPaintTicks(true);
        slider.putClientProperty("MultiThumbSlider.indicateComponent", "false");
        getContentPane().add(slider, c);

        c.gridx = 2;
        c.gridy = 1;
        slider =
                new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL, new float[]{0f, .4f, .75f}, new Character[]{
                        'X', 'Y', 'Z'});
        slider.setInverted(true);
        slider.putClientProperty("MultiThumbSlider.indicateComponent", "false");
        getContentPane().add(slider, c);

        c.gridx = 1;
        c.gridy = 2;
        slider =
                new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL, new float[]{0f, .4f}, new Character[]{'X',
                        'Y'});
        slider.setInverted(true);
        getContentPane().add(slider, c);

        getContentPane().setBackground(Color.white);
        if (getContentPane() instanceof JComponent)
            ((JComponent) getContentPane()).setOpaque(true);
    }
}
