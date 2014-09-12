package org.opentrafficsim.demo.IDMPlus.swing;

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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 20, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoriesSwingApplication extends DSOLApplication
{
    /** */
    private static final long serialVersionUID = 20140820L;

    /**
     * @param title
     * @param panel
     */
    public TrajectoriesSwingApplication(String title,
            DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        super(title, panel);
    }

    /**
     * @param args
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws SimRuntimeException, RemoteException
    {
        TrajectoriesModel model = new TrajectoriesModel();
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        OTSReplication replication =
                new OTSReplication("rep1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model, simulator);
        makePlot(model, panel);
        addInfoTab(panel);
        new TrajectoriesSwingApplication("IDM-plus model - Trajectories", panel);
    }

    /**
     * make the stand-alone plot for the model and put it in the statistics panel.
     * @param model the model.
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
        model.setTrajectories(tp);
        charts.setCell(tp.getContentPane(), 0, 0);
    }

    /**
     * @param panel
     */
    private static void addInfoTab(
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
        String helpSource = "/" + ContourPlotsModel.class.getPackage().getName().replace('.', '/') + "/package.html";
        URL page = ContourPlotsModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel = new HTMLPanel(page);
            panel.getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
        }
    }

}
