package org.opentrafficsim.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo2;
import org.opentrafficsim.swing.gui.OTSSwingApplication;

/**
 * SuperDemo.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class SuperDemo extends JFrame
{
    /** */
    private static final long serialVersionUID = 1L;

    /** demos to show. */
    private List<Demo> demos = new ArrayList<>();

    /**
     * Construct a mode chooser that can start different models.
     * @throws HeadlessException when not run in graphics environment
     */
    public SuperDemo() throws HeadlessException
    {
        super("OTS demo models");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 20));
        addDemos();
        setContentPane(makeGUI());
        pack();
        setVisible(true);
    }

    /**
     * Add the demos to the list.
     */
    private void addDemos()
    {
        this.demos.add(new Demo("Straight", StraightSwing.class, "Single lane road with a blockage for a while.\n"
                + "The model shows the dissolving of the congestion that occurs as a result."));
        this.demos.add(new Demo("SequentialLanes", SequentialLanes.class,
                "Cars and trucks driving on several sequential lanes, each around 1 km long.\n"));
        this.demos.add(new Demo("CircularRoad", CircularRoadSwing.class, "Model of a two-lane circular road with overtaking.\n"
                + "Users can specify the fraction of cars and trucks, as well as some driving parameters."));
        this.demos.add(new Demo("CircularLane", CircularLaneSwing.class, "Model of a one-lane circular road with overtaking.\n"
                + "Users can specify the fraction of cars and trucks, as well as some driving parameters."));
        this.demos.add(new Demo("FundamentalDiagrams", FundamentalDiagrams.class,
                "Creating fundamental diagrams for a straight road.\n"));
        this.demos.add(new Demo("ShortMerge", ShortMerge.class, "Short merge on a highway, followed by a destination split,\n"
                + "forcing cars to change lanes in a relative short distance."));
        this.demos.add(new Demo("Networks", NetworksSwing.class,
                "A number of different networks with merging and splitting,\n" + "forcing cars to change lanes and to merge."));
        this.demos.add(new Demo("CrossingTrafficLights", CrossingTrafficLightsSwing.class,
                "Model of a crossing with traffic lights.\n"));
        this.demos.add(new Demo("TrafCODDemoSimple", TrafCODDemo.class,
                "Model of a simple crossing with traffic lights.\n" + "using a TrafCOD controller"));
        this.demos.add(new Demo("TrafCODDemoComplex", TrafCODDemo2.class,
                "Model of a complex crossing with traffic lights.\n" + "using a TrafCOD controller"));
    }

    /**
     * @return a grid with model start buttons and short model descriptions.
     */
    private JComponent makeGUI()
    {
        Box table = Box.createVerticalBox();
        table.setBackground(Color.WHITE);
        table.setOpaque(true);
        JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        int maxButtonWidth = 0;
        table.add(Box.createRigidArea(new Dimension(0, 3)));
        Box headerBox = Box.createHorizontalBox();
        JLabel header = new JLabel(" OpenTrafficSim demo's ");
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        headerBox.add(header);
        headerBox.add(Box.createHorizontalGlue());
        table.add(headerBox);
        table.add(Box.createRigidArea(new Dimension(0, 3)));
        table.add(new JSeparator());
        for (Demo demo : this.demos)
        {
            table.add(Box.createRigidArea(new Dimension(0, 3)));
            Box row = Box.createHorizontalBox();
            row.add(new JLabel("  "));
            row.add(demo.getButton());
            row.add(new JLabel("  "));
            JTextArea description = new JTextArea();
            description.setText(demo.getDescription());
            description.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            description.setEditable(false);
            description.setWrapStyleWord(true);
            description.setLineWrap(true);
            row.add(description);
            row.add(Box.createHorizontalGlue());
            row.add(new JLabel("  "));
            table.add(row);
            table.add(Box.createRigidArea(new Dimension(0, 3)));
            table.add(new JSeparator());
            maxButtonWidth = Math.max(maxButtonWidth, demo.getButton().getPreferredSize().width);
        }

        for (Demo demo : this.demos)
        {
            demo.getButton().setPreferredSize(new Dimension(maxButtonWidth, demo.getButton().getPreferredSize().height));
        }

        table.add(Box.createVerticalGlue());

        return scrollPane;
    }

    /**
     * @param args String[]; should be empty
     */
    public static void main(final String[] args)
    {
        new SuperDemo();
    }

    /** the class to prevent the models from exiting the whole SuperDemo application. */
    static class NoExitSecurityManager extends SecurityManager
    {
        @Override
        public void checkExit(final int status)
        {
            throw new SecurityException();
        }
    }

    /** the information about the demos. */
    private static class Demo implements ActionListener
    {
        /** the demo name. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        protected final String name;

        /** the start button for the demo. */
        private final JButton button;

        /** the demo class. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        protected final Class<? extends OTSSwingApplication> clazz;

        /** the demo description. */
        private final String description;

        /**
         * @param name String; the demo name
         * @param clazz Class&lt;? extends OTSSwingApplication&gt;; the demo class
         * @param description String; the demo description
         */
        Demo(final String name, final Class<? extends OTSSwingApplication> clazz, final String description)
        {
            super();
            this.name = name;
            this.button = new JButton(name);
            this.button.addActionListener(this);
            this.clazz = clazz;
            this.description = description;
        }

        /**
         * @return the button to tart the demo
         */
        public final JButton getButton()
        {
            return this.button;
        }

        /**
         * @return the demo description
         */
        public final String getDescription()
        {
            return this.description;
        }

        /** {@inheritDoc} */
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            try
            {
                final Method demoMethod = ClassUtil.resolveMethod(this.clazz, "demo", new Class[] {boolean.class});
                Thread demo = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Class.forName(Demo.this.clazz.getName());
                            demoMethod.invoke(null, new Object[] {false});
                        }
                        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                | ClassNotFoundException exception)
                        {
                            JOptionPane
                                    .showMessageDialog(null,
                                            "Method 'demo' for demo " + Demo.this.name + " cound not be started\n"
                                                    + exception.getMessage(),
                                            "Could not start demo", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                demo.start();
            }
            catch (NoSuchMethodException exception)
            {
                JOptionPane.showMessageDialog(null, "Method 'demo' not found for demo " + this.name, "Could not start demo",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
