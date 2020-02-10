package org.opentrafficsim.demo.geometry;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSwingApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.DSOLApplication;
import nl.tudelft.simulation.dsol.swing.gui.DSOLPanel;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 16, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestGeometry extends DSOLApplication implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param title String; t
     * @param panel DSOLPanel&lt;Time,Duration,SimTimeDoubleUnit&gt;; p
     */
    public TestGeometry(final String title, final DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel)
    {
        super(title, panel);
    }

    /**
     * @param args String[]; args
     * @throws RemoteException if error
     * @throws SimRuntimeException if error
     * @throws NamingException if error
     * @throws OTSDrawingException if error
     * @throws DSOLException when the simulator does not implement AnimatorInterface
     */
    public static void main(final String[] args)
            throws SimRuntimeException, NamingException, RemoteException, OTSDrawingException, DSOLException
    {
        OTSAnimator simulator = new OTSAnimator("TestGeometry");
        OTSModelInterface model = new TestModel(simulator);
        OTSReplication replication =
                OTSReplication.create("rep1", Time.ZERO, Duration.ZERO, new Duration(1800.0, SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel = new DSOLPanel<Time, Duration, SimTimeDoubleUnit>(model, simulator);

        Rectangle2D extent = new Rectangle2D.Double(-50, -50, 300, 100);
        Dimension size = new Dimension(1024, 768);
        OTSAnimationPanel animationPanel =
                new OTSAnimationPanel(extent, size, simulator, model, new DefaultSwitchableGTUColorer(), model.getNetwork());
        panel.getTabbedPane().addTab(0, "animation", animationPanel);

        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator(), OTSSwingApplication.DEFAULT_COLORER);
        AnimationToggles.setTextAnimationTogglesStandard(animationPanel);

        // tell the animation panel to update its statistics
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));

        new TestGeometry("TestGeometry", panel);
    }

}
