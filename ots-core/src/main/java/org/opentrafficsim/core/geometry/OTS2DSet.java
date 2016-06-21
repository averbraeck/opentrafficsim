package org.opentrafficsim.core.geometry;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.opentrafficsim.core.Throw;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Set of OTSShape3D objects, organized by 2D projection.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTS2DSet implements Set<OTSShape>
{
    /** Current number of Shapes in this set. */
    private int size = 0;

    /** The four leaves of this node in the quad tree. An empty sub tree may be represented by null. */
    private OTS2DSet[] leaves = new OTS2DSet[4];

    /** The OTSShapes stored at this node. */
    private Set<OTSShape> shapes = new HashSet<OTSShape>();

    /** The bounding box of this OTS2DSet. */
    private final Rectangle2D boundingBox;

    /** The bounding box of this OTS2DSet as an OTSShape. */
    private final OTSShape boundingShape;

    /** How fine will this quad tree divide. */
    private final double minimumCellSize;

    /**
     * Construct an empty OTS2DSet for a region.
     * @param boundingBox Rectangle2D; the region
     * @param minimumCellSize double; resolution of the underlying quad tree
     * @throws OTSGeometryException when the bounding box covers no surface
     */
    public OTS2DSet(final Rectangle2D boundingBox, final double minimumCellSize) throws OTSGeometryException
    {
        Throw.when(null == boundingBox, NullPointerException.class, "The boundingBox may not be null");
        Throw.when(boundingBox.getWidth() <= 0 || boundingBox.getHeight() <= 0, OTSGeometryException.class,
                "The boundingBox must have nonzero surface (got %s", boundingBox);
        Throw.when(minimumCellSize <= 0, OTSGeometryException.class, "The minimumCellSize must be > 0 (got %f)",
                minimumCellSize);
        this.boundingBox = boundingBox;
        this.boundingShape = rectangleShape(boundingBox);
        this.minimumCellSize = minimumCellSize;
    }

    /** {@inheritDoc} */
    @Override
    public final int size()
    {
        return this.size;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty()
    {
        return 0 == size();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(final Object o)
    {
        if (!(o instanceof OTSShape))
        {
            return false;
        }
        OTSShape shape = (OTSShape) o;
        Envelope shapeEnvelope = shape.getEnvelope();
        if (shapeEnvelope.getMinX() >= this.boundingBox.getMaxX() || shapeEnvelope.getMaxX() <= this.boundingBox.getMinX()
                || shapeEnvelope.getMinY() >= this.boundingBox.getMaxY()
                || shapeEnvelope.getMaxY() <= this.boundingBox.getMinY())
        {
            return false;
        }
        if (shape.contains(this.boundingBox)) // This contains operation is the geo operation contains!
        {
            if (this.shapes.contains(shape)) // This contains operation is the set operation contains!
            {
                return true;
            }
        }
        // Recursively check the sub nodes
        for (OTS2DSet subSet : this.leaves)
        {
            if (null == subSet)
            {
                continue;
            }
            if (subSet.contains(shape))
            {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<OTSShape> iterator()
    {
        return new QuadTreeIterator();
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T[] toArray(final T[] a)
    {
        return null;
    }

    /**
     * Construct a OTSShape from a Rectangle2D.
     * @param rectangle Rectangle3D; the rectangle
     * @return OTSShape; a new OTSShape
     */
    private OTSShape rectangleShape(final Rectangle2D rectangle)
    {
        double left = rectangle.getMinX();
        double bottom = rectangle.getMinY();
        double right = rectangle.getMaxX();
        double top = rectangle.getMaxY();
        try
        {
            return new OTSShape(new OTSPoint3D(left, bottom), new OTSPoint3D(right, bottom), new OTSPoint3D(right, top),
                    new OTSPoint3D(left, top));
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean add(final OTSShape e)
    {
        if (!this.boundingShape.intersects(e))
        {
            return false;
        }
        if (e.contains(this.boundingBox) || this.boundingBox.getWidth() < this.minimumCellSize
                && this.boundingBox.getHeight() < this.minimumCellSize)
        {
            boolean result = this.shapes.add(e);
            if (result)
            {
                this.size++;
            }
            return result;
        }
        // Add it to the applicable leaf nodes.
        boolean result = false;
        for (int index = 0; index < this.leaves.length; index++)
        {
            if (null == this.leaves[index])
            {
                Rectangle2D subBox =
                        new Rectangle2D.Double(index / 2 == 0 ? this.boundingBox.getMinX() : this.boundingBox.getCenterX(),
                                index % 2 == 0 ? this.boundingBox.getMinY() : this.boundingBox.getCenterY(),
                                this.boundingBox.getWidth() / 2, this.boundingBox.getHeight() / 2);
                // OTSShape subBoxShape = new OTSShape(subBox);
                if (rectangleShape(subBox).intersects(e))
                {
                    try
                    {
                        this.leaves[index] = new OTS2DSet(subBox, this.minimumCellSize);
                        if (this.leaves[index].add(e))
                        {
                            result = true;
                        }
                    }
                    catch (OTSGeometryException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
            else
            {
                if (this.leaves[index].add(e))
                {
                    result = true;
                }
            }
        }
        if (result)
        {
            this.size++;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean remove(final Object o)
    {
        if (!(o instanceof OTSShape))
        {
            return false;
        }
        OTSShape shape = (OTSShape) o;
        if (!this.boundingShape.intersects(shape))
        {
            return false;
        }
        for (OTSShape s : this.shapes)
        {
            if (shape.equals(s))
            {
                this.shapes.remove(shape);
                this.size--;
                return true;
            }
        }
        boolean result = false;
        for (int index = 0; index < this.leaves.length; index++)
        {
            OTS2DSet set = this.leaves[index];
            if (null != set)
            {
                if (set.remove(shape))
                {
                    result = true;
                    if (0 == set.size())
                    {
                        this.leaves[index] = null;
                    }
                }
            }
        }
        this.size--;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsAll(final Collection<?> c)
    {
        for (Object o : c)
        {
            if (!contains(o))
            {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean addAll(final Collection<? extends OTSShape> c)
    {
        boolean result = false;
        for (OTSShape s : c)
        {
            if (add(s))
            {
                result = true;
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean retainAll(final Collection<?> c)
    {
        boolean result = false;
        for (Iterator<OTSShape> it = iterator(); it.hasNext();)
        {
            OTSShape shape = it.next();
            if (!c.contains(shape))
            {
                it.remove();
                result = true;
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean removeAll(final Collection<?> c)
    {
        boolean result = false;
        for (Iterator<OTSShape> it = iterator(); it.hasNext();)
        {
            OTSShape shape = it.next();
            if (c.contains(shape))
            {
                it.remove();
                result = true;
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear()
    {
        for (int index = 0; index < this.leaves.length; index++)
        {
            this.leaves[index] = null;
        }
        this.shapes.clear();
    }

    /**
     * Iterator for quad tree. Shall iterate over the local set of shapes and the (up to four) non-null leave nodes.
     */
    class QuadTreeIterator implements Iterator<OTSShape>
    {
        /**
         * Position in the current node. Phase values 0..3 index the four leaves; phase value 4 selects the shapes set the
         * current node.
         */
        private int phase = 4;

        /** Iterator of the current (sub-)node. */
        private Iterator<OTSShape> subIterator = null;

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public final boolean hasNext()
        {
            while (this.phase >= 0)
            {
                if (null == this.subIterator)
                {
                    if (4 == this.phase)
                    {
                        this.subIterator = OTS2DSet.this.shapes.iterator();
                    }
                    else
                    {
                        // The second part of this if statement is a (minor) performance improvement
                        if (null != OTS2DSet.this.leaves[this.phase] || 0 == OTS2DSet.this.leaves[this.phase].size())
                        {
                            this.phase--; // move to the next phase
                            continue;
                        }
                        this.subIterator = OTS2DSet.this.leaves[this.phase].iterator();
                    }
                }
                // If execution gets here; we have a valid subIterator.
                if (this.subIterator.hasNext())
                {
                    return true;
                }
                // Our subIterator has run out of things to return. Get rid of it, then see if there is another set to iterate
                // over.
                this.subIterator = null;
                this.phase--; // move to the next phase.
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public final OTSShape next()
        {
            if (hasNext())
            {
                return this.subIterator.next();
            }
            throw new NoSuchElementException();
        }

        /** {@inheritDoc} */
        @Override
        public final void remove()
        {
            if (null == this.subIterator)
            {
                throw new IllegalStateException();
            }
            this.subIterator.remove();
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return toString(0);
    }

    /**
     * Helper function for toString.
     * @param recursionDepth int; maximum number of levels to print recursively
     * @param index int; index in leaves
     * @return String
     */
    private String printLeaf(final int recursionDepth, final int index)
    {
        OTS2DSet leaf = this.leaves[index];
        if (null == leaf)
        {
            return "null";
        }
        if (recursionDepth > 0)
        {
            return leaf.toString(recursionDepth - 1);
        }
        int leafSize = this.leaves[index].size();
        return leafSize + "shape" + (1 == leafSize ? "" : "s");
    }

    /**
     * Recursively print this OTS2DSet.
     * @param recursionDepth int; maximum depth to recurse
     * @return String
     */
    final String toString(final int recursionDepth)
    {
        return "OTS2DSet [size=" + this.size + ", bounds=[LB: " + this.boundingBox.getMinX() + "," + this.boundingBox.getMinY()
                + ", RT: " + this.boundingBox.getMaxX() + "," + this.boundingBox.getMaxY() + "], leaves=[LB: "
                + printLeaf(recursionDepth, 0) + ", RB: " + printLeaf(recursionDepth, 1) + ", LT: "
                + printLeaf(recursionDepth, 2) + ", RT: " + printLeaf(recursionDepth, 3) + "], local " + this.shapes.size()
                + " shape" + (1 == this.shapes.size() ? "" : "s") + ", minimumCellSize=" + this.minimumCellSize + "]";

    }

    /**
     * Return a number of concatenated copies of a string.
     * @param count int; number of copies to concatenate
     * @param string String; the string to repeat
     * @return String
     */
    private String repeat(final int count, final String string)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            result.append(string);
        }
        return result.toString();
    }

    /** Graphic to draw a vertical line. */
    private static final String VLINE = "|";

    /** Graphic to draw a horizontal line. */
    private static final String HLINE = "-";

    /** Graphic to draw a space. */
    private static final String SPACE = " ";

    /** Number of digits to print. */
    private static final int NUMBERSIZE = 6;

    /**
     * Similar to toStringGraphic, but with OTS2DSet argument which can be null.
     * @param set OTS2DSet; the OTS2DSet to render. Can be null.
     * @param recursionDepth int; levels to recurse
     * @return String
     */
    private String subStringGraphic(final OTS2DSet set, final int recursionDepth)
    {
        StringBuffer result = new StringBuffer();
        if (0 == recursionDepth)
        {
            if (null == set)
            {
                result.append(repeat(NUMBERSIZE, SPACE));
            }
            else
            {
                String numberBuf = String.format("%d", size());
                int spare = NUMBERSIZE - numberBuf.length();
                int filled = 0;
                while (filled < spare / 2)
                {
                    result.append(SPACE);
                    filled++;
                }
                result.append(numberBuf);
                while (filled < spare)
                {
                    result.append(SPACE);
                    filled++;
                }
                result.append("\n");
                return result.toString();
            }
        }
        else
        {
            String[] left = subStringGraphic(null == set ? null : set.leaves[1], recursionDepth - 1).split("\\n");
            String[] right = subStringGraphic(null == set ? null : set.leaves[3], recursionDepth - 1).split("\\n");
            String horizontalLine = null;
            for (int i = 0; i < left.length; i++)
            {
                if (0 == i)
                {
                    StringBuilder line = new StringBuilder();
                    int width = left[0].length() + 1 + right[0].length();
                    if (null == set)
                    {
                        line.append(repeat(width, SPACE));
                    }
                    else
                    {
                        String numberBuf = String.format("%d", set.shapes.size());
                        int spare = width - numberBuf.length();
                        line.append(repeat(spare / 2, HLINE));
                        line.append(numberBuf);
                        line.append(repeat(spare - spare / 2, HLINE));
                    }
                    horizontalLine = line.toString();
                }
                result.append(left[i]);
                result.append(null == set ? SPACE : VLINE);
                result.append(right[i]);
                result.append("\n");
            }
            result.append(horizontalLine);
            result.append("\n");
            left = subStringGraphic(null == set ? null : set.leaves[0], recursionDepth - 1).split("\\n");
            right = subStringGraphic(null == set ? null : set.leaves[2], recursionDepth - 1).split("\\n");
            for (int i = 0; i < left.length; i++)
            {
                result.append(left[i]);
                result.append(null == set ? SPACE : VLINE);
                result.append(right[i]);
                result.append("\n");
            }
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Return a String array depicting a OTS2DSet.
     * @param recursionDepth int; levels to recurse
     * @return String
     */
    public final String toStringGraphic(final int recursionDepth)
    {
        return subStringGraphic(this, recursionDepth);
    }

}
