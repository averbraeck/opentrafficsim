package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d2.Angle;

/**
 * Animates a GtuGeneratorPosition.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuGeneratorPositionAnimation extends Renderable2D<GtuGeneratorPosition>
{

    /** */
    private static final long serialVersionUID = 20230204L;

    /** Chevron path to draw. */
    private static final Path2D.Float PATH;

    static
    {
        PATH = new Path2D.Float();
        addChevron(PATH, 0);
        addChevron(PATH, 1);
        addChevron(PATH, 2);
    }

    /**
     * Add chevron to drawing path.
     * @param path Path2D.Float; path.
     * @param number int; number of the chevron.
     */
    private static void addChevron(final Path2D.Float path, final int number)
    {
        float x = number * 1.5f;
        path.moveTo(x, -1.0);
        path.lineTo(x + 1.0, 0.0);
        path.lineTo(x, 1.0);
        path.lineTo(x + 0.75, 1.0);
        path.lineTo(x + 1.75, 0.0);
        path.lineTo(x + 0.75, -1.0);
        path.lineTo(x, -1.0);
    }

    /**
     * Constructor.
     * @param source GtuGeneratorPosition; source.
     * @param contextProvider OtsSimulatorInterface; simulator.
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public GtuGeneratorPositionAnimation(final GtuGeneratorPosition source, final OtsSimulatorInterface contextProvider)
            throws RemoteException, NamingException
    {
        super(source, contextProvider);
        new Queue(source, contextProvider);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLUE);
        graphics.fill(PATH);
    }

    /**
     * Paints a queue counter with a GtuGeneratorPosition.
     * <p>
     * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Queue extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20230204L;

        /**
         * Constructor.
         * @param source Locatable; source.
         * @param simulator OtsSimulatorInterface; simulator.
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException when remote context cannot be found
         */
        public Queue(final Locatable source, final OtsSimulatorInterface simulator) throws RemoteException, NamingException
        {
            super(source, "", 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, 3.0f, 12.0f, 50f, simulator, null,
                    TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public OrientedPoint2d getLocation()
        {
            // draw always on top, and not upside down.
            OrientedPoint2d p = super.getLocation();
            double a = Angle.normalizePi(p.getDirZ());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new OrientedPoint2d(p.x, p.y, a);
        }

        /** {@inheritDoc} */
        @Override
        public void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            setText(Integer.toString(((GtuGeneratorPosition) getSource()).getQueueCount()));
            super.paint(graphics, observer);
        }
    }

}
