package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.lane.AbstractTrafficLight;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * Special GTU that cannot move, but it can be seen by other GTUs.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 15 jul. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrafficLight extends AbstractTrafficLight
{
    /** the color to display. */
    private Color color = Color.BLACK;

    /**
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public TrafficLight(final String name, final Lane lane, final DoubleScalar.Rel<LengthUnit> position,
        OTSDEVSSimulatorInterface simulator) throws GTUException, RemoteException, NetworkException, NamingException
    {
        super(name, lane, position, simulator);

        new TrafficLightAnimation(this, getSimulator());
        // animation
        if (simulator instanceof OTSAnimatorInterface)
        {
            // TODO
        }
    }

    /**
     * @return color
     */
    public final Color getColor()
    {
        return this.color;
    }

    public void changeColor(final int newColor)
    {
        switch (newColor)
        {
            case 0:
                this.color = Color.RED;
                setBlocked(true);
                break;

            case 1:
                this.color = Color.GREEN;
                setBlocked(false);
                break;

            case 2:
                this.color = Color.YELLOW;
                setBlocked(false);
                break;

            case 3:
                this.color = Color.WHITE; // wit knipper
                setBlocked(false);
                break;

            case 4:
                this.color = Color.BLACK;
                setBlocked(false);
                break;

            case 5:
                this.color = Color.WHITE; // geel knipper
                setBlocked(false);
                break;

            default:
                break;
        }
    }

}
