package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.draw.ClickableLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Animates a GtuGeneratorPosition.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuGeneratorPositionAnimation extends OtsRenderable<GtuGeneratorPositionData>
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
     * @param number number of the chevron.
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
     * @param source source.
     * @param contextProvider simulator.
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public GtuGeneratorPositionAnimation(final GtuGeneratorPositionData source, final Contextualized contextProvider)
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
        setRendering(graphics);
        graphics.fill(PATH);
        resetRendering(graphics);
    }

    /**
     * Paints a queue counter with a GtuGeneratorPosition.
     * <p>
     * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Queue extends TextAnimation<GtuGeneratorPositionData, Queue>
    {
        /** */
        private static final long serialVersionUID = 20230204L;

        /**
         * Constructor.
         * @param source source.
         * @param contextualized context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException when remote context cannot be found
         */
        public Queue(final GtuGeneratorPositionData source, final Contextualized contextualized)
                throws RemoteException, NamingException
        {
            super(source, () -> Integer.toString(source.getQueueCount()), 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, 3.0f,
                    12.0f, 50f, contextualized, null, TextAnimation.RENDERWHEN10);
        }
    }

    /**
     * GtuGeneratorPositionData provides the information required to draw a GTU generator position.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface GtuGeneratorPositionData extends ClickableLocatable
    {
        /**
         * Returns the queue count.
         * @return queue count.
         */
        int getQueueCount();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.OBJECT.getZ();
        }
    }

}
