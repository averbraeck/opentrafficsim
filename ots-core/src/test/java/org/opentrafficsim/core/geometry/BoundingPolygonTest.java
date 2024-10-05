package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.PolygonShape;

public class BoundingPolygonTest
{

    /**
     * This code check a bug fix where a transformation in BoundingPolygon.geometryToBounds(...) would create duplicate points
     * from points that were already very close together.
     * @throws OtsGeometryException
     */
    @Test
    public void boundingPolygonTest() throws OtsGeometryException
    {
        List<Point2d> pointList = new ArrayList<>();
        pointList.add(new Point2d(58.8008890211771, -3.98492795650657));
        pointList.add(new Point2d(58.7352328155604, -3.87671405467892));
        pointList.add(new Point2d(58.6022745872352, -3.66684760589021));
        pointList.add(new Point2d(58.463988530184, -3.45735023359504));
        pointList.add(new Point2d(58.3205653656466, -3.2484126590331));
        pointList.add(new Point2d(58.1721958148626, -3.04022560344412));
        pointList.add(new Point2d(58.0190705990717, -2.83297978806779));
        pointList.add(new Point2d(57.8613804395136, -2.62686593414384));
        pointList.add(new Point2d(57.6993160574282, -2.42207476291196));
        pointList.add(new Point2d(57.533068174055, -2.21879699561188));
        pointList.add(new Point2d(57.3628275106338, -2.0172233534833));
        pointList.add(new Point2d(57.1887847884042, -1.81754455776593));
        pointList.add(new Point2d(57.0111307286061, -1.61995132969947));
        pointList.add(new Point2d(56.8300560524791, -1.42463439052365));
        pointList.add(new Point2d(56.6457514812629, -1.23178446147817));
        pointList.add(new Point2d(56.503621700788, -1.08749368041061));

        OtsLine2d line = new OtsLine2d(pointList);

        OtsLine2d left = line.offsetLine(1.4000000000000004, 1.4000000000000001);
        OtsLine2d right = line.offsetLine(-1.4000000000000004, -1.4000000000000001).reverse();

        Point2d[] points = new Point2d[left.size() + right.size()];
        System.arraycopy(left.getPoints(), 0, points, 0, left.size());
        System.arraycopy(right.getPoints(), 0, points, left.size(), right.size());

        Polygon2d geometry = new Polygon2d(points);

        OrientedPoint2d location = new OrientedPoint2d(58.80088902117715, -3.9849279565065774, 2.1161468637905547);
        PolygonShape.geometryToBounds(location, geometry);
    }

}
