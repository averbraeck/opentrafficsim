package org.opentrafficsim.road.gtu.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultCarAnimation extends Renderable2D
{
    /** The GTUColorer that determines the fill color for the car. */
    private GTUColorer gtuColorer;

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedIndividualGTU source, final OTSSimulatorInterface simulator)
        throws NamingException, RemoteException
    {
        this(source, simulator, null);
    }

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @param gtuColorer GTUColorer; the GTUColorer that determines what fill color to use
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedIndividualGTU source, final OTSSimulatorInterface simulator,
        final GTUColorer gtuColorer) throws NamingException, RemoteException
    {
        super(source, simulator);
        if (null == gtuColorer)
        {
            this.gtuColorer = new IDGTUColorer();
        }
        else
        {
            this.gtuColorer = gtuColorer;
        }
    }

    /**
     * Replace the GTUColorer.
     * @param newGTUColorer GTUColorer; the GTUColorer to use from now on
     */
    public final void setGTUColorer(final GTUColorer newGTUColorer)
    {
        this.gtuColorer = newGTUColorer;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        final LaneBasedIndividualGTU car = (LaneBasedIndividualGTU) getSource();

        if (car.isDestroyed())
        {
            try
            {
                destroy();
            }
            catch (Exception e)
            {
                System.err.println("GTU: " + car.toString());
                e.printStackTrace();
            }
        }

        final double length = car.getLength().getSI();
        final double l2 = length / 2;
        final double width = car.getWidth().getSI();
        final double w2 = width / 2;
        final double w4 = width / 4;
        graphics.setColor(this.gtuColorer.getColor(car));
        BasicStroke saveStroke = (BasicStroke) graphics.getStroke();
        graphics.setStroke(new BasicStroke(0));
        Rectangle2D rectangle = new Rectangle2D.Double(-l2, -w2, length, width);
        graphics.draw(rectangle);
        graphics.fill(rectangle);
        // Draw a white disk at the front to indicate which side faces forward
        graphics.setColor(Color.WHITE);
        Ellipse2D.Double frontIndicator = new Ellipse2D.Double(l2 - w2 - w4, -w4, w2, w2);
        graphics.draw(frontIndicator);
        graphics.fill(frontIndicator);

        graphics.setColor(Color.YELLOW);
        if (car.getTurnIndicatorStatus() != null && car.getTurnIndicatorStatus().isLeftOrBoth())
        {
            Rectangle2D.Double leftIndicator = new Rectangle2D.Double(l2 - w4, -w2, w4, w4);
            graphics.fill(leftIndicator);
        }
        
        if (car.getTurnIndicatorStatus() != null && car.getTurnIndicatorStatus().isRightOrBoth())
        {
            Rectangle2D.Double rightIndicator = new Rectangle2D.Double(l2 - w4, w2 - w4, w4, w4);
            graphics.fill(rightIndicator);            
        }
        
        graphics.setColor(Color.RED);
        if (car.getAcceleration().si < 0.0)
        {
            Rectangle2D.Double leftBrake = new Rectangle2D.Double(-l2, w2 - w4, w4, w4);
            Rectangle2D.Double rightBrake = new Rectangle2D.Double(-l2, -w2, w4, w4);
            graphics.setColor(Color.RED);
            graphics.fill(leftBrake);
            graphics.fill(rightBrake);
        }
        graphics.setStroke(saveStroke);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.getSource().toString();
    }

}
