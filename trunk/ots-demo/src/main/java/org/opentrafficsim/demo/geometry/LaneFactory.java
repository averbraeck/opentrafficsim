package org.opentrafficsim.demo.geometry;

import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.IDMPlus.swing.animation.Link;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 okt. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class LaneFactory
{
    /** Do not instantiate this class. */
    private LaneFactory()
    {
        // Cannot be instantiated.
    }

    /**
     * Create a simple, straight Lane.
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @return Lane; the new Lane
     */
    public static Lane makeLane(final String name, final Node from, final Node to)
    {
        Link link =
                new Link(name, from, to, new DoubleScalar.Rel<LengthUnit>(from.getPoint().distance(to.getPoint()),
                        LengthUnit.METER));
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates =
                new Coordinate[]{new Coordinate(from.getPoint().x, from.getPoint().y, 0),
                        new Coordinate(to.getPoint().x, to.getPoint().y, 0)};
        LineString line = factory.createLineString(coordinates);
        try
        {
            new LinearGeometry(link, line, null);
        }
        catch (NetworkException exception)
        {
            throw new Error("This network is probably too simple for this to happen...");
        }
        LaneType<String> carLaneType = new LaneType<String>(name);
        DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        return new Lane(link, latPos, width, width, carLaneType, LongitudinalDirectionality.FORWARD, f2000);
    }

}
