package org.opentrafficsim.draw.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.djutils.exceptions.Throw;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.Size2D;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.draw.ColorPaintScale;

/**
 * Renderer for blocks that are filled with bidirectionally interpolated colors. It extends a {@code XYBlockRenderer} and
 * requires a small extension of the underlying dataset ({@code XyInterpolatedDataset}). The interpolation is performed in the
 * {@code drawItem} method. This class imposes two constraints on the functionality of the super class: i) no BlockAnchor may be
 * set as this is tightly related to the interpolation, and ii) only paint scales of type {@code ColorPaintScale} can be used,
 * as the interpolation obtains pixel colors from it.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XyInterpolatedBlockRenderer extends XYBlockRenderer
{

    /** */
    private static final long serialVersionUID = 20181008L;

    /** Whether to use the interpolation. */
    private boolean interpolate = true;

    /** Dataset that allows retrieving surrounding value for interpolation. */
    private final XyInterpolatedDataset xyInterpolatedDataset;

    /**
     * Constructor.
     * @param xyInterpolatedDataset dataset that allows retrieving surrounding value for interpolation
     */
    public XyInterpolatedBlockRenderer(final XyInterpolatedDataset xyInterpolatedDataset)
    {
        this.xyInterpolatedDataset = xyInterpolatedDataset;
    }

    /**
     * {@inheritDoc} throws UnsupportedOperationException if the paint scale is not of type ColorPaintScale
     */
    @Override
    public void setPaintScale(final PaintScale scale)
    {
        Throw.when(!(scale instanceof ColorPaintScale), UnsupportedOperationException.class,
                "Class XYInterpolatedBlockRenderer requires a ColorPaintScale.");
        super.setPaintScale(scale);
    }

    /**
     * {@inheritDoc} throws UnsupportedOperationException block anchor is governed based on interpolation
     */
    @Override
    public void setBlockAnchor(final RectangleAnchor anchor)
    {
        throw new UnsupportedOperationException(
                "Class XYInterpolatedBlockRenderer does not support setting the anchor, it's coupled to interpolation.");
    }

    /**
     * Enables interpolation or not. Interpolation occurs between cell centers. Therefore the painted blocks are shifted right
     * and up. The user of this class must provide an additional row and column of data to fill up the gaps. These values may be
     * NaN.
     * @param interpolate interpolate or not
     */
    public final void setInterpolate(final boolean interpolate)
    {
        this.interpolate = interpolate;
        if (interpolate)
        {
            super.setBlockAnchor(RectangleAnchor.TOP_LEFT); // reversed y axis
        }
        else
        {
            super.setBlockAnchor(RectangleAnchor.CENTER);
        }
    }

    /**
     * {@inheritDoc} This code is partially based on the parent implementation.
     */
    @Override
    @SuppressWarnings("parameternumber")
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass)
    {

        double z00 = this.xyInterpolatedDataset.getZValue(series, item);
        Paint p;

        if (!this.interpolate)
        {
            // regular non interpolated case
            p = getPaintScale().getPaint(z00);
        }
        else
        {
            // obtain data values in surrounding cells (up, right, and up-right)
            double z10 = getAdjacentZ(series, item, true, false);
            double z01 = getAdjacentZ(series, item, false, true);
            double z11 = getAdjacentZ(series, item, true, true);

            // fix NaN values
            double z00f = fixNaN(z00, z01, z10, z11);
            double z10f = fixNaN(z10, z00, z11, z01);
            double z01f = fixNaN(z01, z00, z11, z10);
            double z11f = fixNaN(z11, z10, z01, z00);

            // use these values to derive an interpolated color raster
            p = new Paint()
            {
                @Override
                public int getTransparency()
                {
                    return TRANSLUCENT;
                }

                @Override
                public PaintContext createContext(final ColorModel cm, final Rectangle deviceBounds,
                        final Rectangle2D userBounds, final AffineTransform xform, final RenderingHints hints)
                {
                    return new PaintContext()
                    {
                        @Override
                        public void dispose()
                        {
                            //
                        }

                        @Override
                        public ColorModel getColorModel()
                        {
                            return ColorModel.getRGBdefault();
                        }

                        @Override
                        public Raster getRaster(final int x, final int y, final int w, final int h)
                        {
                            // a raster can be obtained for any square subset of 1 cell, obtain the offset
                            double wOffset = x - deviceBounds.getX();
                            double hOffset = y - deviceBounds.getY();

                            // initialize a writable raster
                            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);

                            // loop pixels in data buffer (raster.setPixel(i, j, float[]) doesn't work...)
                            for (int k = 0; k < raster.getDataBuffer().getSize(); k++)
                            {
                                // coordinate (i, j) is where pixel k is within the bounds
                                double i = hOffset + k / w;
                                double j = wOffset + k % w;

                                // get weights relative to the edges
                                double bot = i / deviceBounds.getHeight();
                                double top = 1.0 - bot;
                                double rig = j / deviceBounds.getWidth();
                                double lef = 1.0 - rig;

                                // bilinear interpolation of the value
                                double z = z00f * lef * bot + z10f * top * lef + z01f * bot * rig + z11f * top * rig;

                                // with the interpolated value, obtain a color the simple way
                                Color c = (Color) getPaintScale().getPaint(z); // paint scale forced of type ColorPaintScale

                                // write
                                raster.getDataBuffer().setElem(k, c.getRGB());
                            }
                            return raster;
                        }
                    };
                }
            };

        }

        // use rect to obtain x and y range, accounting for offset (direct information is private in super class)
        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        Rectangle2D rect =
                RectangleAnchor.createRectangle(new Size2D(getBlockWidth(), getBlockHeight()), x, y, getBlockAnchor());
        double xx0 = domainAxis.valueToJava2D(rect.getMinX(), dataArea, plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(rect.getMinY(), dataArea, plot.getRangeAxisEdge());
        double xx1 = domainAxis.valueToJava2D(rect.getMaxX(), dataArea, plot.getDomainAxisEdge());
        double yy1 = rangeAxis.valueToJava2D(rect.getMaxY(), dataArea, plot.getRangeAxisEdge());

        // code below this is equal to the super implementation
        Rectangle2D block;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation.equals(PlotOrientation.HORIZONTAL))
        {
            block = new Rectangle2D.Double(Math.min(yy0, yy1), Math.min(xx0, xx1), Math.abs(yy1 - yy0), Math.abs(xx0 - xx1));
        }
        else
        {
            block = new Rectangle2D.Double(Math.min(xx0, xx1), Math.min(yy0, yy1), Math.abs(xx1 - xx0), Math.abs(yy1 - yy0));
        }
        g2.setPaint(p);
        g2.fill(block);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(block);

        if (isItemLabelVisible(series, item))
        {
            drawItemLabel(g2, orientation, dataset, series, item, block.getCenterX(), block.getCenterY(), y < 0.0);
        }

        int datasetIndex = plot.indexOf(dataset);
        double transX = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
        double transY = rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
        updateCrosshairValues(crosshairState, x, y, datasetIndex, transX, transY, orientation);

        EntityCollection entities = state.getEntityCollection();
        if (entities != null)
        {
            addEntity(entities, block, dataset, series, item, block.getCenterX(), block.getCenterY());
        }
    }

    /**
     * Returns the value of an adjacent cell.
     * @param series the series index
     * @param item item
     * @param up whether to get the upper cell (can be combined with right)
     * @param right whether to get the right cell (can be combined with up)
     * @return value in adjacent cell, or {@code Double.NaN} if no such cell.
     */
    private double getAdjacentZ(final int series, final int item, final boolean up, final boolean right)
    {
        if (up && (item + 1) % this.xyInterpolatedDataset.getRangeBinCount() == 0)
        {
            // we cannot interpolate beyond the range extent
            return Double.NaN;
        }
        int adjacentItem = item + (up ? 1 : 0) + (right ? this.xyInterpolatedDataset.getRangeBinCount() : 0);
        if (adjacentItem >= this.xyInterpolatedDataset.getItemCount(series))
        {
            // we cannot interpolate beyond the domain extent
            return Double.NaN;
        }
        return this.xyInterpolatedDataset.getZValue(series, adjacentItem);
    }

    /**
     * Restores a corner value if it's NaN using surrounding values. If both adjacent corner points are not NaN, the mean of
     * those is used. If either is not NaN, that value is used. Otherwise the opposite corner point is used (which may be NaN).
     * This method's main purpose is to fill the left side of the first column of cells and the bottom of the first row of cells
     * in case of interpolation. Coincidentally it can also fill small data gaps visually.
     * @param value value to fix (if needed)
     * @param adjacentCorner1 adjacent corner value
     * @param adjacentCorner2 other adjacent corner value
     * @param oppositeCorner opposite corner value
     * @return fixed value (if possible, i.e. not all corners are NaN)
     */
    private double fixNaN(final double value, final double adjacentCorner1, final double adjacentCorner2,
            final double oppositeCorner)
    {
        if (!Double.isNaN(value))
        {
            return value;
        }
        if (Double.isNaN(adjacentCorner1))
        {
            if (Double.isNaN(adjacentCorner2))
            {
                return oppositeCorner;
            }
            else
            {
                return adjacentCorner2;
            }
        }
        else if (Double.isNaN(adjacentCorner2))
        {
            return adjacentCorner1;
        }
        return 0.5 * (adjacentCorner1 + adjacentCorner2);
    }

    @Override
    public String toString()
    {
        return "XYInterpolatedBlockRenderer [interpolate=" + this.interpolate + ", xyInterpolatedDataset="
                + this.xyInterpolatedDataset + "]";
    }

}
