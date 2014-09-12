package org.opentrafficsim.demo.ntm;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

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
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator */
    private OTSDEVSSimulatorInterface simulator;

    /** areas */
    private Map<java.lang.Long, Area> areas;

    /** nodes from shape file */
    private Map<java.lang.Long, ShpNode> shpNodes;

    /** links from shape file */
    private Map<java.lang.Long, ShpLink> shpLinks;

    /** the centroids */
    private Map<java.lang.Long, Point> centroids;

    /** graph containing the simplified network */
    SimpleWeightedGraph<Node, LinkEdge> areaGraph = new SimpleWeightedGraph<Node, LinkEdge>(LinkEdge.class);

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     */
    @Override
    public void constructModel(
            SimulatorInterface<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble> _simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) _simulator;
        try
        {
            // read the shape files
            this.centroids = ShapeFileReader.ReadCentroids("/gis/centroids.shp");
            this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            this.shpNodes = ShapeFileReader.ReadNodes("/gis/nodes.shp");
            this.shpLinks = ShapeFileReader.ReadLinks("/gis/links.shp", this.shpNodes);

            // build the higher level map and the graph
            buildGraph();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Build the graph using roads between touching areas.
     */
    private void buildGraph()
    {
        // iterate over the areas and find boundary areas
        for (Area area : this.areas.values())
        {
            for (Area area2 : this.areas.values())
            {
                try
                {
                    if (area != area2 && area.getGeometry().touches(area2.getGeometry()))
                    {
                        area.getTouchingAreas().add(area2);
                    }
                }
                catch (TopologyException te)
                {
                    System.out.println("TopologyException " + te.getMessage() + " when checking border of " + area
                            + " and " + area2);
                }
            }
        }

        // iterate over the areas and create nodes at the centroids and edges for touching areas with connecting roads
        for (Area area : this.areas.values())
        {
            Node centroid = new Node(area.getCentroid(), area);
            
        }
    }

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#getSimulator()
     */
    @Override
    public SimulatorInterface<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

}
