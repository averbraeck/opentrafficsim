package org.opentrafficsim.demo.ntm;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.JScrollPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.gui.swing.DSOLApplication;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

import org.opentrafficsim.core.dsol.OTSDEVSAnimator;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
public class NTMTestApplication extends DSOLApplication
{
    /**
     * @param title
     * @param panel
     */
    public NTMTestApplication(String title,
            DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
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
     */
    public static void main(String[] args) throws SimRuntimeException, RemoteException, NamingException, IOException
    {
        NTMModel model = new NTMModel();
        OTSDEVSAnimator simulator = new OTSDEVSAnimator();

        /*-
        try
        {
            String absolutePath =
                    new File(NTMTestApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                            + System.getProperty("file.separator");
            String backgroundMapFile = absolutePath + "gis" + System.getProperty("file.separator") + "map.xml";
            URL gisURL = new URL("file:/" + backgroundMapFile);
            System.err.println("GIS-map file: " + gisURL.toString());
            new GisRenderable2D(simulator, gisURL);
        }
        catch (MalformedURLException e)
        {
            throw new SimRuntimeException("map.xml not found", e);
        }
        */

        OTSReplication replication =
                new OTSReplication("rep1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model);
        simulator.initialize(replication, ReplicationMode.TERMINATING);

        DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel =
                new DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(model,
                        simulator);
        addInfoTab(panel);

        Rectangle2D extent = new Rectangle2D.Double(65000.0, 440000.0, 55000.0, 30000.0);
        Dimension size = new Dimension(1024, 768);
        AnimationPanel animationPanel = new AnimationPanel(extent, size, simulator);
        panel.getTabbedPane().addTab(0, "animation", animationPanel);

        // tell the animation panel to update its statistics
        // TODO: should be done automatically in DSOL!
        animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));

        new NTMTestApplication("Network Transmission Model", panel);
    }

    /**
     * @param panel
     */
    private static void addInfoTab(
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
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
    }
}
