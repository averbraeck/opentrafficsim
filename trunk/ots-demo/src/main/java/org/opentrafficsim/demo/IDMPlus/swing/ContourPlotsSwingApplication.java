package org.opentrafficsim.demo.IDMPlus.swing;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;

import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
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
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ContourPlotsSwingApplication extends DSOLApplication
{
    /**
     * @param title
     * @param panel
     */
    public ContourPlotsSwingApplication(String title, ContourPlotsPanel panel)
    {
        super(title, panel);
    }

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param args
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    public static void main(String[] args) throws SimRuntimeException, RemoteException
    {
        OTSModelInterface model = new ContourPlotsModel();
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        OTSReplication replication =
                new OTSReplication("rep1", new OTSSimTimeDouble(new DoubleScalarAbs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalarRel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalarRel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        ContourPlotsPanel panel = new ContourPlotsPanel(model, simulator);
        makePlots((ContourPlotsModel) model, panel);
        new ContourPlotsSwingApplication("IDM-plus contourplots model", panel);
    }

    /**
     * make the stand-alone plots for the model and put them in the statistics panel.
     * @param model the model.
     */
    private static void makePlots(final ContourPlotsModel model, final ContourPlotsPanel panel)
    {
        ContourPlot cp;

        cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Density Contour Graph");
        cp.setExtendedState(MAXIMIZED_BOTH);
        model.getContourPlots().add(cp);

        cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Speed Contour Graph");
        model.getContourPlots().add(cp);

        cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("FLow Contour Graph");
        model.getContourPlots().add(cp);

        cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Acceleration Contour Graph");
        model.getContourPlots().add(cp);
        
        TablePanel charts = new TablePanel(2, 2);
        panel.addTab("statistics", charts);

        charts.setCell(model.getContourPlots().get(0).getContentPane(), 0, 0);
        charts.setCell(model.getContourPlots().get(1).getContentPane(), 1, 0);
        charts.setCell(model.getContourPlots().get(2).getContentPane(), 0, 1);
        charts.setCell(model.getContourPlots().get(3).getContentPane(), 1, 1);
    }

}
