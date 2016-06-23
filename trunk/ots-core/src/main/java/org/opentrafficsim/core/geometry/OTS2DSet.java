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
 * Set of OTSShape objects and provides methods for fast selection of those objects that intersect an OTSShape. <br>
 * An OTS2DSet internally stores the OTSShapes in a quad tree. At time of construction the minimum cell size is defined. Node
 * expansion is never performed on nodes that are smaller than this limit. <br>
 * Each node (even the non-leaf nodes) store a set of OTSShape. Non-leaf nodes locally store those shapes that completely cover
 * the rectangular area of the node. Such shapes are <b>not</b> also stored in leaf nodes below that node. OTSShapes that
 * partially cover a non-leaf node are stored in each of the leaf nodes below that node that those OTSShapes (partially) cover.
 * Leaf nodes that cannot be expanded (because they are too small) also store all OTSShapes that partially cover the area of the
 * node. <br>
 * If removal of an OTSShape objects results in a leaf becoming empty, that leaf is removed from its parent (which may then
 * itself become empty and removed in turn).
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

    /** Can this node contain sub nodes? */
    private final boolean mayHaveSubNodes;

    /**
     * Construct an empty OTS2DSet for a rectangular region. Objects that do not intersect this region will never be stored in
     * this OTS2DSet. (Trying to add such an OTSShape is <b>not</b> an error; the <code>add</code> method will return false,
     * indicating that the set has not been modified.)
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
        this.mayHaveSubNodes =
                this.boundingBox.getWidth() > this.minimumCellSize || this.boundingBox.getHeight() > this.minimumCellSize;
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
            return false; // shape does not intersect the bounding box of this node
        }
        // This contains operation is the spatial operation with that name!
        if ((!this.mayHaveSubNodes) || shape.contains(this.boundingBox))
        {
            // This contains operation is the set operation with that name!
            if (this.shapes.contains(shape))
            {
                return true;
            }
        }
        // Recursively check the sub nodes
        for (OTS2DSet subSet : this.leaves)
        {
            if (null != subSet && subSet.contains(shape))
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

    /** Returned value of toArray() when size of this OTS2DSet is 0. */
    private static final Object[] EMPTY_ARRAY_OF_OBJECT = new Object[0];

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray()
    {
        if (0 == this.size())
        {
            return EMPTY_ARRAY_OF_OBJECT;
        }
        Object[] result = new Object[this.size()];
        int nextIndex = 0;
        for (OTSShape shape : this)
        {
            result[nextIndex++] = shape;
        }
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> T[] toArray(final T[] a)
    {
        T[] result = a;
        if (result.length < this.size())
        {
            result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), this.size());
        }
        else if (result.length > this.size())
        {
            result[this.size()] = null;
        }
        int nextIndex = 0;
        for (OTSShape shape : this)
        {
            result[nextIndex++] = (T) shape;
        }
        return result;
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
        if ((!this.mayHaveSubNodes) || e.contains(this.boundingBox))
        {
            // e belongs in the set of shapes of this node.
            boolean result = this.shapes.add(e);
            if (result)
            {
                this.size++;
            }
            return result;
        }
        // This node may have sub nodes and e does not entirely contain this node. Add e to all applicable sub nodes.
        boolean result = false;
        for (int index = 0; index < this.leaves.length; index++)
        {
            if (null == this.leaves[index])
            {
                Rectangle2D subBox =
                        new Rectangle2D.Double(index / 2 == 0 ? this.boundingBox.getMinX() : this.boundingBox.getCenterX(),
                                index % 2 == 0 ? this.boundingBox.getMinY() : this.boundingBox.getCenterY(),
                                this.boundingBox.getWidth() / 2, this.boundingBox.getHeight() / 2);
                if (rectangleShape(subBox).intersects(e))
                {
                    // Expand this node by adding a sub node.
                    try
                    {
                        this.leaves[index] = new OTS2DSet(subBox, this.minimumCellSize);
                        if (this.leaves[index].add(e))
                        {
                            result = true;
                        }
                        else
                        {
                            throw new Error("Cannot happen: new node refused to add shape that intersects it");
                        }
                    }
                    catch (OTSGeometryException exception)
                    {
                        exception.printStackTrace(); // Should not be possible
                    }
                }
            }
            else
            {
                // Leaf node already exists. Let the leaf determine if e should be stored (somewhere) in it.
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
        return result;
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
                        this.leaves[index] = null; // Cut off empty leaf node
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
     * Return the set of all shapes in this OTS2DSet that intersect the given rectangle.
     * @param rectangle Rectangle2D; the rectangle
     * @return Set&lt;OTSShape&gt;; the shapes that intersect the rectangle
     */
    public final Set<OTSShape> intersectingShapes(final Rectangle2D rectangle)
    {
        Set<OTSShape> result = new HashSet<OTSShape>();
        if (!this.boundingBox.intersects(rectangle))
        {
            return result;
        }
        for (OTS2DSet leaf : this.leaves)
        {
            if (null != leaf && leaf.boundingBox.intersects(rectangle))
            {
                result.addAll(leaf.intersectingShapes(rectangle));
            }
        }
        for (OTSShape shape : this.shapes)
        {
            OTSShape rectangleShape = rectangleShape(rectangle);
            if (rectangleShape.intersects(shape))
            {
                result.add(shape);
            }
        }
        return result;
    }

    /**
     * Return all OTSShapes in this OTS2DSet that intersect a given OTSShape.
     * @param shape OTSShape; the given OTSShape
     * @return Set&lt;OTSShape&gt;; all OTSShapes in this OTS2DSet that intersect <code>shape</code>
     */
    public final Set<OTSShape> intersectingShapes(final OTSShape shape)
    {
        Envelope envelope = shape.getEnvelope();
        Set<OTSShape> result =
                intersectingShapes(new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(),
                        envelope.getHeight()));
        for (Iterator<OTSShape> it = result.iterator(); it.hasNext();)
        {
            if (!it.next().intersects(shape))
            {
                it.remove();
            }
        }
        return result;
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
        return leafSize + " shape" + (1 == leafSize ? "" : "s");
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
     * Return concatenation of a number of copies of a string.
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
     * Similar to toStringGraphic, but with OTS2DSet argument which can be null. <br>
     * This code is <b>not</b> optimized for performance; the repeated use of String.split is probably expensive.
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
     * Return a String depicting an OTS2DSet.
     * @param recursionDepth int; levels to recurse
     * @return String
     */
    public final String toStringGraphic(final int recursionDepth)
    {
        return subStringGraphic(this, recursionDepth);
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

        /** Set of OTSShapes that were already returned by this iterator. This is probably dreadfully expensive. */
        private Set<OTSShape> seen = new HashSet<OTSShape>();

        /** Iterator of the current (sub-)node. */
        private Iterator<OTSShape> subIterator = null;

        /** The next OTSShape. Must be stored because hasNext has to inspect it to ensure it has not been seen before. */
        private OTSShape nextResult;

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
                        if (null == OTS2DSet.this.leaves[this.phase] || 0 == OTS2DSet.this.leaves[this.phase].size())
                        {
                            this.phase--; // move to the next phase
                            continue;
                        }
                        this.subIterator = OTS2DSet.this.leaves[this.phase].iterator();
                    }
                }
                // If execution gets here; we have a valid subIterator.
                while (this.subIterator.hasNext())
                {
                    this.nextResult = this.subIterator.next();
                    if (this.seen.contains(this.nextResult))
                    {
                        continue;
                    }
                    this.seen.add(this.nextResult);
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
            if (null == this.nextResult)
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
            }
            OTSShape result = this.nextResult;
            this.nextResult = null;
            return result;
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

}
