package org.opentrafficsim.core.network.geotools;

import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> Name of the node
 */
public class NodeGeotools<ID> extends AbstractNode<ID, Coordinate>
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /**
     * Construct a new Node.
     * @param id ID; the Id of the new Node
     * @param coordinate P; the location of the new Node
     */
    public NodeGeotools(final ID id, final Coordinate coordinate)
    {
        super(id, coordinate);
    }

    /**
     * Construct a new Node.
     * @param id ID; the Id of the new Node
     * @param coordinate P; the location of the new Node
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     */
    public NodeGeotools(final ID id, final Coordinate coordinate, final DoubleScalar.Abs<AnglePlaneUnit> direction,
        final DoubleScalar.Abs<AngleSlopeUnit> slope)
    {
        super(id, coordinate, direction, slope);
    }

    /** {@inheritDoc} */
    @Override
    public final double getX()
    {
        return getPoint().x;
    }

    /** {@inheritDoc} */
    @Override
    public final double getY()
    {
        return getPoint().y;
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return getPoint().z;
    }

    /**
     * String ID implementation of the Geotools node.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends NodeGeotools<String>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id String id.
         * @param coordinate the location of the Node.
         */
        public STR(final String id, final Coordinate coordinate)
        {
            super(id, coordinate);
        }
        
        /**
         * Construct a new Node.
         * @param id ID; the String Id of the new Node
         * @param coordinate P; the location of the new Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public STR(final String id, final Coordinate coordinate, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, coordinate, direction, slope);
        }
    }

    /**
     * Integer ID implementation of the Geotools node.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends NodeGeotools<Integer>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id Integer id.
         * @param coordinate the location of the Node.
         */
        public INT(final int id, final Coordinate coordinate)
        {
            super(id, coordinate);
        }

        /**
         * Construct a new Node.
         * @param id ID; the Integer Id of the new Node
         * @param coordinate P; the location of the new Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public INT(final int id, final Coordinate coordinate, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, coordinate, direction, slope);
        }
    }

}
