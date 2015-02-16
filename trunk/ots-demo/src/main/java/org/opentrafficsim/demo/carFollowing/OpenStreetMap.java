package org.opentrafficsim.demo.carFollowing;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;
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
import org.opentrafficsim.importexport.osm.input.ReadOSMFile;
import org.opentrafficsim.importexport.osm.output.Convert;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.PropertyException;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class OpenStreetMap implements WrappableSimulation
{
    /** The OSM network that this Simulation is supposed to draw. */
    private org.opentrafficsim.importexport.osm.Network networkOSM;
    
    /** The OTS network created out of the OSM network. */
    
    private Network<String, CrossSectionLink<?, ?>> networkOTS;
    
    /** The properties of this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /**
     */
    public OpenStreetMap()
    {
        ArrayList<org.opentrafficsim.importexport.osm.Tag> wt = new ArrayList<org.opentrafficsim.importexport.osm.Tag>();
        org.opentrafficsim.importexport.osm.Tag t1 = new org.opentrafficsim.importexport.osm.Tag("highway", "primary");
        wt.add(t1);
        org.opentrafficsim.importexport.osm.Tag t2 = new org.opentrafficsim.importexport.osm.Tag("highway", "secondary");
        wt.add(t2);
        org.opentrafficsim.importexport.osm.Tag t3 = new org.opentrafficsim.importexport.osm.Tag("highway", "tertiary");
        wt.add(t3);
        org.opentrafficsim.importexport.osm.Tag t4 = new org.opentrafficsim.importexport.osm.Tag("highway", "cycleway");
        wt.add(t4);
        org.opentrafficsim.importexport.osm.Tag t5 = new org.opentrafficsim.importexport.osm.Tag("highway", "trunk");
        wt.add(t5);
        org.opentrafficsim.importexport.osm.Tag t6 = new org.opentrafficsim.importexport.osm.Tag("highway", "path");
        wt.add(t6);
        org.opentrafficsim.importexport.osm.Tag t8 = new org.opentrafficsim.importexport.osm.Tag("cyclway", "lane");
        wt.add(t8);
        org.opentrafficsim.importexport.osm.Tag t9 = new org.opentrafficsim.importexport.osm.Tag("highway", "residental");
        wt.add(t9);
        org.opentrafficsim.importexport.osm.Tag t10 = new org.opentrafficsim.importexport.osm.Tag("highway", "service");
        wt.add(t10);
        org.opentrafficsim.importexport.osm.Tag t11 = new org.opentrafficsim.importexport.osm.Tag("highway", "motorway");
        wt.add(t11);
        org.opentrafficsim.importexport.osm.Tag t12 = new org.opentrafficsim.importexport.osm.Tag("highway", "bus_stop");
        wt.add(t12);
        org.opentrafficsim.importexport.osm.Tag t13 = new org.opentrafficsim.importexport.osm.Tag("highway", "motorway_link");
        wt.add(t13);
        org.opentrafficsim.importexport.osm.Tag t14 = new org.opentrafficsim.importexport.osm.Tag("highway", "unclassified");
        wt.add(t14);
        
        ArrayList<String> ft = new ArrayList<String>();
        try
        {
        ReadOSMFile osmf = new ReadOSMFile("file:///home/moe/Documents/TUD/A3.osm.bz2", wt, ft);
        org.opentrafficsim.importexport.osm.Network net = osmf.getNetwork();
        net.makeLinks();
        //net.removeRedundancy();
        this.networkOSM = new org.opentrafficsim.importexport.osm.Network(net);
        this.networkOTS = new Network<String, CrossSectionLink<?, ?>>(this.networkOSM.getName());
        for (org.opentrafficsim.importexport.osm.Node osmNode: this.networkOSM.getNodes().values())
        {
            try
            {
                this.networkOTS.addNode(Convert.convertNode(osmNode));
            }
            catch (NetworkException ne)
            {
                System.out.println(ne.getMessage());
            }
        }
        for (org.opentrafficsim.importexport.osm.Link osmLink: this.networkOSM.getLinks())
        {
            this.networkOTS.add(Convert.convertLink(osmLink));
        }
        }
        catch (URISyntaxException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
    
    /**
     * @param args 
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    OpenStreetMap oSM = new OpenStreetMap();
                    ArrayList<AbstractProperty<?>> localProperties = oSM.getProperties();
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
                    new SimulatorFrame("Contour Plots animation", oSM.buildSimulator(localProperties).getPanel());
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
    public SimpleSimulator buildSimulator(final ArrayList<AbstractProperty<?>> usedProperties) throws SimRuntimeException,
            RemoteException, NetworkException
    {
        OSMModel model = new OSMModel(usedProperties, this.networkOSM);
        Iterator<Node<?, ?>> count = this.networkOTS.getNodeSet().iterator();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 0, 0);
        while (count.hasNext())
        {
            NodeGeotools.STR node = (STR) count.next();
            if (area.equals(new Rectangle2D.Double(0, 0, 0, 0)))
            {
                area = new Rectangle2D.Double(node.getX(), node.getY(), 0, 0);
            }
            else
            {
                area = area.createUnion(new Rectangle2D.Double(node.getX(), node.getY(), 0, 0));
            }
        }
        
        SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model, area);
        new ControlPanel(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Open Street Map Simulation";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "Depicts a Map imported from Open Street Maps";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

}

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
class OSMModel implements OTSModelInterface
{
    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;
    
    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;
    
    /** Provided Network. */
    private org.opentrafficsim.importexport.osm.Network network;
    
    /** Provided links. */
    private ArrayList<CrossSectionLink> links;
    
    /** Provided lanes. */
    private ArrayList<Lane> lanes;
    
    /**
     * @param properties 
     * @param net 
     */
    public OSMModel(final ArrayList<AbstractProperty<?>> properties, final org.opentrafficsim.importexport.osm.Network net)
    {
        this.network = new org.opentrafficsim.importexport.osm.Network(net);
        this.links = new ArrayList<CrossSectionLink>();
        this.lanes = new ArrayList<Lane>();
        this.properties = new ArrayList<AbstractProperty<?>>(properties);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        for (org.opentrafficsim.importexport.osm.Link l: this.network.getLinks())
        {
            try
            {
                Convert.makeLanes(l, this.simulator);
            }
            catch (NetworkException | NamingException exception)
            {
                exception.printStackTrace();
            }
        }
    }
    
    /**
     * @param theSimulator 
     * @param errorMessage 
     */
    public void stopSimulator(final OTSDEVSSimulatorInterface theSimulator, final String errorMessage)
    {
        System.out.println("Error: " + errorMessage);
        try
        {
            if (theSimulator.isRunning())
            {
                theSimulator.stop();
            }
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        throw new Error(errorMessage);
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }
    
}