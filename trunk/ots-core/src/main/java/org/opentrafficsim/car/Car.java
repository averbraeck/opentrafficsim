package org.opentrafficsim.car;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractLaneBasedIndividualGTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public class Car<ID> extends AbstractLaneBasedIndividualGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /**
     * @param id the id of the GTU, could be String or Integer
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel the following model, including a reference to the simulator
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction)
     * @param simulator the simulator
     * @throws RemoteException in case the simulation time cannot be read
     * @throws NamingException if an error occurs when adding the animation handler
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Car(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
        final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
        final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Rel<LengthUnit> length,
        final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
        final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException
    {
        // HACK FIXME (negative length trick)
        super(id, gtuType, gtuFollowingModel, initialLongitudinalPositions, initialSpeed, length.getSI() < 0
            ? new DoubleScalar.Rel<LengthUnit>(-length.getSI(), LengthUnit.METER) : length, width, maximumVelocity,
            simulator);
        if (simulator instanceof OTSAnimatorInterface && length.getSI() >= 0)
        {
            new CarAnimation(this, simulator);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getFront()
    {
        // We take the front position of the Car to be the reference point.
        DoubleScalar.Rel<LengthUnit> zero = new DoubleScalar.Rel<LengthUnit>(0.0d, LengthUnit.METER);
        return new RelativePosition(zero, zero, zero, RelativePosition.FRONT);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getRear()
    {
        // We take the front position of the Car to be the reference point. So the rear is the length
        // of the Car away from the reference point in the negative (non-driving) X-direction.
        DoubleScalar.Rel<LengthUnit> zero = new DoubleScalar.Rel<LengthUnit>(0.0d, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> dx = new DoubleScalar.Rel<LengthUnit>(-getLength().getSI(), LengthUnit.METER);
        return new RelativePosition(dx, zero, zero, RelativePosition.BACK);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        try
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> rearPositions = positions(getRear());
            Lane rearLane = rearPositions.keySet().iterator().next();
            Map<Lane, DoubleScalar.Rel<LengthUnit>> frontPositions = positions(getFront());
            Lane frontLane = frontPositions.keySet().iterator().next();
            return String.format("Car %s rear:%s[%s] front:%s[%s]", getId(), rearLane, rearPositions.get(rearLane),
                frontLane, frontPositions.get(frontLane));
        }
        catch (RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
        return "Caught exception in toString";
    }

}

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class CarAnimation extends Renderable2D
{
    /** Color of this car. */
    private final Color color;

    /** Counter used to cycle through the colors in colorTable. */
    private static int nextIndex = 0;

    /**
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public CarAnimation(final Car<?> source, final OTSSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = COLORTABLE[++nextIndex % COLORTABLE.length];
    }

    /**
     * Colors for the cars.
     */
    private static final Color[] COLORTABLE = {Color.BLACK, new Color(0xa5, 0x2a, 0x2a), Color.RED, Color.ORANGE,
        Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY};

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        final Car<?> car = (Car<?>) getSource();
        final double length = car.getLength().getSI();
        final double width = car.getWidth().getSI();
        graphics.setColor(this.color);
        graphics.draw(new Rectangle2D.Double(-length / 2, -width / 2, length, width));
        // Draw a 1m diameter white disk about 1m before the front to indicate which side faces forward
        graphics.setColor(Color.WHITE);
        graphics.draw(new Ellipse2D.Double(length / 2 - 1.5d, -0.5d, 1d, 1d));
    }

}
