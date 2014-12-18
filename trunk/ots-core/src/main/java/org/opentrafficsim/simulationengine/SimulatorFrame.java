package org.opentrafficsim.simulationengine;

import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Wrap a DSOL simulator, or any (descendant of a) JPanel in a JFrame (wrap it in a window). The window will be
 * maximized.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 16 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimulatorFrame extends JFrame
{

    /** */
    private static final long serialVersionUID = 20141216L;

    /**
     * Wrap a JPanel in a JFrame.
     * @param title String; title for the JFrame
     * @param panel JPanel; the JPanel that will become the contentPane of the JFrame
     */
    public SimulatorFrame(final String title, final JPanel panel)
    {
        super();
        setTitle(title);
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
    }
    
    /**
     * Wrap a WrappableSimulation in a JFrame.
     * @param simulation WrappableSimulation; the simulation that will be shown in the JFrame
     * @param panel JPanel; this should be the JPanel of the simulation
     */
    public SimulatorFrame(final WrappableSimulation simulation, final JPanel panel)
    {
        super();
        setTitle(simulation.shortName());
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
    }
}
