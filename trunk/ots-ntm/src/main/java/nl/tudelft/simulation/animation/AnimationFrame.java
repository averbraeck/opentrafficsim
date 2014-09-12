/*
 * @(#) GridPanel.java Oct 26, 2003 Copyright (c) 2002-2005 Erasmus University
 * Rotterdam Rotterdam, the Netherlands All rights reserved. This software is
 * proprietary information of Erasmus University The code is published under the
 * General Public License
 */
package nl.tudelft.simulation.animation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.animation.actions.HomeAction;
import nl.tudelft.simulation.animation.actions.PanDownAction;
import nl.tudelft.simulation.animation.actions.PanLeftAction;
import nl.tudelft.simulation.animation.actions.PanRightAction;
import nl.tudelft.simulation.animation.actions.PanUpAction;
import nl.tudelft.simulation.animation.actions.ShowGridAction;
import nl.tudelft.simulation.animation.actions.ZoomInAction;
import nl.tudelft.simulation.animation.actions.ZoomOutAction;

/**
 * The AnimationFrame <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * @version $Revision: 1.1 $ $Date: 2007/01/07 04:56:38 $
 * @author <a href="http://www.peter-jacobs.com/index.htm">Peter Jacobs </a>
 */
public class AnimationFrame extends JFrame
{
    /** */
    private static final long serialVersionUID = 1L;

    // TODO: different! Just a quick hack!
    static
    {
        double x = 4.0;
        double y = 51.0;
        double w = 7.0 - x;
        double h = 57.0 - y;
        System.getProperties().put("animation.panel.extent", x + ";" + y + ";" + w + ";" + h);
        System.getProperties().put("animation.panel.size", "1024;768");
    }

    /**
     * Constructor for AnimationFrame.
     * @param name the name of the frame
     * @param simulator the simulator
     */
    public AnimationFrame(final String name, final OTSSimulatorInterface simulator)
    {
        super(name);
        this.getContentPane().setLayout(new BorderLayout());
        Rectangle2D extent = null;
        // TODO: different!
        String extentString = System.getProperties().getProperty("animation.panel.extent");
        if (extentString != null)
        {
            double[] values = new double[4];
            for (int i = 0; i < 3; i++)
            {
                values[i] = new Double(extentString.substring(0, extentString.indexOf(";"))).doubleValue();
                extentString = extentString.substring(extentString.indexOf(";") + 1);
            }
            values[3] = new Double(extentString).doubleValue();
            extent = new Rectangle2D.Double(values[0], values[1], values[2], values[3]);
        }
        else
        {
            extent = new Rectangle2D.Double(-100, -100, 200, 200);
        }
        // TODO: different!
        String sizeString = System.getProperties().getProperty("animation.panel.size");
        Dimension size = new Dimension(1024, 768);
        if (sizeString != null)
        {
            double width = new Double(sizeString.substring(0, sizeString.indexOf(";"))).doubleValue();
            double height = new Double(sizeString.substring(sizeString.indexOf(";") + 1)).doubleValue();
            size = new Dimension((int) width, (int) height);
        }
        AnimationPanel panel = new AnimationPanel(extent, size, simulator);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(new ButtonPanel(panel), BorderLayout.SOUTH);
        this.pack();
        this.setVisible(true);
        panel.requestFocus();
    }

    /**
     * The ButtonPanel class
     */
    public static class ButtonPanel extends JPanel
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * constructs a new ButtonPanel
         * @param target the target to control
         */
        public ButtonPanel(final GridPanel target)
        {
            this.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

            JButton zoomIn = new JButton(new ZoomInAction(target));
            JButton zoomOut = new JButton(new ZoomOutAction(target));
            JButton left = new JButton(new PanLeftAction(target));
            JButton right = new JButton(new PanRightAction(target));
            JButton up = new JButton(new PanUpAction(target));
            JButton down = new JButton(new PanDownAction(target));
            JButton grid = new JButton(new ShowGridAction(target));
            JButton home = new JButton(new HomeAction(target));
            this.add(zoomIn);
            this.add(zoomOut);
            this.add(left);
            this.add(right);
            this.add(up);
            this.add(down);
            this.add(grid);
            this.add(home);
        }
    }
}