package org.opentrafficsim.demo.IDMPlus.swing;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;

import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.FundamentalDiagram;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramPlotsSwingApplication extends DSOLApplication
{
    /** */
    private static final long serialVersionUID = 20140820L;

    /**
     * @param title String
     * @param panel DSOLPanel
     */
    public FundamentalDiagramPlotsSwingApplication(final String title,
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        super(title, panel);
    }

    /** the car following model, e.g. IDM Plus. */
    protected CarFollowingModel carFollowingModel;

    /** the fundamental diagram plots. */
    private ArrayList<FundamentalDiagram> fundamentalDiagramPlots = new ArrayList<FundamentalDiagram>();

    /**
     * @return fundamentalDiagramPlots
     */
    public final ArrayList<FundamentalDiagram> getFundamentalDiagramPlots()
    {
        return this.fundamentalDiagramPlots;
    }

    /**
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    @SuppressWarnings("unused")
    public static void main(final String[] args) throws SimRuntimeException, RemoteException
    {
        FundamentalDiagramPlotsModel model = new FundamentalDiagramPlotsModel();
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        OTSReplication replication =
                new OTSReplication("rep1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);
        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model,
                        simulator);
        makePlots(model, panel);
        addInfoTab(panel);
        new FundamentalDiagramPlotsSwingApplication("IDM-plus model - Fundamental Diagrams", panel);
    }

    /**
     * make the stand-alone plots for the model and put them in the statistics panel.
     * @param model FundamentalDiagramPlotsModel; the model.
     * @param panel DSOLPanel
     */
    private static void makePlots(final FundamentalDiagramPlotsModel model,
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        final int panelsPerRow = 3;
        TablePanel charts = new TablePanel(4, panelsPerRow);
        panel.getTabbedPane().addTab("statistics", charts);
        for (int plotNumber = 0; plotNumber < 10; plotNumber++)
        {
            DoubleScalar.Abs<LengthUnit> detectorLocation =
                    new DoubleScalar.Abs<LengthUnit>(400 + 500 * plotNumber, LengthUnit.METER);
            FundamentalDiagram fd =
                    new FundamentalDiagram("Fundamental Diagram at " + detectorLocation.getSI() + "m", 1,
                            new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.MINUTE), detectorLocation);
            fd.setTitle("Density Contour Graph");
            fd.setExtendedState(MAXIMIZED_BOTH);
            model.getFundamentalDiagrams().add(fd);
            charts.setCell(fd.getContentPane(), plotNumber / panelsPerRow, plotNumber % panelsPerRow);
        }
    }

    /**
     * @param panel DSOLPanel
     */
    private static void addInfoTab(
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
        String helpSource = "/" + ContourPlotsModel.class.getPackage().getName().replace('.', '/') + "/package.html";
        URL page = ContourPlotsModel.class.getResource(helpSource);
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
