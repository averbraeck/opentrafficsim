package org.opentrafficsim.demo.carFollowing;

import java.awt.FileDialog;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
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
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
public class OpenStreetMap extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private OSMModel model;

    /** The OSMNetwork. */
    private OSMNetwork osmNetwork;

    /** The OTS network. */
    private OTSNetwork otsNetwork;

    /** The ProgressListener. */
    private ProgressListener progressListener;

    /** The WarningListener. */
    private WarningListener warningListener;

    /** Bounding rectangle of the loaded map. */
    Rectangle2D rectangle = null;

    /** Construct the OpenStreetMap demo. */
    public OpenStreetMap()
    {
        // The work is done in buildSimulator which, in turn, calls makeModel.
    }

    /**
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OpenStreetMap osm = new OpenStreetMap();
                    List<Property<?>> localProperties = osm.getProperties();
                    try
                    {
                        localProperties.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                                new Double[] { 0.8, 0.2 }, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    localProperties.add(new SelectionProperty("CarFollowingModel", "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>",
                            new String[] { "IDM", "IDM+" }, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car",
                            new Acceleration(1.0, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck",
                            new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 3));
                    osm.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(3600.0, SECOND), localProperties, null, true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
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
        Convert converter = new Convert();
        System.out.println("Opening file " + filename);
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
            System.out.println(filepath);
            this.progressListener = new ProgressListenerImpl();
            this.warningListener = new WarningListenerImpl();
            ReadOSMFile osmf = new ReadOSMFile(filepath, wantedTags, ft, this.progressListener);
            OSMNetwork net = osmf.getNetwork();
            // net.removeRedundancy(); // Defective; do not call removeRedundancy
            this.osmNetwork = net; // new OSMNetwork(net); // Why would you make a copy?
            this.otsNetwork = new OTSNetwork(this.osmNetwork.getName());
            for (OSMNode osmNode : this.osmNetwork.getNodes().values())
            {
                try
                {
                    this.otsNetwork.addNode(converter.convertNode(this.otsNetwork, osmNode));
                }
                catch (NetworkException ne)
                {
                    System.out.println(ne.getMessage());
                }
            }
            for (OSMLink osmLink : this.osmNetwork.getLinks())
            {
                // TODO OTS-256
                Link link = converter.convertLink(this.otsNetwork, osmLink, null);
                this.otsNetwork.addLink(link);
            }
            this.osmNetwork.makeLinks(this.warningListener, this.progressListener);
        }
        catch (URISyntaxException | IOException | NetworkException | OTSGeometryException exception)
        {
            exception.printStackTrace();
            return null;
        }
        this.model = new OSMModel(getUserModifiedProperties(), this.osmNetwork, this.warningListener, this.progressListener,
                converter);
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
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Open Street Map Demonstration";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Load an OpenStreetMap file and show it";
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final java.awt.geom.Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(this.rectangle.getX(), this.rectangle.getY(), this.rectangle.getWidth(),
                this.rectangle.getHeight());
    }
}

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
class OSMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150227L;

    /** The simulator. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** Provided Network. */
    private OSMNetwork osmNetwork;

    /** Provided lanes. */
    private List<Lane> lanes = new ArrayList<>();

    /** */
    private ProgressListener progressListener;

    /** */
    private WarningListener warningListener;

    /** The coordinate converter. */
    private final Convert converter;

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the properties (not used)
     * @param osmNetwork OSMNetwork; the OSM network structure
     * @param wL WarningListener; the receiver of warning events
     * @param pL ProgressListener; the receiver of progress events
     * @param converter Convert; the output converter
     */
    OSMModel(final List<Property<?>> properties, final OSMNetwork osmNetwork, final WarningListener wL,
            final ProgressListener pL, final Convert converter)
    {
        this.osmNetwork = osmNetwork;
        this.warningListener = wL;
        this.progressListener = pL;
        this.converter = converter;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> theSimulator)
            throws SimRuntimeException
    {
        OTSNetwork otsNetwork = new OTSNetwork(this.osmNetwork.getName());
        for (OSMNode osmNode : this.osmNetwork.getNodes().values())
        {
            try
            {
                otsNetwork.addNode(this.converter.convertNode(otsNetwork, osmNode));
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
                otsNetwork.addLink(this.converter.convertLink(otsNetwork, osmLink, this.simulator));
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
                this.lanes.addAll(this.converter.makeLanes(otsNetwork, link, (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator,
                        this.warningListener));
            }
            catch (Exception e)
            {
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
        /*
         * System.out.println("Number of Links: " + this.network.getLinks().size()); System.out.println("Number of Nodes: " +
         * this.network.getNodes().size()); System.out.println("Number of Lanes: " + this.lanes.size());
         */
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }
}
