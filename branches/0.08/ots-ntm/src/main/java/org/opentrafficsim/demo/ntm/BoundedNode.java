package org.opentrafficsim.demo.ntm;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class BoundedNode extends Node
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
     * @param centroid the center of the area for the simplified graph.
     * @param nr Node number
     * @param area the area to which the node belongs.
     * @param behaviourType describes behaviour of the node depending on its type
     * @param parametersNTM
     */
    public BoundedNode(final Coordinate centroid, final String nr, final Area area, final TrafficBehaviourType behaviourType)
    {
        super(nr, new Coordinate(centroid.x, centroid.y, zCoordinate(behaviourType)), behaviourType);
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
            DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(80, SpeedUnit.KM_PER_HOUR);
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
     * @param area set area.
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
     * @param cellBehaviour set cellBehaviour.
     */
    public final void setCellBehaviour(final CellBehaviour cellBehaviour)
    {
        this.cellBehaviour = cellBehaviour;
    }

}
