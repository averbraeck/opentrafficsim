package org.opentrafficsim.gui.multislider;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opentrafficsim.gui.multislider.MultiThumbSlider.Collision;

public class MultiThumbSliderDemo extends JPanel
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

    JComboBox<Collision> collisionComboBox = new JComboBox<Collision>();

    JRadioButton overlapOnButton = new JRadioButton("On");

    JRadioButton overlapOffButton = new JRadioButton("Off");

    JRadioButton removalOnButton = new JRadioButton("Allowed");

    JRadioButton removalOffButton = new JRadioButton("Not Allowed");

    MultiThumbSlider<Character> slider1 = new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL, new float[] {0f, .5f,
        .75f, 1f}, new Character[] {'A', 'B', 'C', 'D'});

    MultiThumbSlider<Character> slider2 = new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL, new float[] {0f, .75f},
        new Character[] {'X', 'Y'});

    MultiThumbSlider<Character> slider3 = new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL, new float[] {0f, .4f,
        .75f}, new Character[] {'X', 'Y', 'Z'});

    MultiThumbSlider<Character> slider4 = new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL,
        new float[] {0f, .4f}, new Character[] {'X', 'Y'});

    MultiThumbSlider<?>[] sliders = new MultiThumbSlider<?>[] {slider1, slider2, slider3, slider4};

    public MultiThumbSliderDemo()
    {
        setLayout(new GridBagLayout());
        JPanel controls = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(controls, c);

        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 10, 10);
        slider1.setAutoAdding(false);
        slider1.setPaintTicks(true);
        slider1.setThumbRemovalAllowed(false);
        add(slider1, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 1;
        slider2.setAutoAdding(false);
        slider2.setPaintTicks(true);
        slider2.setThumbRemovalAllowed(false);
        add(slider2, c);

        c.gridx = 2;
        c.gridy = 2;
        slider3.setAutoAdding(false);
        slider3.setInverted(true);
        slider3.setThumbRemovalAllowed(false);
        slider3.putClientProperty("thumbShape", "crosshair");
        add(slider3, c);

        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 0;
        slider4.setAutoAdding(false);
        slider4.setInverted(true);
        slider4.setThumbRemovalAllowed(false);
        slider4.putClientProperty("thumbShape", "crosshair");
        add(slider4, c);

        setBackground(Color.white);
        setOpaque(true);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(3, 3, 3, 3);
        controls.add(new JLabel("Collision Policy:"), c);
        c.gridy++;
        controls.add(new JLabel("Thumb Overlap:"), c);
        c.gridy++;
        controls.add(new JLabel("Removal:"), c);

        c.gridy = 0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        controls.add(collisionComboBox, c);
        c.gridy++;
        controls.add(wrap(overlapOnButton, overlapOffButton), c);
        c.gridy++;
        controls.add(wrap(removalOnButton, removalOffButton), c);

        ButtonGroup group1 = new ButtonGroup();
        group1.add(overlapOnButton);
        group1.add(overlapOffButton);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(removalOnButton);
        group2.add(removalOffButton);

        overlapOffButton.setSelected(true);
        removalOffButton.setSelected(true);

        ActionListener overlapActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : sliders)
                {
                    s.putClientProperty("minimumValueProximity", overlapOffButton.isSelected() ? null : 0);
                }
            }
        };
        overlapOnButton.addActionListener(overlapActionListener);
        overlapOffButton.addActionListener(overlapActionListener);

        ActionListener removalActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : sliders)
                {
                    s.setThumbRemovalAllowed(removalOnButton.isSelected());
                }
            }
        };
        removalOnButton.addActionListener(removalActionListener);
        removalOffButton.addActionListener(removalActionListener);

        for (Collision collision : Collision.values())
        {
            collisionComboBox.addItem(collision);
        }
        ActionListener collisionActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : sliders)
                {
                    s.setCollisionPolicy((Collision) collisionComboBox.getSelectedItem());
                }
            }
        };
        collisionComboBox.addActionListener(collisionActionListener);
        collisionActionListener.actionPerformed(null);
        overlapActionListener.actionPerformed(null);
        removalActionListener.actionPerformed(null);
        controls.setOpaque(false);

        // optional console output
        for (MultiThumbSlider<?> s : sliders)
        {
            s.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    MultiThumbSlider<?> s = (MultiThumbSlider<?>) e.getSource();
                    float[] pos = s.getThumbPositions();
                    Object[] values = s.getValues();
                    for (int a = 0; a < pos.length; a++)
                    {
                        System.out.println("thumb[" + a + "]: " + pos[a] + " -> " + values[a]);
                    }
                }
            });
        }
    }

    private JPanel wrap(JComponent... list)
    {
        JPanel p = new JPanel(new FlowLayout());
        p.setOpaque(false);
        for (JComponent c : list)
        {
            p.add(c);
        }
        return p;
    }
}
