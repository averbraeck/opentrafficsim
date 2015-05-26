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

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools.STR;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.importexport.osm.OSMLink;
import org.opentrafficsim.importexport.osm.OSMNetwork;
import org.opentrafficsim.importexport.osm.OSMNode;
import org.opentrafficsim.importexport.osm.OSMTag;
import org.opentrafficsim.importexport.osm.events.ProgressEvent;
import org.opentrafficsim.importexport.osm.events.ProgressListener;
import org.opentrafficsim.importexport.osm.events.ProgressListenerImpl;
import org.opentrafficsim.importexport.osm.events.WarningListener;
import org.opentrafficsim.importexport.osm.events.WarningListenerImpl;
import org.opentrafficsim.importexport.osm.input.ReadOSMFile;
import org.opentrafficsim.importexport.osm.output.Convert;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.PropertyException;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.SimpleSimulation;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
public class OpenStreetMap implements WrappableSimulation
{
    /** The properties after (possible) editing by the user. */
    private ArrayList<AbstractProperty<?>> savedUserModifiedProperties;

    /** The OSMNetwork. */
    private OSMNetwork osmNetwork;

    /** The OTS network. */
    private Network<String, CrossSectionLink<?, ?>> otsNetwork;

    /** The properties of this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** The ProgressListener. */
    private ProgressListener progressListener;

    /** The WarningListener. */
    private WarningListener warningListener;

    /** Construct the OpenStreetMap demo. */
    public OpenStreetMap()
    {
        // The work is done in buildSimulator.
    }

    /**
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    OpenStreetMap osm = new OpenStreetMap();
                    ArrayList<AbstractProperty<?>> localProperties = osm.getProperties();
                    try
                    {
                        localProperties.add(new ProbabilityDistributionProperty("Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>",
                                new String[]{"passenger car", "truck"}, new Double[]{0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    localProperties.add(new SelectionProperty("Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Car",
                            new DoubleScalar.Abs<AccelerationUnit>(1.0, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(
                                    1.0, TimeUnit.SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Truck",
                            new DoubleScalar.Abs<AccelerationUnit>(0.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.25, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(
                                    1.0, TimeUnit.SECOND), 3));
                    new SimulatorFrame("OpenStreetMap animation", osm.buildSimulator(localProperties).getPanel());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    /** {@inheritDoc} 
     * @throws NamingException */
    @Override
    public final SimpleAnimator buildSimulator(final ArrayList<AbstractProperty<?>> usedProperties)
            throws SimRuntimeException, RemoteException, NetworkException, NamingException
    {
        this.savedUserModifiedProperties = usedProperties;
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
        ArrayList<OSMTag> wantedTags =
                new ArrayList<OSMTag>();
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
        ArrayList<String> ft = new ArrayList<String>();
        try
        {
            System.out.println(filepath);
            this.progressListener = new ProgressListenerImpl();
            this.warningListener = new WarningListenerImpl();
            ReadOSMFile osmf = new ReadOSMFile(filepath, wantedTags, ft, this.progressListener);
            OSMNetwork net = osmf.getNetwork();
            // net.removeRedundancy(); // Defective; do not call removeRedundancy
            this.osmNetwork = net; // new OSMNetwork(net); // Why would you make a copy?
            this.otsNetwork = new Network<String, CrossSectionLink<?, ?>>(this.osmNetwork.getName());
            for (OSMNode osmNode : this.osmNetwork.getNodes().values())
            {
                try
                {
                    this.otsNetwork.addNode(converter.convertNode(osmNode));
                }
                catch (NetworkException ne)
                {
                    System.out.println(ne.getMessage());
                }
            }
            for (OSMLink osmLink : this.osmNetwork.getLinks())
            {
                this.otsNetwork.add(converter.convertLink(osmLink));
            }
            this.osmNetwork.makeLinks(this.warningListener, this.progressListener);
        }
        catch (URISyntaxException | IOException exception)
        {
            exception.printStackTrace();
            return null;
        }
        OSMModel model =
                new OSMModel(usedProperties, this.osmNetwork, this.warningListener, this.progressListener, converter);
        Iterator<Node<?, ?>> count = this.otsNetwork.getNodeSet().iterator();
        Rectangle2D area = null;
        while (count.hasNext())
        {
            NodeGeotools.STR node = (STR) count.next();
            if (null == area)
            {
                area = new Rectangle2D.Double(node.getX(), node.getY(), 0, 0);
            }
            else
            {
                area = area.createUnion(new Rectangle2D.Double(node.getX(), node.getY(), 0, 0));
            }
        }
        SimpleAnimator result =
                new SimpleAnimator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model, area);
        new ControlPanel(result, this);
        return result;
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
    public final ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

    /** {@inheritDoc} */
    @Override
    public SimpleSimulation rebuildSimulator() throws SimRuntimeException, RemoteException, NetworkException,
            NamingException
    {
        return buildSimulator(this.savedUserModifiedProperties);
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getUserModifiedProperties()
    {
        return this.savedUserModifiedProperties;
    }

}

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
class OSMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150227L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** User settable properties. */
    private List<AbstractProperty<?>> properties = null;

    /** Provided Network. */
    private OSMNetwork osmNetwork;

    /** Provided lanes. */
    private List<Lane> lanes = new ArrayList<Lane>();

    /** */
    private ProgressListener progressListener;

    /** */
    private WarningListener warningListener;
    
    /** The coordinate converter. */
    private final Convert converter;

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the properties
     * @param osmNetwork OSMNetwork; the OSM network structure
     * @param wL WarningListener; the receiver of warning events
     * @param pL ProgressListener; the receiver of progress events
     * @param converter Convert; the output converter
     */
    public OSMModel(final ArrayList<AbstractProperty<?>> properties, final OSMNetwork osmNetwork,
            final WarningListener wL, final ProgressListener pL, final Convert converter)
    {
        this.properties = new ArrayList<AbstractProperty<?>>(properties);
        this.osmNetwork = osmNetwork;
        this.warningListener = wL;
        this.progressListener = pL;
        this.converter = converter;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        Network<String, CrossSectionLink<?, ?>> otsNetwork =
                new Network<String, CrossSectionLink<?, ?>>(this.osmNetwork.getName());
        for (OSMNode osmNode : this.osmNetwork.getNodes().values())
        {
            try
            {
                otsNetwork.addNode(this.converter.convertNode(osmNode));
            }
            catch (NetworkException ne)
            {
                System.out.println(ne.getMessage());
            }
        }
        for (OSMLink osmLink : this.osmNetwork.getLinks())
        {
            otsNetwork.add(this.converter.convertLink(osmLink));
        }
        Convert.findSinksandSources(this.osmNetwork, this.progressListener);
        this.progressListener.progress(new ProgressEvent(this.osmNetwork, "Creation the lanes on "
                + this.osmNetwork.getLinks().size() + " links"));
        double total = this.osmNetwork.getLinks().size();
        double counter = 0;
        double nextPercentage = 5.0;
        for (OSMLink link : this.osmNetwork.getLinks())
        {
            try
            {
                this.lanes.addAll(this.converter.makeLanes(link, (OTSDEVSSimulatorInterface) theSimulator,
                        this.warningListener));
            }
            catch (NetworkException | NamingException exception)
            {
                exception.printStackTrace();
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
         * System.out.println("Number of Links: " + this.network.getLinks().size());
         * System.out.println("Number of Nodes: " + this.network.getNodes().size());
         * System.out.println("Number of Lanes: " + this.lanes.size());
         */
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }

}
