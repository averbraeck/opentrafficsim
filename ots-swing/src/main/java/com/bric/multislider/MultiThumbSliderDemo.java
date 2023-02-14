package com.bric.multislider;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;

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

import com.bric.multislider.MultiThumbSlider.Collision;
import com.bric.multislider.MultiThumbSliderUi.Thumb;

@SuppressWarnings("javadoc")
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

    JComboBox<Class> uiComboBox = new JComboBox<Class>();

    JComboBox<DefaultMultiThumbSliderUi.Thumb> thumbComboBox = new JComboBox<DefaultMultiThumbSliderUi.Thumb>();

    JRadioButton overlapOnButton = new JRadioButton("On");

    JRadioButton overlapOffButton = new JRadioButton("Off");

    JRadioButton removalOnButton = new JRadioButton("Allowed");

    JRadioButton removalOffButton = new JRadioButton("Not Allowed");

    MultiThumbSlider<Character> slider1 = new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL,
            new float[] {0f, .5f, .75f, 1f}, new Character[] {'A', 'B', 'C', 'D'});

    MultiThumbSlider<Character> slider2 =
            new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL, new float[] {0f, .75f}, new Character[] {'X', 'Y'});

    MultiThumbSlider<Character> slider3 = new MultiThumbSlider<Character>(MultiThumbSlider.VERTICAL,
            new float[] {0f, .4f, .75f}, new Character[] {'X', 'Y', 'Z'});

    MultiThumbSlider<Character> slider4 =
            new MultiThumbSlider<Character>(MultiThumbSlider.HORIZONTAL, new float[] {0f, .4f}, new Character[] {'X', 'Y'});

    MultiThumbSlider<?>[] sliders = new MultiThumbSlider<?>[] {this.slider1, this.slider2, this.slider3, this.slider4};

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
        this.slider1.setAutoAdding(false);
        this.slider1.setPaintTicks(true);
        this.slider1.setThumbRemovalAllowed(false);
        add(this.slider1, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 1;
        this.slider2.setAutoAdding(false);
        this.slider2.setPaintTicks(true);
        this.slider2.setThumbRemovalAllowed(false);
        add(this.slider2, c);

        c.gridx = 2;
        c.gridy = 2;
        this.slider3.setAutoAdding(false);
        this.slider3.setInverted(true);
        this.slider3.setThumbRemovalAllowed(false);
        add(this.slider3, c);

        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 0;
        this.slider4.setAutoAdding(false);
        this.slider4.setInverted(true);
        this.slider4.setThumbRemovalAllowed(false);
        add(this.slider4, c);

        setBackground(Color.white);
        setOpaque(true);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(3, 3, 3, 3);
        controls.add(new JLabel("Collision Policy:"), c);
        c.gridy++;
        controls.add(new JLabel("Thumb Shape:"), c);
        c.gridy++;
        controls.add(new JLabel("Thumb Overlap:"), c);
        c.gridy++;
        controls.add(new JLabel("Removal:"), c);
        c.gridy++;
        controls.add(new JLabel("UI:"), c);

        c.gridy = 0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        controls.add(this.collisionComboBox, c);
        c.gridy++;
        controls.add(this.thumbComboBox, c);
        c.gridy++;
        controls.add(wrap(this.overlapOnButton, this.overlapOffButton), c);
        c.gridy++;
        controls.add(wrap(this.removalOnButton, this.removalOffButton), c);
        c.gridy++;
        controls.add(this.uiComboBox, c);

        ButtonGroup group1 = new ButtonGroup();
        group1.add(this.overlapOnButton);
        group1.add(this.overlapOffButton);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(this.removalOnButton);
        group2.add(this.removalOffButton);

        this.overlapOffButton.setSelected(true);
        this.removalOffButton.setSelected(true);

        ActionListener overlapActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : MultiThumbSliderDemo.this.sliders)
                {
                    s.setThumbOverlap(MultiThumbSliderDemo.this.overlapOnButton.isSelected());
                }
            }
        };
        this.overlapOnButton.addActionListener(overlapActionListener);
        this.overlapOffButton.addActionListener(overlapActionListener);

        ActionListener removalActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : MultiThumbSliderDemo.this.sliders)
                {
                    s.setThumbRemovalAllowed(MultiThumbSliderDemo.this.removalOnButton.isSelected());
                }
            }
        };
        this.removalOnButton.addActionListener(removalActionListener);
        this.removalOffButton.addActionListener(removalActionListener);

        for (Collision collision : Collision.values())
        {
            this.collisionComboBox.addItem(collision);
        }
        ActionListener collisionActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : MultiThumbSliderDemo.this.sliders)
                {
                    s.setCollisionPolicy((Collision) MultiThumbSliderDemo.this.collisionComboBox.getSelectedItem());
                }
            }
        };
        this.collisionComboBox.addActionListener(collisionActionListener);

        for (Thumb t : Thumb.values())
        {
            this.thumbComboBox.addItem(t);
        }
        ActionListener thumbActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (MultiThumbSlider<?> s : MultiThumbSliderDemo.this.sliders)
                {
                    s.putClientProperty(MultiThumbSliderUi.THUMB_SHAPE_PROPERTY,
                            (Thumb) MultiThumbSliderDemo.this.thumbComboBox.getSelectedItem());
                }
            }
        };
        this.thumbComboBox.addActionListener(thumbActionListener);

        thumbActionListener.actionPerformed(null);
        collisionActionListener.actionPerformed(null);
        overlapActionListener.actionPerformed(null);
        removalActionListener.actionPerformed(null);
        controls.setOpaque(false);

        // optional console output
        for (MultiThumbSlider<?> s : this.sliders)
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

        this.uiComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    for (MultiThumbSlider<?> s : MultiThumbSliderDemo.this.sliders)
                    {
                        Class t = (Class) MultiThumbSliderDemo.this.uiComboBox.getSelectedItem();
                        Constructor constructor = t.getConstructor(new Class[] {MultiThumbSlider.class});
                        s.setUI((MultiThumbSliderUi) constructor.newInstance(new Object[] {s}));
                    }
                    Window w = SwingUtilities.getWindowAncestor(MultiThumbSliderDemo.this);
                    if (w != null)
                        w.pack();
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        });
        this.uiComboBox.addItem(AquaMultiThumbSliderUi.class);
        this.uiComboBox.addItem(DefaultMultiThumbSliderUi.class);
        this.uiComboBox.addItem(VistaMultiThumbSliderUI.class);

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
