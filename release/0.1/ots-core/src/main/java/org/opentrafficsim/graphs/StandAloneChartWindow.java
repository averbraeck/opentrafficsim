package org.opentrafficsim.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

/**
 * Create a stand-alone window for a JFreeChart graph.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 8 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class StandAloneChartWindow
{
    /** This class shall never be instantiated. */
    private StandAloneChartWindow()
    {
        // This class should not be instantiated
    }

    /**
     * Create a menu item that, when clicked, creates a detached window for a JFreeChart plot.
     * @param data MultipleViewerChart; graph that is capable of being shown in several JFrames
     * @return JMenuItem
     */
    public static JMenuItem createMenuItem(final MultipleViewerChart data)
    {
        JMenuItem result = new JMenuItem("Show in detached window");
        result.addActionListener(new ActionListener()
        {
            private final MultipleViewerChart additionalViewer = data;

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                JFrame window = this.additionalViewer.addViewer();
                window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                window.pack();
                window.setVisible(true);
            }
        });
        return result;
    }
}
