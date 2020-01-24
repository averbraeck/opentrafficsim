package org.opentrafficsim.demo.carFollowing;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.JFrame;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMLink;
import org.opentrafficsim.road.network.factory.osm.OSMNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMNode;
import org.opentrafficsim.road.network.factory.osm.OSMTag;
import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListenerImpl;
import org.opentrafficsim.road.network.factory.osm.events.WarningListener;
import org.opentrafficsim.road.network.factory.osm.events.WarningListenerImpl;
import org.opentrafficsim.road.network.factory.osm.input.ReadOSMFile;
import org.opentrafficsim.road.network.factory.osm.output.Convert;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
public class OpenStreetMap extends OTSSimulationApplication<OSMModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the OpenStreetMap demo.
     * @param model OSMModel; the model
     * @param panel OTSAnimationPanel; the Swing panel
     * @throws OTSDrawingException on animation error
     */
    public OpenStreetMap(final OSMModel model, final OTSAnimationPanel panel) throws OTSDrawingException
    {
        super(model, panel);
    }

    /** {@inheritDoc} */
    @Override
    protected void setAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesFull(getAnimationPanel());
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            String filepath = chooseFile();
            if (filepath != null)
            {
                OTSAnimator simulator = new OTSAnimator("OpenStreetMap");
                final OSMModel osmModel = new OSMModel(simulator, filepath);
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), osmModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(osmModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, osmModel, DEFAULT_COLORER, osmModel.getNetwork());
                OpenStreetMap app = new OpenStreetMap(osmModel, animationPanel);
                app.setExitOnClose(exitOnClose);
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * choose a file and construct the model.
     * @return a model based on a given file
     */
    protected static final String chooseFile()
    {
        JFrame frame = new JFrame();
        FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
        fd.setFile("*.osm");
        fd.setVisible(true);
        File[] file = fd.getFiles();
        if (file.length == 0)
        {
            return null;
        }
        String filename = fd.getFile();
        String filepath = null;
        try
        {
            filepath = file[0].toURI().toURL().toString();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        if (filename == null)
        {
            System.out.println("You cancelled the choice");
            return null;
        }
        return filepath;
    }
}

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
class OSMModel extends AbstractOTSModel
{
    /** */
    private static final long serialVersionUID = 20150227L;

    /** Provided Network. */
    private OSMNetwork osmNetwork;

    /** Provided lanes. */
    private List<Lane> lanes = new ArrayList<>();

    /** The OTS network. */
    private OTSRoadNetwork otsNetwork = new OTSRoadNetwork("network", true);

    /** The ProgressListener. */
    private ProgressListener progressListener;

    /** The WarningListener. */
    private WarningListener warningListener;

    /** The coordinate converter. */
    private Convert converter;

    /** Bounding rectangle of the loaded map. */
    private Rectangle2D rectangle = null;

    /** the file path. */
    private String filepath;

