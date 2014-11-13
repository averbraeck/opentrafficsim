package org.opentrafficsim.demo.IDMPlus.swing;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.swing.JScrollPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;

import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.TrajectoryPlot;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 20, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoriesSwingApplication extends DSOLApplication
{
    /** */
    private static final long serialVersionUID = 20140820L;

    /**
     * @param title String
     * @param panel DSOLPanel
     */
    public TrajectoriesSwingApplication(final String title,
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        super(title, panel);
    }

    /**
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException on ???
     * @throws RemoteException on communication failure
     */
    public static void main(final String[] args) throws SimRuntimeException, RemoteException
    {
        TrajectoriesModel model = new TrajectoriesModel();
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        OTSReplication replication =
                new OTSReplication("rep1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model,
                        simulator);
        makePlot(model, panel);
        addInfoTab(panel);
        new TrajectoriesSwingApplication("IDM-plus model - Trajectories", panel);
    }

    /**
     * make the stand-alone plot for the model and put it in the statistics panel.
     * @param model the model.
     * @param panel DSOLPanel
     */
    private static void makePlot(final TrajectoriesModel model,
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        TablePanel charts = new TablePanel(1, 1);
        panel.getTabbedPane().addTab("statistics", charts);
        DoubleScalar.Rel<TimeUnit> sampleInterval = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);
        TrajectoryPlot tp =
                new TrajectoryPlot("Trajectory Plot", sampleInterval, model.getMinimumDistance(),
                        model.getMaximumDistance());
        tp.setTitle("Density Contour Graph");
        tp.setExtendedState(MAXIMIZED_BOTH);
        model.setTrajectoryPlot(tp);
        charts.setCell(tp.getContentPane(), 0, 0);
    }

    /**
     * @param panel DSOLPanel
     */
    private static void addInfoTab(
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
        String helpSource = "/" + InternalContourPlotsModel.class.getPackage().getName().replace('.', '/') + "/package.html";
        URL page = InternalContourPlotsModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel;
            try
            {
                htmlPanel = new HTMLPanel(page);
                panel.getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

}
