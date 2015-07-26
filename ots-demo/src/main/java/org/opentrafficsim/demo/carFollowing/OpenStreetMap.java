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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
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
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Feb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
public class OpenStreetMap extends AbstractWrappableSimulation
{
    /** the model. */
    private OSMModel model;

    /** The OSMNetwork. */
    private OSMNetwork osmNetwork;

    /** The OTS network. */
    private OTSNetwork<String, String, String> otsNetwork;

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
                    osm.buildSimulator(localProperties, null, true);
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
    protected OTSModelInterface makeModel(GTUColorer colorer)
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
        ArrayList<OSMTag> wantedTags = new ArrayList<OSMTag>();
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
            this.otsNetwork =
                    new OTSNetwork<String, String, String>(this.osmNetwork.getName());
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
                @SuppressWarnings("unchecked")
                Link<String, String> link = (Link<String, String>) converter.convertLink(osmLink);
                this.otsNetwork.addLink(link);
            }
            this.osmNetwork.makeLinks(this.warningListener, this.progressListener);
        }
        catch (URISyntaxException | IOException | NetworkException exception)
        {
            exception.printStackTrace();
            return null;
        }
        this.model =
                new OSMModel(getUserModifiedProperties(), this.osmNetwork, this.warningListener, this.progressListener,
                        converter);
        Iterator<Node<String>> count = this.otsNetwork.getNodeMap().values().iterator();
        Rectangle2D area = null;
        while (count.hasNext())
        {
            Node<String> node = count.next();
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
    protected JPanel makeCharts()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected java.awt.geom.Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(this.rectangle.getX(), this.rectangle.getY(), this.rectangle.getWidth(),
                this.rectangle.getHeight());
    }
}

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version eb 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Moritz Bergmann
 */
class OSMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150227L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** Provided Network. */
    private OSMNetwork osmNetwork;

    /** Provided lanes. */
    private List<Lane<?, ?>> lanes = new ArrayList<Lane<?, ?>>();

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
    public OSMModel(final ArrayList<AbstractProperty<?>> properties, final OSMNetwork osmNetwork,
            final WarningListener wL, final ProgressListener pL, final Convert converter)
    {
        this.osmNetwork = osmNetwork;
        this.warningListener = wL;
        this.progressListener = pL;
        this.converter = converter;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        try
        {
        OTSNetwork<String, String, String> otsNetwork =
                new OTSNetwork<String, String, String>(this.osmNetwork.getName());
        for (OSMNode osmNode : this.osmNetwork.getNodes().values())
        {
                otsNetwork.addNode(this.converter.convertNode(osmNode));
        }
        for (OSMLink osmLink : this.osmNetwork.getLinks())
        {
            otsNetwork.addLink((Link<String, String>) this.converter.convertLink(osmLink));
        }
        Convert.findSinksandSources(this.osmNetwork, this.progressListener);
        this.progressListener.progress(new ProgressEvent(this.osmNetwork, "Creation the lanes on "
                + this.osmNetwork.getLinks().size() + " links"));
        double total = this.osmNetwork.getLinks().size();
        double counter = 0;
        double nextPercentage = 5.0;
        for (OSMLink link : this.osmNetwork.getLinks())
        {
                this.lanes.addAll(this.converter.makeLanes(link, (OTSDEVSSimulatorInterface) theSimulator,
                        this.warningListener));
            counter++;
            double currentPercentage = counter / total * 100;
            if (currentPercentage >= nextPercentage)
            {
                this.progressListener.progress(new ProgressEvent(this, nextPercentage + "% Progress"));
                nextPercentage += 5.0D;
                }
            }
        }
        catch (NetworkException | NamingException | OTSGeometryException ne)
        {
            System.out.println(ne.getMessage());
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