    /**
     * @param simulator OTSSimulatorInterface; the simulator
     * @param filepath String; the path to the OSM file
     */
    OSMModel(final OTSSimulatorInterface simulator, final String filepath)
    {
        super(simulator);
        this.filepath = filepath;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        this.converter = new Convert();
        System.out.println("Opening file " + this.filepath);
        ArrayList<OSMTag> wantedTags = new ArrayList<>();
        wantedTags.add(new OSMTag("highway", "primary"));
        wantedTags.add(new OSMTag("highway", "secondary"));
        wantedTags.add(new OSMTag("highway", "tertiary"));
        wantedTags.add(new OSMTag("highway", "cycleway"));
        wantedTags.add(new OSMTag("highway", "trunk"));
        wantedTags.add(new OSMTag("highway", "path"));
        wantedTags.add(new OSMTag("cycleway", "lane"));
        wantedTags.add(new OSMTag("highway", "residential"));
        wantedTags.add(new OSMTag("highway", "service"));
        wantedTags.add(new OSMTag("highway", "motorway"));
        wantedTags.add(new OSMTag("highway", "bus_stop"));
        wantedTags.add(new OSMTag("highway", "motorway_link"));
        wantedTags.add(new OSMTag("highway", "unclassified"));
        wantedTags.add(new OSMTag("highway", "footway"));
        wantedTags.add(new OSMTag("cycleway", "track"));
        wantedTags.add(new OSMTag("highway", "road"));
        wantedTags.add(new OSMTag("highway", "pedestrian"));
        wantedTags.add(new OSMTag("highway", "track"));
        wantedTags.add(new OSMTag("highway", "living_street"));
        wantedTags.add(new OSMTag("highway", "tertiary_link"));
        wantedTags.add(new OSMTag("highway", "secondary_link"));
        wantedTags.add(new OSMTag("highway", "primary_link"));
        wantedTags.add(new OSMTag("highway", "trunk_link"));
        ArrayList<String> ft = new ArrayList<>();
        try
        {
            System.out.println(this.filepath);
            this.progressListener = new ProgressListenerImpl();
            this.warningListener = new WarningListenerImpl();
            ReadOSMFile osmf = new ReadOSMFile(this.filepath, wantedTags, ft, this.progressListener);
            OSMNetwork net = osmf.getNetwork();
            // net.removeRedundancy(); // Defective; do not call removeRedundancy
            this.osmNetwork = net; // new OSMNetwork(net); // Why would you make a copy?
            this.otsNetwork = new OTSRoadNetwork(this.osmNetwork.getName(), true);
            for (OSMNode osmNode : this.osmNetwork.getNodes().values())
            {
                try
                {
                    this.converter.convertNode(this.otsNetwork, osmNode);
                }
                catch (NetworkException ne)
                {
                    System.out.println(ne.getMessage());
                }
            }
            for (OSMLink osmLink : this.osmNetwork.getLinks())
            {
                // TODO OTS-256
                Link link = this.converter.convertLink(this.otsNetwork, osmLink, null);
                this.otsNetwork.addLink(link);
            }
            this.osmNetwork.makeLinks(this.warningListener, this.progressListener);
        }
        catch (URISyntaxException | IOException | NetworkException | OTSGeometryException exception)
        {
            exception.printStackTrace();
            return;
        }

        Iterator<Node> count = this.otsNetwork.getNodeMap().values().iterator();
        Rectangle2D area = null;
        while (count.hasNext())
        {
            Node node = count.next();
            if (null == area)
            {
                area = new Rectangle2D.Double(node.getPoint().x, node.getPoint().y, 0, 0);
            }
            else
            {
                area = area.createUnion(new Rectangle2D.Double(node.getPoint().x, node.getPoint().y, 0, 0));
            }
        }
        this.rectangle = area;

        this.otsNetwork = new OTSRoadNetwork(this.osmNetwork.getName(), true);
        for (OSMNode osmNode : this.osmNetwork.getNodes().values())
        {
            try
            {
                this.converter.convertNode(this.otsNetwork, osmNode);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
        for (OSMLink osmLink : this.osmNetwork.getLinks())
        {
            try
            {
                this.converter.convertLink(this.otsNetwork, osmLink, this.simulator);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
        Convert.findSinksandSources(this.osmNetwork, this.progressListener);
        this.progressListener.progress(
                new ProgressEvent(this.osmNetwork, "Creation the lanes on " + this.osmNetwork.getLinks().size() + " links"));
        double total = this.osmNetwork.getLinks().size();
        double counter = 0;
        double nextPercentage = 5.0;
        for (OSMLink link : this.osmNetwork.getLinks())
        {
            try
            {
                this.lanes.addAll(this.converter.makeLanes(this.otsNetwork, link, this.simulator, this.warningListener));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
            counter++;
            double currentPercentage = counter / total * 100;
            if (currentPercentage >= nextPercentage)
            {
                this.progressListener.progress(new ProgressEvent(this, nextPercentage + "% Progress"));
                nextPercentage += 5.0D;
            }
        }
        System.out.println("Number of Links: " + this.otsNetwork.getLinkMap().size());
        System.out.println("Number of Nodes: " + this.otsNetwork.getNodeMap().size());
        System.out.println("Number of Lanes: " + this.lanes.size());
    }

    /** {@inheritDoc} */
    @Override
    public OTSRoadNetwork getNetwork()
    {
        return this.otsNetwork;
    }

    /**
     * @return rectangle
     */
    public final Rectangle2D getRectangle()
    {
        return this.rectangle;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "OSMModel";
    }
}
