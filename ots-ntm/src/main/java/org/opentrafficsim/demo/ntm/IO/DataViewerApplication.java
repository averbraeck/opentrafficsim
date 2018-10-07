package org.opentrafficsim.demo.ntm.IO;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.JScrollPane;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.demo.ntm.NTMModel;
import org.opentrafficsim.simulationengine.SimpleAnimator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DataViewerApplication extends DSOLApplication
{
    /**
     * @param title
     * @param panel
     */
    public DataViewerApplication(String title, DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel)
    {
        super(title, panel);
    }

    /** */
    private static final long serialVersionUID = 20140819L;

    /**
     * @param args
     * @throws SimRuntimeException
     * @throws RemoteException
     * @throws NamingException
     * @throws IOException
     * @throws PropertyException
     */
    public static void main(final String[] args) throws SimRuntimeException, NamingException, IOException, PropertyException
    {
        DataViewer model = new DataViewer();
        // model.getSettingsNTM().getStartTimeSinceMidnight().getInUnit(DurationUnit.SECOND)
        Time startTime = new Time(0.0, TimeUnit.BASE_SECOND);
        // OTSReplication replication =
        // new OTSReplication("rep1", startTime, new Duration(0.0, DurationUnit.SECOND), model
        // .getSettingsNTM().getDurationOfSimulation(), model);
        SimpleAnimator simulator = new SimpleAnimator(startTime, new Duration(0.0, DurationUnit.SECOND),
                new Duration(7200.0, DurationUnit.SECOND), model);
        // simulator.initialize(replication, ReplicationMode.TERMINATING);

        DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel = new DSOLPanel<Time, Duration, SimTimeDoubleUnit>(model, simulator);
        addInfoTab(panel);

        Rectangle2D extent = new Rectangle2D.Double(65000.0, 440000.0, 55000.0, 30000.0);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, simulator);
        panel.getTabbedPane().addTab(0, "animation", animationPanel);

        // tell the animation panel to update its statistics
        // TODO should be done automatically in DSOL!
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));

        new DataViewerApplication("Network Transmission Model", panel);
    }

    /**
     * @param panel
     */
    private static void addInfoTab(final DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
        String helpSource = "/" + NTMModel.class.getPackage().getName().replace('.', '/') + "/html/ntm.html";
        URL page = NTMModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel = null;
            try
            {
                htmlPanel = new HTMLPanel(page);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            panel.getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
        }
        else
        {
            System.err.println("Information page " + helpSource + " not found.");
        }
    }
}
