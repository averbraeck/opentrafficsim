package org.opentrafficsim.core.geometry;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.locationtech.jts.geom.Envelope;

import nl.tudelft.simulation.dsol.logger.SimLogger;

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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTS2DSet implements Set<OTSShape>, Serializable
{
    /** */
    private static final long serialVersionUID = 20170400L;

    /** Set of all shapes used for iterators, etc. */
    private final Set<OTSShape> allShapes = new LinkedHashSet<OTSShape>();

    /** How fine will this quad tree divide. This one is copied to each sub-node which is somewhat inefficient. */
    private final double minimumCellSize;

    /** Spatial storage for the OTSShapes. */
    private QuadTreeNode quadTree;

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
    public final Iterator<OTSShape> iterator()
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
    public final boolean add(final OTSShape e)
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
            SimLogger.always().error("add: ERROR object could not be added to the quad tree");
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
        if (!this.quadTree.remove((OTSShape) o))
        {
            SimLogger.always().error("remove: ERROR object could not be removed from the quad tree");
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
        this.quadTree.clear();
        this.allShapes.clear();
    }

    /**
     * Return the set of all shapes in this OTS2DSet that intersect the given rectangle.
     * @param rectangle Rectangle2D; the rectangle
     * @return Set&lt;OTSShape&gt;; the shapes that intersect the rectangle
     */
    public final Set<OTSShape> intersectingShapes(final Rectangle2D rectangle)
    {
        return this.quadTree.intersectingShapes(rectangle);
    }

    /**
     * Recursively print this OTS2DSet.
     * @param recursionDepth int; maximum depth to recurse
     * @return String
     */
    final String toString(final int recursionDepth)
    {
        return "OTS2DSet [contains " + size() + (1 == this.allShapes.size() ? "shape" : "shapes") + ", minimumCellSize="
                + this.minimumCellSize + ", quadTree=" + this.quadTree.toString(recursionDepth) + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return toString(0);
    }

    /**
     * Return all OTSShapes in this OTS2DSet that intersect a given OTSShape.
     * @param shape OTSShape; the given OTSShape
     * @return Set&lt;OTSShape&gt;; all OTSShapes in this OTS2DSet that intersect <code>shape</code>
     */
    public final Set<OTSShape> intersectingShapes(final OTSShape shape)
    {
        Envelope envelope = shape.getEnvelope();
        Set<OTSShape> result = intersectingShapes(
                new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight()));
        for (Iterator<OTSShape> it = result.iterator(); it.hasNext();)
        {
            if (!it.next().intersects(shape))
            {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Return an ASCII art rendering of this OTS2DSet.
     * @param recursionDepth int; maximum recursion depth
     * @return String; a somewhat human readable rendering of this OTS2DSet
     */
    public final String toStringGraphic(final int recursionDepth)
    {
        return this.quadTree.toStringGraphic(recursionDepth);
    }

    /**
     * Iterator for quad tree. Shall iterate over the local set of shapes and the (up to four) non-null leave nodes.
     */
    class QuadTreeIterator implements Iterator<OTSShape>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** Underlying iterator that traverses the allShapes Set. */
        @SuppressWarnings("synthetic-access")
        private final Iterator<OTSShape> theIterator = OTS2DSet.this.allShapes.iterator();

        /** Remember the last returned result so we can remove it when requested. */
        private OTSShape lastResult = null;

        /** {@inheritDoc} */
        @Override
        public final boolean hasNext()
        {
            return this.theIterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public final OTSShape next()
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
            if (!OTS2DSet.this.quadTree.remove(this.lastResult))
            {
                SimLogger.always().error("iterator.remove: ERROR: could not remove {} from the quad tree", this.lastResult);
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
     * Spatial-aware storage for a set of OTSShape objects.
     */
    class QuadTreeNode implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** The OTSShapes stored at this node. */
        private Set<OTSShape> shapes = new LinkedHashSet<OTSShape>();

        /** The bounding box of this QuadTreeNode. */
        private final Rectangle2D boundingBox;

        /** The bounding box of this QuadTreeNode as an OTSShape. */
        private final OTSShape boundingShape;

        /**
         * The four leaves of this node in the quad tree. An empty sub tree may be represented by null. If this field is
         * initialized to null; this node may not expand by adding sub-nodes.
         */
        private final QuadTreeNode[] leaves;

        /**
         * Construct a new QuadTreeNode.
         * @param boundingBox Rectangle2D; the bounding box of the area of the new QuadTreeNode
         */
        @SuppressWarnings("synthetic-access")
        QuadTreeNode(final Rectangle2D boundingBox)
        {
            this.boundingBox = boundingBox;
            this.boundingShape = rectangleShape(boundingBox);
            this.leaves = boundingBox.getWidth() > OTS2DSet.this.minimumCellSize
                    || boundingBox.getHeight() > OTS2DSet.this.minimumCellSize ? new QuadTreeNode[4] : null;
        }

        /**
         * Return a Set containing all OTSShapes in this QuadTreeNode that intersect a rectangular area.
         * @param rectangle Rectangle2D; the area
         * @return Set&lt;OTSShape&gt;; the set
         */
        public Set<OTSShape> intersectingShapes(final Rectangle2D rectangle)
        {
            Set<OTSShape> result = new LinkedHashSet<OTSShape>();
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
         * Test if this QuadTreeNode intersects a rectangular area.
         * @param rectangle Rectangle2D; the rectangular area
         * @return boolean; true if the rectangular area intersects this QuadTreeNode; false otherwise
         */
        private boolean intersects(final Rectangle2D rectangle)
        {
            return this.boundingBox.intersects(rectangle);
        }

        /**
         * Remove all OTSShapes from this QuadTreeNode and cut off all leaves.
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
         * Remove an OTSShape from this QuadTreeNode.
         * @param shape OTSShape; the shape that must be removed.
         * @return boolean; true if this node (or a sub-node) was altered; false otherwise
         */
        public boolean remove(final OTSShape shape)
        {
            if (!this.boundingShape.intersects(shape))
            {
                return false;
            }
            for (OTSShape s : this.shapes)
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
         * @return boolean; true if this QuadTreeNode is empty
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
         * Test if the area of this QuadTree intersects an OTSShape.
         * @param shape OTSShape; the shape
         * @return boolean; true if the area of this QuadTree intersects the shape; false otherwise
         */
        public boolean intersects(final OTSShape shape)
        {
            return this.boundingShape.intersects(shape);
        }

        /**
         * Construct a OTSShape from a Rectangle2D.
         * @param rectangle Rectangle2D; the rectangle
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
                SimLogger.always().error(exception);
                return null;
            }
        }

        /**
         * Add an OTSShape to this QuadTreeNode.
         * @param shape OTSShape; the shape
         * @return boolean; true if this QuadTreeNode changed as a result of this operation
         */
        public final boolean add(final OTSShape shape)
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
                    double subWidth = this.boundingBox.getWidth() / 2;
                    double subHeight = this.boundingBox.getHeight() / 2;
                    if (0 == subWidth)
                    {
                        // loss of precision; degenerate into a binary tree
                        subWidth = this.boundingBox.getWidth();
                    }
                    if (0 == subHeight)
                    {
                        // loss of precision; degenerate into a binary tree
                        subHeight = this.boundingBox.getHeight();
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
                    Rectangle2D subBox = new Rectangle2D.Double(left, bottom, subWidth, subHeight);
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
         * @param recursionDepth int; maximum number of levels to print recursively
         * @param index int; index in leaves
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
         * @param recursionDepth int; maximum depth to recurse
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
         * @param recursionDepth int; maximum depth to recurse
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
         * Similar to toStringGraphic, but with QuadTreeNode argument which can be null. <br>
         * This code is <b>not</b> optimized for performance; the repeated use of String.split is probably expensive.
         * @param qtn QuadTreeNode; the QuadTreeNode to render. Can be null.
         * @param recursionDepth int; levels to recurse
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
         * @param recursionDepth int; levels to recurse
         * @return String
         */
        public final String toStringGraphic(final int recursionDepth)
        {
            return subStringGraphic(this, recursionDepth);
        }

    }

}
