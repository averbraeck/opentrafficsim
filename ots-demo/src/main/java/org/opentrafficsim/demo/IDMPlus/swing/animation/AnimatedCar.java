package org.opentrafficsim.demo.IDMPlus.swing.animation;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
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
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AnimatedCar extends Car
{
    /** */
    private static final long serialVersionUID = 20141003L;

    /**
     * Create a new Car with animation in case we schedule on an Animator.
     * @param id integer; the id of the new Car
     * @param simulator OTSDEVSSimulator
     * @param carFollowingModel CarFollowingModel; the car following model used by the new Car
     * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the first evaluation time of the new Car
     * @param initialPosition DoubleScalar.Abs&lt;LengthUnit&gt;; the initial position of the new Car
     * @param initialSpeed DoubleScalar.Rel&lt;SpeedUnit&gt;; the initial speed of the new Car
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public AnimatedCar(final int id, final OTSDEVSSimulatorInterface simulator,
            final CarFollowingModel<AnimatedCar> carFollowingModel, final Abs<TimeUnit> initialTime,
            final Abs<LengthUnit> initialPosition, final Rel<SpeedUnit> initialSpeed) throws RemoteException,
            NamingException
    {
        super(id, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
        if (simulator instanceof OTSAnimatorInterface)
        {
            new CarAnimation(this, simulator);
        }
    }

}
