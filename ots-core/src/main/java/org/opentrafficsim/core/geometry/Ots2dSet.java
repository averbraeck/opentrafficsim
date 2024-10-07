package org.opentrafficsim.core.geometry;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

/**
 * Set of Polygon2d objects and provides methods for fast selection of those objects that intersect a Polygon2d. <br>
 * An Ots2dSet internally stores the Polygon2ds in a quad tree. At time of construction the minimum cell size is defined. Node
 * expansion is never performed on nodes that are smaller than this limit. <br>
 * Each node (even the non-leaf nodes) store a set of Polygon2d. Non-leaf nodes locally store those shapes that completely cover
 * the rectangular area of the node. Such shapes are <b>not</b> also stored in leaf nodes below that node. Polygon2ds that
 * partially cover a non-leaf node are stored in each of the leaf nodes below that node that those Polygon2ds (partially) cover.
 * Leaf nodes that cannot be expanded (because they are too small) also store all Polygon2ds that partially cover the area of
 * the node. <br>
 * If removal of a Polygon2d objects results in a leaf becoming empty, that leaf is removed from its parent (which may then
 * itself become empty and removed in turn).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Ots2dSet implements Set<Polygon2d>, Serializable
{
    /** */
    private static final long serialVersionUID = 20170400L;

    /** Set of all shapes used for iterators, etc. */
    private final Set<Polygon2d> allShapes = new LinkedHashSet<Polygon2d>();

    /** How fine will this quad tree divide. This one is copied to each sub-node which is somewhat inefficient. */
    private final double minimumCellSize;

    /** Spatial storage for the Polygon2ds. */
    private QuadTreeNode quadTree;

    /**
     * Construct an empty Ots2dSet for a rectangular region. Objects that do not intersect this region will never be stored in
     * this Ots2dSet. (Trying to add such a Polygon2d is <b>not</b> an error; the <code>add</code> method will return false,
     * indicating that the set has not been modified.)
     * @param boundingBox the region
     * @param minimumCellSize resolution of the underlying quad tree
     * @throws OtsGeometryException when the bounding box covers no surface
     */
    public Ots2dSet(final Bounds2d boundingBox, final double minimumCellSize) throws OtsGeometryException
    {
        Throw.when(null == boundingBox, NullPointerException.class, "The boundingBox may not be null");
        Throw.when(boundingBox.getDeltaX() <= 0 || boundingBox.getDeltaY() <= 0, OtsGeometryException.class,
                "The boundingBox must have nonzero surface (got %s", boundingBox);
        Throw.when(minimumCellSize <= 0, OtsGeometryException.class, "The minimumCellSize must be > 0 (got %f)",
                minimumCellSize);
        this.quadTree = new QuadTreeNode(boundingBox);
        this.minimumCellSize = minimumCellSize;
    }

    /** {@inheritDoc} */
    @Override
    public final int size()
    {
        return this.allShapes.size();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty()
    {
        return this.allShapes.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(final Object o)
    {
        return this.allShapes.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<Polygon2d> iterator()
    {
        return new QuadTreeIterator();
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] toArray()
    {
        return this.allShapes.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T[] toArray(final T[] a)
    {
        return this.allShapes.toArray(a);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean add(final Polygon2d e)
    {
        if (!this.quadTree.intersects(e))
        {
            return false;
        }
        if (this.allShapes.contains(e))
        {
            return false;
        }
        if (!this.quadTree.add(e))
        {
            CategoryLogger.always().error("add: ERROR object could not be added to the quad tree");
        }
        return this.allShapes.add(e);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean remove(final Object o)
    {
        if (!this.allShapes.remove(o))
        {
            return false;
        }
        if (!this.quadTree.remove((Polygon2d) o))
        {
            CategoryLogger.always().error("remove: ERROR object could not be removed from the quad tree");
        }
        return true;
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
    public final boolean addAll(final Collection<? extends Polygon2d> c)
    {
        boolean result = false;
        for (Polygon2d s : c)
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
        for (Iterator<Polygon2d> it = iterator(); it.hasNext();)
        {
            Polygon2d shape = it.next();
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
        for (Iterator<Polygon2d> it = iterator(); it.hasNext();)
        {
            Polygon2d shape = it.next();
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
        this.quadTree.clear();
        this.allShapes.clear();
    }

    /**
     * Return the set of all shapes in this Ots2dSet that intersect the given rectangle.
     * @param rectangle the rectangle
     * @return the shapes that intersect the rectangle
     */
    public final Set<Polygon2d> intersectingShapes(final Bounds2d rectangle)
    {
        return this.quadTree.intersectingShapes(rectangle);
    }

    /**
     * Recursively print this Ots2dSet.
     * @param recursionDepth maximum depth to recurse
     * @return String
     */
    final String toString(final int recursionDepth)
    {
        return "Ots2dSet [contains " + size() + (1 == this.allShapes.size() ? "shape" : "shapes") + ", minimumCellSize="
                + this.minimumCellSize + ", quadTree=" + this.quadTree.toString(recursionDepth) + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return toString(0);
    }

    /**
     * Return all Polygon2ds in this Ots2dSet that intersect a given Polygon2d.
     * @param shape the given Polygon2d
     * @return all Polygon2ds in this Ots2dSet that intersect <code>shape</code>
     */
    public final Set<Polygon2d> intersectingShapes(final Polygon2d shape)
    {
        Bounds2d bounds = shape.getBounds();
        Set<Polygon2d> result =
                intersectingShapes(new Bounds2d(bounds.getMinX(), bounds.getMinY(), bounds.getDeltaX(), bounds.getDeltaY()));
        for (Iterator<Polygon2d> it = result.iterator(); it.hasNext();)
        {
            if (!it.next().intersects(shape))
            {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Return an ASCII art rendering of this Ots2dSet.
     * @param recursionDepth maximum recursion depth
     * @return a somewhat human readable rendering of this Ots2dSet
     */
    public final String toStringGraphic(final int recursionDepth)
    {
        return this.quadTree.toStringGraphic(recursionDepth);
    }

    /**
     * Iterator for quad tree. Shall iterate over the local set of shapes and the (up to four) non-null leave nodes.
     */
    class QuadTreeIterator implements Iterator<Polygon2d>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** Underlying iterator that traverses the allShapes Set. */
        @SuppressWarnings("synthetic-access")
        private final Iterator<Polygon2d> theIterator = Ots2dSet.this.allShapes.iterator();

        /** Remember the last returned result so we can remove it when requested. */
        private Polygon2d lastResult = null;

        /** {@inheritDoc} */
        @Override
        public final boolean hasNext()
        {
            return this.theIterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public final Polygon2d next()
        {
            this.lastResult = this.theIterator.next();
            return this.lastResult;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public final void remove()
        {
            this.theIterator.remove();
            if (!Ots2dSet.this.quadTree.remove(this.lastResult))
            {
                CategoryLogger.always().error("iterator.remove: ERROR: could not remove {} from the quad tree",
                        this.lastResult);
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "QuadTreeIterator [theIterator=" + this.theIterator + ", lastResult=" + this.lastResult + "]";
        }

    }

    /**
     * Spatial-aware storage for a set of Polygon2d objects.
     */
    class QuadTreeNode implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** The Polygon2ds stored at this node. */
        private Set<Polygon2d> shapes = new LinkedHashSet<Polygon2d>();

        /** The bounding box of this QuadTreeNode. */
        private final Bounds2d boundingBox;

        /** The bounding box of this QuadTreeNode as a Polygon2d. */
        private final Polygon2d boundingShape;

        /**
         * The four leaves of this node in the quad tree. An empty sub tree may be represented by null. If this field is
         * initialized to null; this node may not expand by adding sub-nodes.
         */
        private final QuadTreeNode[] leaves;

        /**
         * Construct a new QuadTreeNode.
         * @param boundingBox the bounding box of the area of the new QuadTreeNode
         */
        @SuppressWarnings("synthetic-access")
        QuadTreeNode(final Bounds2d boundingBox)
        {
            this.boundingBox = boundingBox;
            this.boundingShape = rectangleShape(boundingBox);
            this.leaves = boundingBox.getDeltaY() > Ots2dSet.this.minimumCellSize
                    || boundingBox.getDeltaX() > Ots2dSet.this.minimumCellSize ? new QuadTreeNode[4] : null;
        }

        /**
         * Return a Set containing all Polygon2ds in this QuadTreeNode that intersect a rectangular area.
         * @param rectangle the area
         * @return the set
         */
        public Set<Polygon2d> intersectingShapes(final Bounds2d rectangle)
        {
            Set<Polygon2d> result = new LinkedHashSet<Polygon2d>();
            if (!this.boundingBox.intersects(rectangle))
            {
                return result;
            }
            if (null == this.leaves)
            {
                return result;
            }
            for (QuadTreeNode leaf : this.leaves)
            {
                if (null != leaf && leaf.intersects(rectangle))
                {
                    result.addAll(leaf.intersectingShapes(rectangle));
                }
            }
            for (Polygon2d shape : this.shapes)
            {
                Polygon2d rectangleShape = rectangleShape(rectangle);
                if (rectangleShape.intersects(shape))
                {
                    result.add(shape);
                }
            }
            return result;
        }

        /**
         * Test if this QuadTreeNode intersects a rectangular area.
         * @param rectangle the rectangular area
         * @return true if the rectangular area intersects this QuadTreeNode; false otherwise
         */
        private boolean intersects(final Bounds2d rectangle)
        {
            return this.boundingBox.intersects(rectangle);
        }

        /**
         * Remove all Polygon2ds from this QuadTreeNode and cut off all leaves.
         */
        public void clear()
        {
            this.shapes.clear();
            for (int index = 0; index < this.leaves.length; index++)
            {
                this.leaves[index] = null;
            }
        }

        /**
         * Remove a Polygon2d from this QuadTreeNode.
         * @param shape the shape that must be removed.
         * @return true if this node (or a sub-node) was altered; false otherwise
         */
        public boolean remove(final Polygon2d shape)
        {
            if (!this.boundingShape.intersects(shape))
            {
                return false;
            }
            for (Polygon2d s : this.shapes)
            {
                if (shape.equals(s))
                {
                    this.shapes.remove(shape);
                    return true;
                }
            }
            boolean result = false;
            for (int index = 0; index < this.leaves.length; index++)
            {
                QuadTreeNode qtn = this.leaves[index];
                if (null != qtn)
                {
                    if (qtn.remove(shape))
                    {
                        result = true;
                        if (qtn.isEmpty())
                        {
                            this.leaves[index] = null; // Cut off empty leaf node
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Check if this QuadTreeNode is empty.
         * @return true if this QuadTreeNode is empty
         */
        private boolean isEmpty()
        {
            if (!this.shapes.isEmpty())
            {
                return false;
            }
            if (null == this.leaves)
            {
                return true;
            }
            for (QuadTreeNode qtn : this.leaves)
            {
                if (null != qtn)
                {
                    return false;
                }
            }
            return true;
        }

        /**
         * Test if the area of this QuadTree intersects a Polygon2d.
         * @param shape the shape
         * @return true if the area of this QuadTree intersects the shape; false otherwise
         */
        public boolean intersects(final Polygon2d shape)
        {
            return this.boundingShape.intersects(shape);
        }

        /**
         * Construct a Polygon2d from a Rectangle2D.
         * @param rectangle the rectangle
         * @return a new Polygon2d
         */
        private Polygon2d rectangleShape(final Bounds2d rectangle)
        {
            double left = rectangle.getMinX();
            double bottom = rectangle.getMinY();
            double right = rectangle.getMaxX();
            double top = rectangle.getMaxY();
            return new Polygon2d(new Point2d(left, bottom), new Point2d(right, bottom), new Point2d(right, top),
                    new Point2d(left, top));
        }

        /**
         * Add a Polygon2d to this QuadTreeNode.
         * @param shape the shape
         * @return true if this QuadTreeNode changed as a result of this operation
         */
        public final boolean add(final Polygon2d shape)
        {
            if (!this.boundingShape.intersects(shape))
            {
                return false;
            }
            if ((null == this.leaves) || shape.contains(this.boundingBox))
            {
                // shape belongs in the set of shapes of this node.
                return this.shapes.add(shape);
            }
            // This node may have leaves and shape does not entirely contain this node. Add shape to all applicable leaves.
            boolean result = false;
            for (int index = 0; index < this.leaves.length; index++)
            {
                if (null == this.leaves[index])
                {
                    double subWidth = this.boundingBox.getDeltaX() / 2;
                    double subHeight = this.boundingBox.getDeltaY() / 2;
                    if (0 == subWidth)
                    {
                        // loss of precision; degenerate into a binary tree
                        subWidth = this.boundingBox.getDeltaX();
                    }
                    if (0 == subHeight)
                    {
                        // loss of precision; degenerate into a binary tree
                        subHeight = this.boundingBox.getDeltaY();
                    }
                    double left = this.boundingBox.getMinX();
                    if (0 != index / 2)
                    {
                        left += subWidth;
                    }
                    double bottom = this.boundingBox.getMinY();
                    if (0 != index % 2)
                    {
                        bottom += subHeight;
                    }
                    Bounds2d subBox = new Bounds2d(left, left + subWidth, bottom, bottom + subHeight);
                    if (rectangleShape(subBox).intersects(shape))
                    {
                        // Expand this node by adding a sub node.
                        this.leaves[index] = new QuadTreeNode(subBox);
                        if (this.leaves[index].add(shape))
                        {
                            result = true;
                        }
                        else
                        {
                            throw new Error("Cannot happen: new QuadTreeNode refused to add shape that intersects it");
                        }
                    }
                }
                else
                {
                    // Leaf node already exists. Let the leaf determine if shape should be stored (somewhere) in it.
                    if (this.leaves[index].add(shape))
                    {
                        result = true;
                    }
                }
            }
            return result;
        }

        /**
         * Helper function for toString.
         * @param recursionDepth maximum number of levels to print recursively
         * @param index index in leaves
         * @return String
         */
        private String printLeaf(final int recursionDepth, final int index)
        {
            QuadTreeNode leaf = this.leaves[index];
            if (null == leaf)
            {
                return "null";
            }
            if (recursionDepth > 0)
            {
                return leaf.toString(recursionDepth - 1);
            }
            int leafSize = leaf.shapes.size();
            return leafSize + " shape" + (1 == leafSize ? "" : "s");
        }

        /**
         * Recursively print this QuadTreeNode.
         * @param recursionDepth maximum depth to recurse
         * @return String
         */
        final String toString(final int recursionDepth)
        {
            return "QuadTreeNode [" + this.shapes.size() + ", bounds=[LB: " + this.boundingBox.getMinX() + ","
                    + this.boundingBox.getMinY() + ", RT: " + this.boundingBox.getMaxX() + "," + this.boundingBox.getMaxY()
                    + "], " + subNodes(recursionDepth) + ", local " + this.shapes.size()
                    + (1 == this.shapes.size() ? " shape" : " shapes") + "]";
        }

        /**
         * Print the leaves of this QuadTreeNode.
         * @param recursionDepth maximum depth to recurse
         * @return String
         */
        private String subNodes(final int recursionDepth)
        {
            if (null == this.leaves)
            {
                return "cannot have leaves";
            }
            return "leaves=[LB: " + printLeaf(recursionDepth, 0) + ", RB: " + printLeaf(recursionDepth, 1) + ", LT: "
                    + printLeaf(recursionDepth, 2) + ", RT: " + printLeaf(recursionDepth, 3) + "]";
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return toString(0);
        }

        /**
         * Return concatenation of a number of copies of a string.
         * @param count number of copies to concatenate
         * @param string the string to repeat
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
         * Similar to toStringGraphic, but with QuadTreeNode argument which can be null. <br>
         * This code is <b>not</b> optimized for performance; the repeated use of String.split is probably expensive.
         * @param qtn the QuadTreeNode to render. Can be null.
         * @param recursionDepth levels to recurse
         * @return String
         */
        private String subStringGraphic(final QuadTreeNode qtn, final int recursionDepth)
        {
            StringBuffer result = new StringBuffer();
            if (0 == recursionDepth)
            {
                if (null == qtn)
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
                String[] left = subStringGraphic(null == qtn || null == qtn.leaves ? null : qtn.leaves[1], recursionDepth - 1)
                        .split("\\n");
                String[] right = subStringGraphic(null == qtn || null == qtn.leaves ? null : qtn.leaves[3], recursionDepth - 1)
                        .split("\\n");
                String horizontalLine = null;
                for (int i = 0; i < left.length; i++)
                {
                    if (0 == i)
                    {
                        StringBuilder line = new StringBuilder();
                        int width = left[0].length() + 1 + right[0].length();
                        if (null == qtn)
                        {
                            line.append(repeat(width, SPACE));
                        }
                        else
                        {
                            String numberBuf = String.format("%d", qtn.shapes.size());
                            int spare = width - numberBuf.length();
                            line.append(repeat(spare / 2, HLINE));
                            line.append(numberBuf);
                            line.append(repeat(spare - spare / 2, HLINE));
                        }
                        horizontalLine = line.toString();
                    }
                    result.append(left[i]);
                    result.append(null == qtn ? SPACE : VLINE);
                    result.append(right[i]);
                    result.append("\n");
                }
                result.append(horizontalLine);
                result.append("\n");
                left = subStringGraphic(null == qtn || null == qtn.leaves ? null : qtn.leaves[0], recursionDepth - 1)
                        .split("\\n");
                right = subStringGraphic(null == qtn || null == qtn.leaves ? null : qtn.leaves[2], recursionDepth - 1)
                        .split("\\n");
                for (int i = 0; i < left.length; i++)
                {
                    result.append(left[i]);
                    result.append(null == qtn ? SPACE : VLINE);
                    result.append(right[i]);
                    result.append("\n");
                }
                result.append("\n");
            }
            return result.toString();
        }

        /**
         * Return a String depicting this QuadTreeNode.
         * @param recursionDepth levels to recurse
         * @return String
         */
        public final String toStringGraphic(final int recursionDepth)
        {
            return subStringGraphic(this, recursionDepth);
        }

    }

}
