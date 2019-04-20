package org.opentrafficsim.road.network.factory.rti.communication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SubjectiveCarAnimation extends Renderable2D implements Serializable
{

    /** */
    private static final long serialVersionUID = 20141229L;

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param source SubjectiveCar; the Car to draw
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public SubjectiveCarAnimation(final SubjectiveCar source, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        final SubjectiveCar car = (SubjectiveCar) getSource();
        final double length = car.getLength().getSI();
        final double width = car.getWidth().getSI();
        graphics.setColor(Color.ORANGE);
        BasicStroke saveStroke = (BasicStroke) graphics.getStroke();
        graphics.setStroke(new BasicStroke(0));
        Rectangle2D rectangle = new Rectangle2D.Double(-length / 2, -width / 2, length, width);
        graphics.draw(rectangle);
        graphics.fill(rectangle);
        // Draw a 1m diameter white disk about 1m before the front to indicate which side faces forward
        graphics.setColor(Color.WHITE);
        Ellipse2D.Double frontIndicator = new Ellipse2D.Double(length / 2 - 1.5d, -0.5d, 1d, 1d);
        graphics.draw(frontIndicator);
        graphics.fill(frontIndicator);
        graphics.setStroke(saveStroke);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DefaultCarAnimation [id=" + ((SubjectiveCar) this.getSource()).getId() + "]";
    }

}
