package org.opentrafficsim.demo.ntm.trafficdemand;

import org.opentrafficsim.demo.ntm.AreaNode;


/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 15 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfo
{
    /** total number of Trips for this simulation. */
    private double numberOfTrips;

    /** the first AreaNode encountered on the path to Destination.*/
    private AreaNode neighbour;
    
    /** trips on their journey passing an area.*/
    private double passingTrips;

    /** flow to neighbour.*/
    private double flow;

    
    /**
     * @param numberOfTrips total number of Trips for this simulation
     */

    public TripInfo(final double numberOfTrips)
    {
        super();
        this.numberOfTrips = numberOfTrips;
    }

    /**
     * @return numberOfTrips
     */
    public final double getNumberOfTrips()
    {
        return this.numberOfTrips;
    }


    /**
     * @param numberOfTrips set numberOfTrips
     */
    public final void setNumberOfTrips(final double numberOfTrips)
    {
        this.numberOfTrips = numberOfTrips;
    }

    /**
     * @return neighbour.
     */
    public final AreaNode getNeighbour()
    {
        return this.neighbour;
    }

    /**
     * @param neighbour set neighbour.
     */
    public final void setNeighbour(final AreaNode neighbour)
    {
        this.neighbour = neighbour;
    }

    /**
     * @return passingTrips.
     */
    public final double getPassingTrips()
    {
        return this.passingTrips;
    }

    /**
     * @param passingTrips set passingTrips.
     */
    public final void setPassingTrips(final double passingTrips)
    {
        this.passingTrips = passingTrips;
    }
    
    /**
     * @param addTrips set passingTrips.
     */
    public final void addToPassingTrips(final double addTrips)
    {
        this.passingTrips += addTrips;
    }

    /**
     * @return flow.
     */
    public final double getFlow()
    {
        return this.flow;
    }

    /**
     * @param flow set flow.
     */
    public final void setFlow(final double flow)
    {
        this.flow = flow;
    }



}
