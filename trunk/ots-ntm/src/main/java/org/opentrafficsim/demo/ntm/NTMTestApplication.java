package org.opentrafficsim.demo.ntm;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.demo.ntm.IO.ProjectConfigurations;
import org.opentrafficsim.simulationengine.SimpleAnimator;

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
public class NTMTestApplication extends DSOLApplication
{
    /**
     * @param title
     * @param panel
     */
    public NTMTestApplication(String title, DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel)
    {
        super(title, panel);
    }

    /** */
    private static final long serialVersionUID = 20140819L;

    /** */
    public static DSOLPanel<Time, Duration, SimTimeDoubleUnit> panel;

    public static JTextArea textArea;

    /**
     * @param args String[];
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        NTMModel model = new NTMModel();
        InputNTM inputNTM = new InputNTM();
        model.setInputNTM(inputNTM);
        // String startMap = "D:/gtamminga/workspace/ots-ntm/src/main/resources/gis/TheHague/";
        String startMap = "E:/java/opentrafficsim/workspace/ots-ntm/src/main/resources/gis/";
        // String startMap = System.getProperty("user.dir");
        ProjectConfigurations.readConfigurations(startMap, model);
        if (!new File(model.getInputNTM().getInputMap()).canRead())
        {
            model.getInputNTM().setInputMap(System.getProperty("user.dir"));
        }
        // model.getSettingsNTM().getStartTimeSinceMidnight().getInUnit(DurationUnit.SECOND)
        Time startTime = new Time(0.0, TimeUnit.BASE_SECOND);
        SimpleAnimator simulator = new SimpleAnimator(startTime, new Duration(0.0, DurationUnit.SECOND),
                new Duration(10800.0, DurationUnit.SECOND), model);
        // simulator.initialize(replication, ReplicationMode.TERMINATING);

        panel = new DSOLPanel<Time, Duration, SimTimeDoubleUnit>(model, simulator);
        addInfoTab(panel);

        Rectangle2D extent = new Rectangle2D.Double(65000.0, 440000.0, 55000.0, 30000.0);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, simulator);
        panel.getTabbedPane().addTab(0, "animation", animationPanel);
        String content = "The simulation starts with the import of alle data, and initializes the NTM model\n"
                + "Wait untill this process has finished...\n" + " \n";
        int index = panel.getTabbedPane().getSelectedIndex();
        textArea = new JTextArea(content);
        panel.getTabbedPane().setComponentAt(index, textArea);
        // tell the animation panel to update its statistics
        // TODO should be done automatically in DSOL!
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));
        // infoBox("Start initialization", "NTM");
        new NTMTestApplication("Network Transmission Model", panel);

        // infoBox("Ended initialization", "NTM");
        textArea.append("Finished the initialization,\n" + "Push the Start button now! \n" + " \n");

    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * @param panel DSOLPanel&lt;Time,Duration,SimTimeDoubleUnit&gt;;
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
