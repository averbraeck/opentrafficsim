package org.opentrafficsim.demo.IDMPlus.swing.animation;

import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.NamingException;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AnimatedCar extends Car<Integer>
{
    /** */
    private static final long serialVersionUID = 20141003L;

    /**
     * Create a new Car with animation in case we schedule on an Animator.
     * @param id integer; the id of the new Car
     * @param simulator OTSDEVSSimulator
     * @param carFollowingModel CarFollowingModel; the car following model used by the new Car
     * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the first evaluation time of the new Car
     * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane positions of the new Car
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new Car
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public AnimatedCar(final int id, final OTSDEVSSimulatorInterface simulator,
            final GTUFollowingModel carFollowingModel, final Abs<TimeUnit> initialTime,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions, final Abs<SpeedUnit> initialSpeed) throws RemoteException,
            NamingException
    {
        super(id, null/* GTUType */, new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                        SpeedUnit.KM_PER_HOUR), carFollowingModel, initialLongitudinalPositions, initialSpeed, simulator);
        if (simulator instanceof OTSAnimatorInterface)
        {
            new CarAnimation(this, simulator);
        }
    }

}
