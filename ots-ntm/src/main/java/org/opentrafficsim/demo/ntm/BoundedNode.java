package org.opentrafficsim.demo.ntm;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.locationtech.jts.geom.Coordinate;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class BoundedNode extends NTMNode
{
    /** */
    private static final long serialrsionUID = 20140920L;

    /** */
    private Area area;

    /** */
    private CellBehaviour cellBehaviour;

    private static double zCoordinate(TrafficBehaviourType behaviourType)
    {
        if (null == behaviourType)
        {
            System.out.println("WTF");
        }
        switch (behaviourType)
        {
            case CENTROID:
                throw new Error("centroid does not have z value");
            case CORDON:
                return 3;
            case FLOW:
                return 4;
            case NTM:
                return 2;
            case ROAD:
                return 2;
            default:
                throw new Error("Unhandled BehaviourType: " + behaviourType);
        }
    }

    /**
     * /**
     * @param centroid Coordinate; the center of the area for the simplified graph.
     * @param nr String; Node number
     * @param area Area; the area to which the node belongs.
     * @param behaviourType TrafficBehaviourType; describes behaviour of the node depending on its type
     * @param parametersNTM
     * @throws NetworkException
     */
    public BoundedNode(final Network network, final Coordinate centroid, final String nr, final Area area,
            final TrafficBehaviourType behaviourType) throws NetworkException
    {
        super(network, nr, new Coordinate(centroid.x, centroid.y, zCoordinate(behaviourType)), behaviourType);
        this.area = area;

        if (behaviourType == TrafficBehaviourType.ROAD)
        {
            this.setCellBehaviour(new CellBehaviour());
        }
        else if (behaviourType == TrafficBehaviourType.NTM)
        {
            ParametersNTM parametersNTM = null;
            parametersNTM = new ParametersNTM(area.getAverageSpeed(), area.getRoadLength());
            this.setCellBehaviour(new CellBehaviourNTM(area, parametersNTM));
        }

        else if (behaviourType == TrafficBehaviourType.FLOW)
        {
            Speed speed = new Speed(80, SpeedUnit.KM_PER_HOUR);
            // TODO parameters should depend on area characteristics
            // Frequency maxCapacityPerLane = new Frequency(2000,
            // FrequencyUnit.PER_HOUR);
            // ParametersFundamentalDiagram parametersCTM = new ParametersFundamentalDiagram(speed, maxCapacityPerLane);
            ParametersFundamentalDiagram parametersCTM = new ParametersFundamentalDiagram();
            this.setCellBehaviour(new CellBehaviourFlow(area, parametersCTM));
        }

        else if (behaviourType == TrafficBehaviourType.CORDON)
        {
            this.setCellBehaviour(new CellBehaviourCordon());
        }

        else
        {
            this.setCellBehaviour(new CellBehaviour());
        }

    }

    /**
     * @return area.
     */
    public final Area getArea()
    {
        return this.area;
    }

    /**
     * @param area Area; set area.
     */
    public final void setArea(final Area area)
    {
        this.area = area;
    }

    /**
     * @return cellBehaviour.
     */
    public final CellBehaviour getCellBehaviour()
    {
        return this.cellBehaviour;
    }

    /**
     * @param cellBehaviour CellBehaviour; set cellBehaviour.
     */
    public final void setCellBehaviour(final CellBehaviour cellBehaviour)
    {
        this.cellBehaviour = cellBehaviour;
    }

}
