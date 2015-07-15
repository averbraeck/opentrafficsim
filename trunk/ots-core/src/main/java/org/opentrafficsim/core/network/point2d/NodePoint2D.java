package org.opentrafficsim.core.network.point2d;

import java.awt.geom.Point2D;

import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version11 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> Type of the node name (e.g., String, Integer).
 */
public class NodePoint2D<ID> extends AbstractNode<ID, Point2D>
{
    /** */
    private static final long serialVersionUID = 20150104L;

    /**
     * Construct a new Node.
     * @param id the Id of the new Node
     * @param point the location of the Node
     */
    public NodePoint2D(final ID id, final Point2D point)
    {
        super(id, point);
    }

    /**
     * Construct a new Node.
     * @param id the Id of the new Node
     * @param point the location of the Node
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     */
    public NodePoint2D(final ID id, final Point2D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
    {
        super(id, point, direction, slope);
    }

    /** {@inheritDoc} */
    @Override
    public final double getX()
    {
        return getPoint().getX();
    }

    /** {@inheritDoc} */
    @Override
    public final double getY()
    {
        return getPoint().getY();
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return 0.0d;
    }

    /**
     * String ID implementation of the Point2D node.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial versionJan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends NodePoint2D<String>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id String id.
         * @param point the location of the Node.
         */
        public STR(final String id, final Point2D point)
        {
            super(id, point);
        }

        /**
         * @param id the String Id of the new Node
         * @param point the location of the Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public STR(final String id, final Point2D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
                final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, point, direction, slope);
        }

    }

    /**
     * Integer ID implementation of the Point2D node.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial versionJan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends NodePoint2D<Integer>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id Integer id.
         * @param point the location of the Node.
         */
        public INT(final int id, final Point2D point)
        {
            super(id, point);
        }

        /**
         * @param id the Integer Id of the new Node
         * @param point the location of the Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public INT(final int id, final Point2D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
                final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, point, direction, slope);
        }

    }

}
