/*
 * @(#)MultiThumbSliderUI.java
 *
 * $Date: 2015-01-04 20:37:28 -0500 (Sun, 04 Jan 2015) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.multislider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import com.bric.multislider.MultiThumbSlider.Collision;

/**
 * This is the abstract UI for <code>MultiThumbSliders</code>
 * @param <T> the type
 */
public abstract class MultiThumbSliderUi<T> extends ComponentUI implements MouseListener, MouseMotionListener
{

    /**
     * The Swing client property associated with a Thumb.
     * @see Thumb
     */
    public static final String THUMB_SHAPE_PROPERTY = MultiThumbSliderUi.class.getName() + ".thumbShape";

    PropertyChangeListener thumbShapeListener = new PropertyChangeListener()
    {

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            MultiThumbSliderUi.this.slider.repaint();
        }

    };

    /**
     * A thumb shape.
     */
    public static enum Thumb
    {
        Circle()
        {
            @Override
            public Shape getShape(float width, float height, boolean leftEdge, boolean rightEdge)
            {
                Ellipse2D e = new Ellipse2D.Float(-width / 2f, -height / 2f, width, height);
                return e;
            }
        },
        Triangle()
        {
            @Override
            public Shape getShape(float width, float height, boolean leftEdge, boolean rightEdge)
            {
                float k = width / 2;
                GeneralPath p = new GeneralPath();
                float r = 5;

                if ((leftEdge) && (!rightEdge))
                {
                    k = k * 2;
                    p.moveTo(0, height / 2);
                    p.lineTo(-k, height / 2 - k);
                    p.lineTo(-k, -height / 2 + r);
                    p.curveTo(-k, -height / 2, -k, -height / 2, -k + r, -height / 2);
                    p.lineTo(0, -height / 2);
                    p.closePath();
                }
                else if ((rightEdge) && (!leftEdge))
                {
                    k = k * 2;
                    p.moveTo(0, -height / 2);
                    p.lineTo(k - r, -height / 2);
                    p.curveTo(k, -height / 2, k, -height / 2, k, -height / 2 + r);
                    p.lineTo(k, height / 2 - k);
                    p.lineTo(0, height / 2);
                    p.closePath();
                }
                else
                {
                    p.moveTo(0, height / 2);
                    p.lineTo(-k, height / 2 - k);
                    p.lineTo(-k, -height / 2 + r);
                    p.curveTo(-k, -height / 2, -k, -height / 2, -k + r, -height / 2);
                    p.lineTo(k - r, -height / 2);
                    p.curveTo(k, -height / 2, k, -height / 2, k, -height / 2 + r);
                    p.lineTo(k, height / 2 - k);
                    p.closePath();
                }
                return p;
            }
        },
        Rectangle()
        {
            @Override
            public Shape getShape(float width, float height, boolean leftEdge, boolean rightEdge)
            {
                if ((leftEdge) && (!rightEdge))
                {
                    return new Rectangle2D.Float(-width, -height / 2, width, height);
                }
                else if ((rightEdge) && (!leftEdge))
                {
                    return new Rectangle2D.Float(0, -height / 2, width, height);
                }
                else
                {
                    return new Rectangle2D.Float(-width / 2, -height / 2, width, height);
                }
            }
        },
        Hourglass()
        {
            @Override
            public Shape getShape(float width, float height, boolean leftEdge, boolean rightEdge)
            {
                GeneralPath p = new GeneralPath();
                if ((leftEdge) && (!rightEdge))
                {
                    float k = width;
                    p.moveTo(-width, -height / 2);
                    p.lineTo(0, -height / 2);
                    p.lineTo(0, height / 2);
                    p.lineTo(-width, height / 2);
                    p.lineTo(0, height / 2 - k);
                    p.lineTo(0, -height / 2 + k);
                    p.closePath();
                }
                else if ((rightEdge) && (!leftEdge))
                {
                    float k = width;
                    p.moveTo(width, -height / 2);
                    p.lineTo(0, -height / 2);
                    p.lineTo(0, height / 2);
                    p.lineTo(width, height / 2);
                    p.lineTo(0, height / 2 - k);
                    p.lineTo(0, -height / 2 + k);
                    p.closePath();
                }
                else
                {
                    float k = width / 2;
                    p.moveTo(-width / 2, -height / 2);
                    p.lineTo(width / 2, -height / 2);
                    p.lineTo(0, -height / 2 + k);
                    p.lineTo(0, height / 2 - k);
                    p.lineTo(width / 2, height / 2);
                    p.lineTo(-width / 2, height / 2);
                    p.lineTo(0, height / 2 - k);
                    p.lineTo(0, -height / 2 + k);
                    p.closePath();
                }
                return p;
            }
        };

        /**
         * Create a thumb that is centered at (0,0) for a horizontally oriented slider.
         * @param sliderUI the slider UI this thumb relates to.
         * @param x the x-coordinate where this thumb is centered.
         * @param y the y-coordinate where this thumb is centered.
         * @param width the width of the the thumb (assuming this is a horizontal slider)
         * @param height the height of the the thumb (assuming this is a horizontal slider)
         * @param leftEdge true if this is the left-most thumb
         * @param rightEdge true if this is the right-most thumb.
         * @return the shape of this thumb.
         */
        public Shape getShape(MultiThumbSliderUi<?> sliderUI, float x, float y, int width, int height, boolean leftEdge,
                boolean rightEdge)
        {

            // TODO: reinstate leftEdge and rightEdge once bug related to nudging
            // adjacent thumbs is resolved.

            GeneralPath path = new GeneralPath(getShape(width, height, false, false));
            if (sliderUI.slider.getOrientation() == SwingConstants.VERTICAL)
            {
                path.transform(AffineTransform.getRotateInstance(-Math.PI / 2));
            }
            path.transform(AffineTransform.getTranslateInstance(x, y));
            return path;
        }

        /**
         * Create a thumb that is centered at (0,0) for a horizontally oriented slider.
         * @param width the width of the the thumb (assuming this is a horizontal slider)
         * @param height the height of the the thumb (assuming this is a horizontal slider)
         * @param leftEdge true if this is the left-most thumb
         * @param rightEdge true if this is the right-most thumb.
         * @return the shape of this thumb.
         */
        public abstract Shape getShape(float width, float height, boolean leftEdge, boolean rightEdge);
    }

    protected MultiThumbSlider<T> slider;

    /**
     * The maximum width returned by <code>getMaximumSize()</code>. (or if the slider is vertical, this is the maximum height.)
     */
    int MAX_LENGTH = 300;

    /**
     * The minimum width returned by <code>getMinimumSize()</code>. (or if the slider is vertical, this is the minimum height.)
     */
    int MIN_LENGTH = 50;

    /**
     * The maximum width returned by <code>getPreferredSize()</code>. (or if the slider is vertical, this is the preferred
     * height.)
     */
    int PREF_LENGTH = 140;

    /**
     * The height of a horizontal slider -- or width of a vertical slider.
     */
    int DEPTH = 15;

    /**
     * The pixel position of the thumbs. This may be x or y coordinates, depending on whether this slider is horizontal or
     * vertical
     */
    int[] thumbPositions = new int[0];

    /**
     * A float from zero to one, indicating whether that thumb should be highlighted or not.
     */
    protected float[] thumbIndications = new float[0];

    /** This is used by the animating thread. The field indication is updated until it equals this value. */
    private float indicationGoal = 0;

    /**
     * The overall indication of the thumbs. At one they should be opaque, at zero they should be transparent.
     */
    float indication = 0;

    /** The rectangle the track should be painted in. */
    protected Rectangle trackRect = new Rectangle(0, 0, 0, 0);

    public MultiThumbSliderUi(MultiThumbSlider<T> slider)
    {
        this.slider = slider;
    }

    @Override
    public Dimension getMaximumSize(JComponent s)
    {
        MultiThumbSlider<T> mySlider = (MultiThumbSlider<T>) s;
        int k = Math.max(this.DEPTH, getPreferredComponentDepth());
        if (mySlider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            return new Dimension(this.MAX_LENGTH, k);
        }
        return new Dimension(k, this.MAX_LENGTH);
    }

    @Override
    public Dimension getMinimumSize(JComponent s)
    {
        MultiThumbSlider<T> mySlider = (MultiThumbSlider<T>) s;
        int k = Math.max(this.DEPTH, getPreferredComponentDepth());
        if (mySlider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            return new Dimension(this.MIN_LENGTH, k);
        }
        return new Dimension(k, this.MIN_LENGTH);
    }

    @Override
    public Dimension getPreferredSize(JComponent s)
    {
        MultiThumbSlider<T> mySlider = (MultiThumbSlider<T>) s;
        int k = Math.max(this.DEPTH, getPreferredComponentDepth());
        if (mySlider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            return new Dimension(this.PREF_LENGTH, k);
        }
        return new Dimension(k, this.PREF_LENGTH);
    }

    /**
     * Return the typical height of a horizontally oriented slider, or the width of the vertically oriented slider.
     * @return the typical height of a horizontally oriented slider, or the width of the vertically oriented slider.
     */
    protected abstract int getPreferredComponentDepth();

    /**
     * This records the positions/values of each thumb. This is used when the mouse is pressed, so as the mouse is dragged
     * values can get replaced and rearranged freely. (Including removing and adding thumbs)
     */
    class State
    {
        T[] values;

        float[] positions;

        int selectedThumb;

        public State()
        {
            this.values = MultiThumbSliderUi.this.slider.getValues();
            this.positions = MultiThumbSliderUi.this.slider.getThumbPositions();
            this.selectedThumb = MultiThumbSliderUi.this.slider.getSelectedThumb(false);
        }

        public State(State s)
        {
            this.selectedThumb = s.selectedThumb;
            this.positions = new float[s.positions.length];
            this.values = createSimilarArray(s.values, s.values.length);
            System.arraycopy(s.positions, 0, this.positions, 0, this.positions.length);
            System.arraycopy(s.values, 0, this.values, 0, this.values.length);
        }

        /** Strip values outside of [0,1] */
        private void polish()
        {
            while (this.positions[0] < 0)
            {
                float[] f2 = new float[this.positions.length - 1];
                System.arraycopy(this.positions, 1, f2, 0, this.positions.length - 1);
                T[] c2 = createSimilarArray(this.values, this.values.length - 1);
                System.arraycopy(this.values, 1, c2, 0, this.positions.length - 1);
                this.positions = f2;
                this.values = c2;
                this.selectedThumb++;
            }
            while (this.positions[this.positions.length - 1] > 1)
            {
                float[] f2 = new float[this.positions.length - 1];
                System.arraycopy(this.positions, 0, f2, 0, this.positions.length - 1);
                T[] c2 = createSimilarArray(this.values, this.values.length - 1);
                System.arraycopy(this.values, 0, c2, 0, this.positions.length - 1);
                this.positions = f2;
                this.values = c2;
                this.selectedThumb--;
            }
            if (this.selectedThumb >= this.positions.length)
                this.selectedThumb = -1;
        }

        /** Make the slider reflect this object */
        public void install()
        {
            polish();

            MultiThumbSliderUi.this.slider.setValues(this.positions, this.values);
            MultiThumbSliderUi.this.slider.setSelectedThumb(this.selectedThumb);
        }

        /**
         * This is a kludgy casting trick to make our arrays mesh with generics.
         * @param src source array
         * @param length the length
         * @return array of type T
         */
        private T[] createSimilarArray(T[] src, int length)
        {
            Class<?> componentType = src.getClass().getComponentType();
            return (T[]) Array.newInstance(componentType, length);
        }

        public void removeThumb(int index)
        {
            float[] f = new float[this.positions.length - 1];
            T[] c = createSimilarArray(this.values, this.values.length - 1);
            System.arraycopy(this.positions, 0, f, 0, index);
            System.arraycopy(this.values, 0, c, 0, index);
            System.arraycopy(this.positions, index + 1, f, index, f.length - index);
            System.arraycopy(this.values, index + 1, c, index, f.length - index);
            this.positions = f;
            this.values = c;
            this.selectedThumb = -1;
        }

        public boolean setPosition(int thumbIndex, float newPosition)
        {
            return setPosition(thumbIndex, newPosition, true);
        }

        private boolean isCrossover(int thumbIndexA, int thumbIndexB, float newThumbBPosition)
        {
            if (thumbIndexA == thumbIndexB)
                return false;
            int oldState = new Float(this.positions[thumbIndexA]).compareTo(this.positions[thumbIndexB]);
            int newState = new Float(this.positions[thumbIndexA]).compareTo(newThumbBPosition);
            if (newState * oldState < 0)
                return true;
            return isOverlap(thumbIndexA, thumbIndexB, newThumbBPosition);
        }

        private boolean isOverlap(int thumbIndexA, int thumbIndexB, float newThumbBPosition)
        {
            if (thumbIndexA == thumbIndexB)
                return false;
            if (!MultiThumbSliderUi.this.slider.isThumbOverlap())
            {
                Point2D aCenter = getThumbCenter(this.positions[thumbIndexA]);
                Point2D bCenter = getThumbCenter(newThumbBPosition);
                Rectangle2D aBounds = ShapeBounds.getBounds(getThumbShape(thumbIndexA, aCenter));
                Rectangle2D bBounds = ShapeBounds.getBounds(getThumbShape(thumbIndexB, bCenter));
                return aBounds.intersects(bBounds) || aBounds.equals(bBounds);
            }
            return false;
        }

        private boolean setPosition(int thumbIndex, float newPosition, boolean revise)
        {
            Collision c = MultiThumbSliderUi.this.slider.getCollisionPolicy();
            if (Collision.JUMP_OVER_OTHER.equals(c) && (!MultiThumbSliderUi.this.slider.isThumbOverlap()))
            {
                newPosition = Math.max(0, Math.min(1, newPosition));
                for (int a = 0; a < this.positions.length; a++)
                {
                    if (isOverlap(a, thumbIndex, newPosition))
                    {
                        if (revise)
                        {
                            float alternative;

                            int maxWidth = Math.max(getThumbSize(a).width, getThumbSize(thumbIndex).width);
                            float trackSize = MultiThumbSliderUi.this.slider.getOrientation() == SwingConstants.HORIZONTAL
                                    ? MultiThumbSliderUi.this.trackRect.width : MultiThumbSliderUi.this.trackRect.height;
                            newPosition = Math.max(0, Math.min(1, newPosition));
                            // offset is measured in pixels
                            for (int offset = 0; offset < 4 * maxWidth; offset++)
                            {
                                alternative = Math.max(0, Math.min(1, newPosition - ((float) offset) / trackSize));
                                if (!isOverlap(a, thumbIndex, alternative))
                                {
                                    return setPosition(thumbIndex, alternative, false);
                                }
                                alternative = Math.max(0, Math.min(1, newPosition + ((float) offset) / trackSize));
                                if (!isOverlap(a, thumbIndex, alternative))
                                {
                                    return setPosition(thumbIndex, alternative, false);
                                }
                            }
                            return false;
                        }
                        return false;
                    }
                }
            }
            else if (Collision.STOP_AGAINST.equals(c))
            {
                for (int a = 0; a < this.positions.length; a++)
                {
                    if (isCrossover(a, thumbIndex, newPosition))
                    {
                        // this move would cross thumbIndex over an existing thumb. This violates the collision policy:
                        if (revise)
                        {
                            float alternative;

                            int maxWidth = Math.max(getThumbSize(a).width, getThumbSize(thumbIndex).width);
                            float trackSize = MultiThumbSliderUi.this.slider.getOrientation() == SwingConstants.HORIZONTAL
                                    ? MultiThumbSliderUi.this.trackRect.width : MultiThumbSliderUi.this.trackRect.height;
                            // offset is measured in pixels
                            for (int offset = 0; offset < 2 * maxWidth; offset++)
                            {
                                if (this.positions[a] > this.positions[thumbIndex])
                                {
                                    alternative = this.positions[a] - ((float) offset) / trackSize;
                                }
                                else
                                {
                                    alternative = this.positions[a] + ((float) offset) / trackSize;
                                }
                                if (!isCrossover(a, thumbIndex, alternative))
                                {
                                    return setPosition(thumbIndex, alternative, false);
                                }
                            }
                            return false;
                        }

                        return false;
                    }
                }
            }
            else if (Collision.NUDGE_OTHER.equals(c))
            {
                if (revise)
                {
                    final Set<Integer> processedThumbs = new LinkedHashSet<Integer>();
                    processedThumbs.add(-1);

                    class NudgeRequest
                    {
                        /** The index of the thumb this request wants to move. */
                        final int thumbIndex;

                        /** The original value of this thumb. */
                        final float startingValue;

                        /** The amount we're asking to change this value by. */
                        final float requestedDelta;

                        NudgeRequest(int thumbIndex, float startingValue, float requestedDelta)
                        {
                            this.thumbIndex = thumbIndex;
                            this.startingValue = startingValue;
                            this.requestedDelta = requestedDelta;
                        }

                        void process()
                        {
                            float span;
                            if (MultiThumbSliderUi.this.slider.isThumbOverlap())
                            {
                                span = 0;
                            }
                            else
                            {
                                span = (float) ShapeBounds.getBounds(getThumbShape(this.thumbIndex)).getWidth();
                                if (MultiThumbSliderUi.this.slider.getOrientation() == SwingConstants.HORIZONTAL)
                                {
                                    span = span / ((float) MultiThumbSliderUi.this.trackRect.width);
                                }
                                else
                                {
                                    span = span / ((float) MultiThumbSliderUi.this.trackRect.height);
                                }
                            }
                            int[] neighbors = getNeighbors(this.thumbIndex);
                            float newPosition = this.startingValue + this.requestedDelta;
                            processedThumbs.add(this.thumbIndex);

                            if (neighbors[0] == -1 && newPosition < 0)
                            {
                                setPosition(this.thumbIndex, 0, false);
                            }
                            else if (neighbors[1] == -1 && newPosition > 1)
                            {
                                setPosition(this.thumbIndex, 1, false);
                            }
                            else if (processedThumbs.add(neighbors[0]) && (newPosition < State.this.positions[neighbors[0]]
                                    || Math.abs(State.this.positions[neighbors[0]] - newPosition) < span - .0001))
                            {
                                NudgeRequest dependsOn = new NudgeRequest(neighbors[0], State.this.positions[neighbors[0]],
                                        (newPosition - span) - State.this.positions[neighbors[0]]);
                                dependsOn.process();
                                setPosition(this.thumbIndex, State.this.positions[dependsOn.thumbIndex] + span, false);
                            }
                            else if (processedThumbs.add(neighbors[1]) && (newPosition > State.this.positions[neighbors[1]]
                                    || Math.abs(State.this.positions[neighbors[1]] - newPosition) < span - .0001))
                            {
                                NudgeRequest dependsOn = new NudgeRequest(neighbors[1], State.this.positions[neighbors[1]],
                                        (newPosition + span) - State.this.positions[neighbors[1]]);
                                dependsOn.process();
                                setPosition(this.thumbIndex, State.this.positions[dependsOn.thumbIndex] - span, false);
                            }
                            else
                            {
                                setPosition(this.thumbIndex, this.startingValue + this.requestedDelta, false);
                            }
                        }
                    }

                    float originalValue = this.positions[thumbIndex];
                    NudgeRequest rootRequest =
                            new NudgeRequest(thumbIndex, this.positions[thumbIndex], newPosition - this.positions[thumbIndex]);
                    rootRequest.process();
                    return this.positions[thumbIndex] != originalValue;
                }

            }
            this.positions[thumbIndex] = newPosition;
            return true;
        }

        /**
         * Return the left (lesser) neighbor and the right (greater) neighbor. Either index may be -1 if it is not available.
         * @param thumbIndex the index of the thumb to examine.
         * @return the left (lesser) neighbor and the right (greater) neighbor.
         */
        int[] getNeighbors(int thumbIndex)
        {
            float leftNeighborDelta = 10;
            float rightNeighborDelta = 10;
            int leftNeighbor = -1;
            int rightNeighbor = -1;
            for (int a = 0; a < this.positions.length; a++)
            {
                if (a != thumbIndex)
                {
                    if (this.positions[thumbIndex] < this.positions[a])
                    {
                        float delta = this.positions[a] - this.positions[thumbIndex];
                        if (delta < rightNeighborDelta)
                        {
                            rightNeighborDelta = delta;
                            rightNeighbor = a;
                        }
                    }
                    else if (this.positions[thumbIndex] > this.positions[a])
                    {
                        float delta = this.positions[thumbIndex] - this.positions[a];
                        if (delta < leftNeighborDelta)
                        {
                            leftNeighborDelta = delta;
                            leftNeighbor = a;
                        }
                    }
                }
            }
            return new int[] {leftNeighbor, rightNeighbor};
        }
    }

    Thread animatingThread = null;

    Runnable animatingRunnable = new Runnable()
    {
        public void run()
        {
            boolean finished = false;
            while (!finished)
            {
                synchronized (MultiThumbSliderUi.this)
                {
                    finished = true;
                    for (int a = 0; a < MultiThumbSliderUi.this.thumbIndications.length; a++)
                    {
                        if (a != MultiThumbSliderUi.this.slider.getSelectedThumb())
                        {
                            if (a == MultiThumbSliderUi.this.currentIndicatedThumb)
                            {
                                if (MultiThumbSliderUi.this.thumbIndications[a] < 1)
                                {
                                    MultiThumbSliderUi.this.thumbIndications[a] =
                                            Math.min(1, MultiThumbSliderUi.this.thumbIndications[a] + .025f);
                                    finished = false;
                                }
                            }
                            else
                            {
                                if (MultiThumbSliderUi.this.thumbIndications[a] > 0)
                                {
                                    MultiThumbSliderUi.this.thumbIndications[a] =
                                            Math.max(0, MultiThumbSliderUi.this.thumbIndications[a] - .025f);
                                    finished = false;
                                }
                            }
                        }
                        else
                        {
                            // the selected thumb is painted as selected,
                            // so there's no indication to animate.
                            // just set the indication to whatever it should
                            // be and move on. No repainting.
                            if (a == MultiThumbSliderUi.this.currentIndicatedThumb)
                            {
                                MultiThumbSliderUi.this.thumbIndications[a] = 1;
                            }
                            else
                            {
                                MultiThumbSliderUi.this.thumbIndications[a] = 0;
                            }
                        }
                    }
                    if (MultiThumbSliderUi.this.indicationGoal > MultiThumbSliderUi.this.indication + .01f)
                    {
                        if (MultiThumbSliderUi.this.indication < .99f)
                        {
                            MultiThumbSliderUi.this.indication = Math.min(1, MultiThumbSliderUi.this.indication + .1f);
                            finished = false;
                        }
                    }
                    else if (MultiThumbSliderUi.this.indicationGoal < MultiThumbSliderUi.this.indication - .01f)
                    {
                        if (MultiThumbSliderUi.this.indication > .01f)
                        {
                            MultiThumbSliderUi.this.indication = Math.max(0, MultiThumbSliderUi.this.indication - .1f);
                            finished = false;
                        }
                    }
                }
                if (!finished)
                    MultiThumbSliderUi.this.slider.repaint();

                // rest a little bit
                long t = System.currentTimeMillis();
                while (System.currentTimeMillis() - t < 20)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (Exception e)
                    {
                        Thread.yield();
                    }
                }
            }
        }
    };

    private int currentIndicatedThumb = -1;

    protected boolean mouseInside = false;

    protected boolean mouseIsDown = false;

    private State pressedState;

    private int dx, dy;

    public void mousePressed(MouseEvent e)
    {
        this.dx = 0;
        this.dy = 0;

        if (this.slider.isEnabled() == false)
            return;

        if (e.getClickCount() >= 2)
        {
            if (this.slider.doDoubleClick(e.getX(), e.getY()))
            {
                e.consume();
                return;
            }
        }
        else if (e.isPopupTrigger())
        {
            int x = e.getX();
            int y = e.getY();
            if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
            {
                if (x < this.trackRect.x || x > this.trackRect.x + this.trackRect.width)
                    return;
                y = this.trackRect.y + this.trackRect.height;
            }
            else
            {
                if (y < this.trackRect.y || y > this.trackRect.y + this.trackRect.height)
                    return;
                x = this.trackRect.x + this.trackRect.width;
            }
            if (this.slider.doPopup(x, y))
            {
                e.consume();
                return;
            }
        }
        this.mouseIsDown = true;
        mouseMoved(e);

        if (e.getSource() != this.slider)
        {
            throw new RuntimeException("only install this UI on the GradientSlider it was constructed with");
        }
        this.slider.requestFocus();

        int index = getIndex(e);
        if (index != -1)
        {
            if (this.slider.getOrientation() == SwingConstants.HORIZONTAL)
            {
                this.dx = -e.getX() + this.thumbPositions[index];
            }
            else
            {
                this.dy = -e.getY() + this.thumbPositions[index];
            }
        }

        if (index != -1)
        {
            this.slider.setSelectedThumb(index);
            e.consume();
        }
        else
        {
            if (this.slider.isAutoAdding())
            {
                float k;

                int v;
                if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
                {
                    v = e.getX();
                }
                else
                {
                    v = e.getY();
                }

                if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
                {
                    k = ((float) (v - this.trackRect.x)) / ((float) this.trackRect.width);
                    if (this.slider.isInverted())
                        k = 1 - k;
                }
                else
                {
                    k = ((float) (v - this.trackRect.y)) / ((float) this.trackRect.height);
                    if (this.slider.isInverted() == false)
                        k = 1 - k;
                }
                if (k > 0 && k < 1)
                {
                    int added = this.slider.addThumb(k);
                    this.slider.setSelectedThumb(added);
                }
                e.consume();
            }
            else
            {
                if (this.slider.getSelectedThumb() != -1)
                {
                    this.slider.setSelectedThumb(-1);
                    e.consume();
                }
            }
        }
        this.pressedState = new State();
    }

    private int getIndex(MouseEvent e)
    {
        int v;
        Rectangle2D shapeSum =
                new Rectangle2D.Double(this.trackRect.x, this.trackRect.y, this.trackRect.width, this.trackRect.height);
        for (int a = 0; a < this.slider.getThumbCount(); a++)
        {
            shapeSum.add(ShapeBounds.getBounds(getThumbShape(a)));
        }
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            v = e.getX();
            if (v < shapeSum.getMinX() || v > shapeSum.getMaxX())
            {
                return -1; // didn't click in the track;
            }
        }
        else
        {
            v = e.getY();
            if (v < shapeSum.getMinY() || v > shapeSum.getMaxY())
            {
                return -1;
            }
        }
        int min = Math.abs(v - this.thumbPositions[0]);
        int minIndex = 0;
        for (int a = 1; a < this.thumbPositions.length; a++)
        {
            int distance = Math.abs(v - this.thumbPositions[a]);
            if (distance < min)
            {
                min = distance;
                minIndex = a;
            }
            else if (distance == min)
            {
                // two thumbs may perfectly overlap
                if (v < this.thumbPositions[a])
                {
                    // you clicked to the left of the fulcrum, so we should side with the smaller index
                    if (this.slider.isInverted())
                    {
                        // ... unless it's inverted:
                        minIndex = a;
                    }
                }
                else
                {
                    if (!this.slider.isInverted())
                        minIndex = a;
                }
            }
        }
        if (min < getThumbSize(minIndex).width / 2)
        {
            return minIndex;
        }
        return -1;
    }

    public void mouseEntered(MouseEvent e)
    {
        mouseMoved(e);
    }

    public void mouseExited(MouseEvent e)
    {
        setCurrentIndicatedThumb(-1);
        setMouseInside(false);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseMoved(MouseEvent e)
    {
        if (this.slider.isEnabled() == false)
            return;

        int i = getIndex(e);
        setCurrentIndicatedThumb(i);
        boolean b = (e.getX() >= 0 && e.getX() < this.slider.getWidth() && e.getY() >= 0 && e.getY() < this.slider.getHeight());
        if (this.mouseIsDown)
            b = true;
        setMouseInside(b);
    }

    protected Dimension getThumbSize(int thumbIndex)
    {
        return new Dimension(16, 16);
    }

    /**
     * Create the shape used to render a specific thumb.
     * @param thumbIndex the index of the thumb to render.
     * @return the shape used to render a specific thumb.
     * @see #getThumbCenter(int)
     * @see #getThumb(int)
     */
    public Shape getThumbShape(int thumbIndex)
    {
        return getThumbShape(thumbIndex, null);
    }

    /**
     * Create the shape used to render a specific thumb.
     * @param thumbIndex the index of the thumb to render.
     * @param center an optional center to focus the thumb around. If this is null then the current (real) center is used, but
     *            this can be supplied manually to consider possible shapes and visual size constraints based on the current
     *            collision policy.
     * @return the shape used to render a specific thumb.
     * @see #getThumbCenter(int)
     * @see #getThumb(int)
     */
    public Shape getThumbShape(int thumbIndex, Point2D center)
    {
        Thumb thumb = getThumb(thumbIndex);
        if (center == null)
            center = getThumbCenter(thumbIndex);
        Dimension d = getThumbSize(thumbIndex);
        return thumb.getShape(this, (float) center.getX(), (float) center.getY(), d.width, d.height, thumbIndex == 0,
                thumbIndex == this.slider.getThumbCount() - 1);
    }

    /**
     * Calculate the thumb center
     * @param thumbIndex the index of the thumb to consult.
     * @return the center of a given thumb
     */
    public Point2D getThumbCenter(int thumbIndex)
    {
        float[] values = this.slider.getThumbPositions();
        float n = values[thumbIndex];

        return getThumbCenter(n);
    }

    /**
     * Calculate the thumb center based on a fractional position
     * @param position a value from [0,1]
     * @return the center of a potential thumbnail for this position.
     */
    public Point2D getThumbCenter(float position)
    {
        /*
         * I'm on the fence about whether to document this as allowing null or not. Does this occur in the wild? If so: is this
         * more an internal error than something we need to document/allow for?
         */
        if (position < 0 || position > 1)
            return null;

        if (this.slider.getOrientation() == MultiThumbSlider.VERTICAL)
        {
            float y;
            float height = (float) this.trackRect.height;
            float x = (float) this.trackRect.getCenterX();
            if (this.slider.isInverted())
            {
                y = (float) (position * height + this.trackRect.y);
            }
            else
            {
                y = (float) ((1 - position) * height + this.trackRect.y);
            }
            return new Point2D.Float(x, y);
        }
        else
        {
            float x;
            float width = (float) this.trackRect.width;
            float y = (float) this.trackRect.getCenterY();
            if (this.slider.isInverted())
            {
                x = (float) ((1 - position) * width + this.trackRect.x);
            }
            else
            {
                x = (float) (position * width + this.trackRect.x);
            }
            return new Point2D.Float(x, y);
        }
    }

    /**
     * Return the Thumb option used to render a specific thumb. The default implementation here consults the client property
     * MultiThumbSliderUI.THUMB_SHAPE_PROPERTY, and returns Circle by default.
     * @param thumbIndex the index of the thumb to render.
     * @return the Thumb option used to render a specific thumb.
     */
    public Thumb getThumb(int thumbIndex)
    {
        Thumb thumb = getProperty(this.slider, THUMB_SHAPE_PROPERTY, Thumb.Circle);
        return thumb;
    }

    private void setCurrentIndicatedThumb(int i)
    {
        if (getProperty(this.slider, "MultiThumbSlider.indicateThumb", "true").equals("false"))
        {
            // never activate a specific thumb
            i = -1;
        }
        this.currentIndicatedThumb = i;
        boolean finished = true;
        for (int a = 0; a < this.thumbIndications.length; a++)
        {
            if (a == this.currentIndicatedThumb)
            {
                if (this.thumbIndications[a] != 1)
                {
                    finished = false;
                }
            }
            else
            {
                if (this.thumbIndications[a] != 0)
                {
                    finished = false;
                }
            }
        }
        if (!finished)
        {
            synchronized (MultiThumbSliderUi.this)
            {
                if (this.animatingThread == null || this.animatingThread.isAlive() == false)
                {
                    this.animatingThread = new Thread(this.animatingRunnable);
                    this.animatingThread.start();
                }
            }
        }
    }

    private void setMouseInside(boolean b)
    {
        this.mouseInside = b;
        updateIndication();
    }

    public void mouseDragged(MouseEvent e)
    {
        if (this.slider.isEnabled() == false)
            return;

        e.translatePoint(this.dx, this.dy);

        mouseMoved(e);
        if (this.pressedState != null && this.pressedState.selectedThumb != -1)
        {
            this.slider.setValueIsAdjusting(true);

            State newState = new State(this.pressedState);
            float v;
            boolean outside;
            if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
            {
                v = ((float) (e.getX() - this.trackRect.x)) / ((float) this.trackRect.width);
                if (this.slider.isInverted())
                    v = 1 - v;
                outside = (e.getY() < this.trackRect.y - 10) || (e.getY() > this.trackRect.y + this.trackRect.height + 10);

                // don't whack the thumb off the slider if you happen to be *near* the edge:
                if (e.getX() > this.trackRect.x - 10 && e.getX() < this.trackRect.x + this.trackRect.width + 10)
                {
                    if (v < 0)
                        v = 0;
                    if (v > 1)
                        v = 1;
                }
            }
            else
            {
                v = ((float) (e.getY() - this.trackRect.y)) / ((float) this.trackRect.height);
                if (this.slider.isInverted() == false)
                    v = 1 - v;
                outside = (e.getX() < this.trackRect.x - 10) || (e.getX() > this.trackRect.x + this.trackRect.width + 10);

                if (e.getY() > this.trackRect.y - 10 && e.getY() < this.trackRect.y + this.trackRect.height + 10)
                {
                    if (v < 0)
                        v = 0;
                    if (v > 1)
                        v = 1;
                }
            }
            if (newState.positions.length <= this.slider.getMinimumThumbnailCount())
            {
                outside = false; // I don't care if you are outside: no removing!
            }
            newState.setPosition(newState.selectedThumb, v);

            // because we delegate mouseReleased() to this method:
            if (outside && this.slider.isThumbRemovalAllowed())
            {
                newState.removeThumb(newState.selectedThumb);
            }
            if (validatePositions(newState))
            {
                newState.install();
            }
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (this.slider.isEnabled() == false)
            return;

        this.mouseIsDown = false;
        if (this.pressedState != null && this.slider.getThumbCount() <= this.pressedState.positions.length)
        {
            mouseDragged(e); // go ahead and commit this final location
        }
        if (this.slider.isValueAdjusting())
        {
            this.slider.setValueIsAdjusting(false);
        }
        this.slider.repaint();

        if (e.isPopupTrigger() && this.slider.doPopup(e.getX(), e.getY()))
        {
            // on windows popuptriggers happen on mouseRelease
            e.consume();
            return;
        }
    }

    /**
     * This retrieves a property. If the component has this property manually set (by calling
     * <code>component.putClientProperty()</code>), then that value will be returned. Otherwise this method refers to
     * <code>UIManager.get()</code>. If that value is missing, this returns <code>defaultValue</code>
     * @param jc component
     * @param propertyName the property name
     * @param defaultValue if no other value is found, this is returned
     * @return the property value
     */
    public static <K> K getProperty(JComponent jc, String propertyName, K defaultValue)
    {
        Object jcValue = jc.getClientProperty(propertyName);
        if (jcValue != null)
            return (K) jcValue;
        Object uiValue = UIManager.get(propertyName);
        if (uiValue != null)
            return (K) uiValue;
        return defaultValue;
    }

    /**
     * Makes sure the thumbs are in the right order.
     * @param state state
     * @return true if the thumbs are valid. False if there are two thumbs with the same value (this is not allowed)
     */
    protected boolean validatePositions(State state)
    {
        float[] p = state.positions;
        Object[] c = state.values;

        /**
         * Don't let the user position a thumb outside of [0,1] if there are only 2 colors: colors outside [0,1] are deleted,
         * and we can't delete colors so we get less than 2.
         */
        if (p.length <= this.slider.getMinimumThumbnailCount() || (!this.slider.isThumbRemovalAllowed()))
        {
            /**
             * Since the user can only manipulate 1 thumb at a time, only 1 thumb should be outside the domain of [0,1]. So we
             * *don't* have to reorganize c when we change p
             */
            for (int a = 0; a < p.length; a++)
            {
                if (p[a] < 0)
                {
                    p[a] = 0;
                }
                else if (p[a] > 1)
                {
                    p[a] = 1;
                }
            }
        }

        // validate the new positions:
        boolean checkAgain = true;
        while (checkAgain)
        {
            checkAgain = false;
            for (int a = 0; a < p.length - 1; a++)
            {
                if (p[a] > p[a + 1])
                {
                    checkAgain = true;

                    float swap1 = p[a];
                    p[a] = p[a + 1];
                    p[a + 1] = swap1;
                    Object swap2 = c[a];
                    c[a] = c[a + 1];
                    c[a + 1] = swap2;

                    if (a == state.selectedThumb)
                    {
                        state.selectedThumb = a + 1;
                    }
                    else if (a + 1 == state.selectedThumb)
                    {
                        state.selectedThumb = a;
                    }
                }
            }
        }

        return true;
    }

    FocusListener focusListener = new FocusListener()
    {
        public void focusLost(FocusEvent e)
        {
            Component c = (Component) e.getSource();
            if (getProperty(MultiThumbSliderUi.this.slider, "MultiThumbSlider.indicateComponent", "false").toString()
                    .equals("true"))
            {
                MultiThumbSliderUi.this.slider.setSelectedThumb(-1);
            }
            updateIndication();
            c.repaint();
        }

        public void focusGained(FocusEvent e)
        {
            Component c = (Component) e.getSource();
            int i = MultiThumbSliderUi.this.slider.getSelectedThumb(false);
            if (i == -1)
            {
                int direction = 1;
                if (MultiThumbSliderUi.this.slider.getOrientation() == MultiThumbSlider.VERTICAL)
                    direction *= -1;
                if (MultiThumbSliderUi.this.slider.isInverted())
                    direction *= -1;
                MultiThumbSliderUi.this.slider
                        .setSelectedThumb((direction == 1) ? 0 : MultiThumbSliderUi.this.slider.getThumbCount() - 1);
            }
            updateIndication();
            c.repaint();
        }
    };

    /**
     * This will try to add a thumb between index1 and index2.
     * <P>
     * This method will not add a thumb if there is already a very small distance between these two endpoints
     * @param index1 low value
     * @param index2 high value
     * @return true if a new thumb was added
     */
    protected boolean addThumb(int index1, int index2)
    {
        float pos1 = 0;
        float pos2 = 1;
        int min;
        int max;
        if (index1 < index2)
        {
            min = index1;
            max = index2;
        }
        else
        {
            min = index2;
            max = index1;
        }
        float[] positions = this.slider.getThumbPositions();
        if (min >= 0)
            pos1 = positions[min];
        if (max < positions.length)
            pos2 = positions[max];

        if (pos2 - pos1 < .05)
            return false;

        float newPosition = (pos1 + pos2) / 2f;
        this.slider.setSelectedThumb(this.slider.addThumb(newPosition));

        return true;
    }

    KeyListener keyListener = new KeyListener()
    {
        public void keyPressed(KeyEvent e)
        {
            if (MultiThumbSliderUi.this.slider.isEnabled() == false)
                return;

            if (e.getSource() != MultiThumbSliderUi.this.slider)
                throw new RuntimeException("only install this UI on the GradientSlider it was constructed with");
            int i = MultiThumbSliderUi.this.slider.getSelectedThumb();
            int code = e.getKeyCode();
            int orientation = MultiThumbSliderUi.this.slider.getOrientation();
            if (i != -1 && (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_LEFT) && orientation == MultiThumbSlider.HORIZONTAL
                    && e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
            {
                // insert a new thumb
                int i2;
                if ((code == KeyEvent.VK_RIGHT && MultiThumbSliderUi.this.slider.isInverted() == false)
                        || (code == KeyEvent.VK_LEFT && MultiThumbSliderUi.this.slider.isInverted() == true))
                {
                    i2 = i + 1;
                }
                else
                {
                    i2 = i - 1;
                }
                addThumb(i, i2);
                e.consume();
                return;
            }
            else if (i != -1 && (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN) && orientation == MultiThumbSlider.VERTICAL
                    && e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
            {
                // insert a new thumb
                int i2;
                if ((code == KeyEvent.VK_UP && MultiThumbSliderUi.this.slider.isInverted() == false)
                        || (code == KeyEvent.VK_DOWN && MultiThumbSliderUi.this.slider.isInverted() == true))
                {
                    i2 = i + 1;
                }
                else
                {
                    i2 = i - 1;
                }
                addThumb(i, i2);
                e.consume();
                return;
            }
            else if (code == KeyEvent.VK_DOWN && orientation == MultiThumbSlider.HORIZONTAL && i != -1)
            {
                // popup up!
                int x = MultiThumbSliderUi.this.slider.isInverted()
                        ? (int) (MultiThumbSliderUi.this.trackRect.x + MultiThumbSliderUi.this.trackRect.width
                                * (1 - MultiThumbSliderUi.this.slider.getThumbPositions()[i]))
                        : (int) (MultiThumbSliderUi.this.trackRect.x + MultiThumbSliderUi.this.trackRect.width
                                * MultiThumbSliderUi.this.slider.getThumbPositions()[i]);
                int y = MultiThumbSliderUi.this.trackRect.y + MultiThumbSliderUi.this.trackRect.height;
                if (MultiThumbSliderUi.this.slider.doPopup(x, y))
                {
                    e.consume();
                    return;
                }
            }
            else if (code == KeyEvent.VK_RIGHT && orientation == MultiThumbSlider.VERTICAL && i != -1)
            {
                // popup up!
                int y = MultiThumbSliderUi.this.slider.isInverted()
                        ? (int) (MultiThumbSliderUi.this.trackRect.y + MultiThumbSliderUi.this.trackRect.height
                                * MultiThumbSliderUi.this.slider.getThumbPositions()[i])
                        : (int) (MultiThumbSliderUi.this.trackRect.y + MultiThumbSliderUi.this.trackRect.height
                                * (1 - MultiThumbSliderUi.this.slider.getThumbPositions()[i]));
                int x = MultiThumbSliderUi.this.trackRect.x + MultiThumbSliderUi.this.trackRect.width;
                if (MultiThumbSliderUi.this.slider.doPopup(x, y))
                {
                    e.consume();
                    return;
                }
            }
            if (i != -1)
            {
                // move the selected thumb
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_DOWN)
                {
                    nudge(i, 1);
                    e.consume();
                }
                else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_UP)
                {
                    nudge(i, -1);
                    e.consume();
                }
                else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE)
                {
                    if (MultiThumbSliderUi.this.slider.getThumbCount() > MultiThumbSliderUi.this.slider
                            .getMinimumThumbnailCount() && MultiThumbSliderUi.this.slider.isThumbRemovalAllowed())
                    {
                        MultiThumbSliderUi.this.slider.removeThumb(i);
                        e.consume();
                    }
                }
                else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER)
                {
                    MultiThumbSliderUi.this.slider.doDoubleClick(-1, -1);
                }
            }
        }

        public void keyReleased(KeyEvent e)
        {
        }

        public void keyTyped(KeyEvent e)
        {
        }
    };

    PropertyChangeListener propertyListener = new PropertyChangeListener()
    {

        public void propertyChange(PropertyChangeEvent e)
        {
            String name = e.getPropertyName();
            if (name.equals(MultiThumbSlider.VALUES_PROPERTY) || name.equals(MultiThumbSlider.ORIENTATION_PROPERTY)
                    || name.equals(MultiThumbSlider.INVERTED_PROPERTY))
            {
                calculateGeometry();
                MultiThumbSliderUi.this.slider.repaint();
            }
            else if (name.equals(MultiThumbSlider.SELECTED_THUMB_PROPERTY)
                    || name.equals(MultiThumbSlider.PAINT_TICKS_PROPERTY))
            {
                MultiThumbSliderUi.this.slider.repaint();
            }
            else if (name.equals("MultiThumbSlider.indicateComponent"))
            {
                setMouseInside(MultiThumbSliderUi.this.mouseInside);
                MultiThumbSliderUi.this.slider.repaint();
            }
        }

    };

    ComponentListener compListener = new ComponentListener()
    {

        public void componentHidden(ComponentEvent e)
        {
        }

        public void componentMoved(ComponentEvent e)
        {
        }

        public void componentResized(ComponentEvent e)
        {
            calculateGeometry();
            Component c = (Component) e.getSource();
            c.repaint();
        }

        public void componentShown(ComponentEvent e)
        {
        }
    };

    protected void updateIndication()
    {
        synchronized (MultiThumbSliderUi.this)
        {
            if (this.slider.isEnabled() && (this.slider.hasFocus() || this.mouseInside))
            {
                this.indicationGoal = 1;
            }
            else
            {
                this.indicationGoal = 0;
            }

            if (getProperty(this.slider, "MultiThumbSlider.indicateComponent", "false").equals("false"))
            {
                // always turn on the "indication", so controls are always visible
                this.indicationGoal = 1;
                if (this.slider.isVisible() == false)
                { // when the component isn't yet initialized
                    this.indication = 1; // initialize it to fully indicated
                }
            }

            if (this.indication != this.indicationGoal)
            {
                if (this.animatingThread == null || this.animatingThread.isAlive() == false)
                {
                    this.animatingThread = new Thread(this.animatingRunnable);
                    this.animatingThread.start();
                }
            }
        }
    }

    protected synchronized void calculateGeometry()
    {
        this.trackRect = calculateTrackRect();

        float[] pos = this.slider.getThumbPositions();

        if (this.thumbPositions.length != pos.length)
        {
            this.thumbPositions = new int[pos.length];
            this.thumbIndications = new float[pos.length];
        }
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            for (int a = 0; a < this.thumbPositions.length; a++)
            {
                if (this.slider.isInverted() == false)
                {
                    this.thumbPositions[a] = this.trackRect.x + (int) (this.trackRect.width * pos[a]);
                }
                else
                {
                    this.thumbPositions[a] = this.trackRect.x + (int) (this.trackRect.width * (1 - pos[a]));
                }
                this.thumbIndications[a] = 0;
            }
        }
        else
        {
            for (int a = 0; a < this.thumbPositions.length; a++)
            {
                if (this.slider.isInverted())
                {
                    this.thumbPositions[a] = this.trackRect.y + (int) (this.trackRect.height * pos[a]);
                }
                else
                {
                    this.thumbPositions[a] = this.trackRect.y + (int) (this.trackRect.height * (1 - pos[a]));
                }
                this.thumbIndications[a] = 0;
            }
        }
    }

    protected Rectangle calculateTrackRect()
    {
        Insets i = new Insets(5, 5, 5, 5);
        int w, h;
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            w = this.slider.getWidth() - i.left - i.right;
            h = Math.min(this.DEPTH, this.slider.getHeight() - i.top - i.bottom);
        }
        else
        {
            h = this.slider.getHeight() - i.top - i.bottom;
            w = Math.min(this.DEPTH, this.slider.getWidth() - i.left - i.right);
        }
        return new Rectangle(this.slider.getWidth() / 2 - w / 2, this.slider.getHeight() / 2 - h / 2, w, h);
    }

    private void nudge(int thumbIndex, int direction)
    {
        float pixelFraction;
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            pixelFraction = 1f / (this.trackRect.width);
        }
        else
        {
            pixelFraction = 1f / (this.trackRect.height);
        }
        if (direction < 0)
            pixelFraction *= -1;
        if (this.slider.isInverted())
            pixelFraction *= -1;
        if (this.slider.getOrientation() == MultiThumbSlider.VERTICAL)
            pixelFraction *= -1;

        // repeat a couple of times: it's possible we'll nudge two values
        // so they're exactly equal, which will make validate() fail.
        // in that case: move the value ANOTHER nudge to the left/right
        // to really make a change. But make sure we still respect the [0,1] limits.
        State state = new State();
        int a = 0;
        while (a < 10 && state.positions[thumbIndex] >= 0 && state.positions[thumbIndex] <= 1)
        {
            state.setPosition(thumbIndex, state.positions[thumbIndex] + pixelFraction);
            if (validatePositions(state))
            {
                state.install();
                return;
            }
            a++;
        }
    }

    @Override
    public void installUI(JComponent slider)
    {
        slider.addMouseListener(this);
        slider.addMouseMotionListener(this);
        slider.addFocusListener(this.focusListener);
        slider.addKeyListener(this.keyListener);
        slider.addComponentListener(this.compListener);
        slider.addPropertyChangeListener(this.propertyListener);
        slider.addPropertyChangeListener(THUMB_SHAPE_PROPERTY, this.thumbShapeListener);
        calculateGeometry();
    }

    @Override
    public void paint(Graphics g, JComponent slider2)
    {
        if (slider2 != this.slider)
            throw new RuntimeException("only use this UI on the GradientSlider it was constructed with");

        Graphics2D g2 = (Graphics2D) g;
        int w = this.slider.getWidth();
        int h = this.slider.getHeight();

        if (this.slider.isOpaque())
        {
            g.setColor(this.slider.getBackground());
            g.fillRect(0, 0, w, h);
        }

        if (slider2.hasFocus())
        {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintFocus(g2);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        paintTrack(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintThumbs(g2);
    }

    protected abstract void paintTrack(Graphics2D g);

    protected abstract void paintFocus(Graphics2D g);

    protected abstract void paintThumbs(Graphics2D g);

    @Override
    public void uninstallUI(JComponent slider)
    {
        slider.removeMouseListener(this);
        slider.removeMouseMotionListener(this);
        slider.removeFocusListener(this.focusListener);
        slider.removeKeyListener(this.keyListener);
        slider.removeComponentListener(this.compListener);
        slider.removePropertyChangeListener(this.propertyListener);
        slider.removePropertyChangeListener(THUMB_SHAPE_PROPERTY, this.thumbShapeListener);
        super.uninstallUI(slider);
    }

}
