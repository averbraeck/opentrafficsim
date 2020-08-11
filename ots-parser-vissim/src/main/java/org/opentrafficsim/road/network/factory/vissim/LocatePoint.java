/**
 *
 */
package org.opentrafficsim.road.network.factory.vissim;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

/**
 * Computes the location of the point a given distance along a LineString.
 */
public class LocatePoint
{

    public static Coordinate pointAlongSegment(Coordinate p0, Coordinate p1, double distance)
    {
        double segLen = p1.distance(p0);
        double frac = distance / segLen;
        if (frac <= 0.0)
        {
            return p0;
        }
        if (frac >= 1.0)
        {
            return p1;
        }

        double x = (p1.x - p0.x) * frac + p0.x;
        double y = (p1.y - p0.y) * frac + p0.y;
        return new Coordinate(x, y);
    }

    public static Coordinate pointAlongLine(LineString line, double distance)
    {
        LocatePoint loc = new LocatePoint(line, distance);
        return loc.getPoint();
    }

    private Coordinate pt;

    private int index;

    public LocatePoint(LineString line, double distance)
    {
        compute(line, distance);
    }

    private void compute(LineString line, double distance)
    {
        // <TODO> handle negative distances (measure from opposite end of line)
        double totalDist = 0.0;
        Coordinate[] coord = line.getCoordinates();
        for (int i = 0; i < coord.length - 1; i++)
        {
            Coordinate p0 = coord[i];
            Coordinate p1 = coord[i + 1];
            double segLen = p1.distance(p0);
            if (totalDist + segLen > distance)
            {
                pt = pointAlongSegment(p0, p1, distance - totalDist);
                index = i;
                return;
            }
            totalDist += segLen;
        }
        // distance is greater than line length
        pt = new Coordinate(coord[coord.length - 1]);
        index = coord.length;
    }

    public Coordinate getPoint()
    {
        return pt;
    }

    /**
     * Returns the index of the segment containing the computed point.
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

}
