package org.opentrafficsim.demo.geometry.shape;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.naming.NamingException;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSReplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSAnimator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShapeTestApplication extends DSOLApplication implements UNITS
{
    /**
     * @param title String title of the application window
     * @param panel DSOLPanel
     */
    public ShapeTestApplication(final String title, final DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel)
    {
        super(title, panel);
    }

    /** */
    private static final long serialVersionUID = 20140819L;

    /**
     * @param args String[]; command line arguments
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     * @throws IOException on ???
     */
    public static void main(final String[] args) throws SimRuntimeException, NamingException, IOException
    {
        ShapeModel model = new ShapeModel();
        DEVSAnimator simulator = new DEVSAnimator();
        OTSReplication replication = new OTSReplication("rep1", new SimTimeDoubleUnit(Time.ZERO), Duration.ZERO,
                new Duration(7200.0, SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);

        DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel = new DSOLPanel<Time, Duration, SimTimeDoubleUnit>(model, simulator);

        Rectangle2D extent = new Rectangle2D.Double(65000.0, 440000.0, 55000.0, 30000.0);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, simulator);
        panel.getTabbedPane().addTab(0, "animation", animationPanel);

        // tell the animation panel to update its statistics
        // TODO should be done automatically in DSOL!
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));

        new ShapeTestApplication("Network Transmission Model", panel);
    }

}
