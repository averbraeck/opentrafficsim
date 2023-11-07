package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.List;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;

/**
 * Cross section element data for in the editor.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EditorCrossSectionData implements CrossSectionElementData
{

    /** Center line. */
    protected final PolyLine2d centerLine;

    /** Contour. */
    private final Polygon2d contour;

    /**
     * Constructor.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     */
    public EditorCrossSectionData(final PolyLine2d centerLine, final Polygon2d contour)
    {
        this.centerLine = centerLine;
        this.contour = contour;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return new Bounds2d(this.contour.getBounds().getDeltaX(), this.contour.getBounds().getDeltaY());
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.contour.getBounds().midPoint();
    }

    /** {@inheritDoc} */
    @Override
    public List<Point2d> getContour()
    {
        return this.contour.getPointList();
    }

}
