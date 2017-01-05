/**
 *
 */
package org.opentrafficsim.road.network.factory.vissim;

/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Computes the location of the point a given distance along a LineString
 */
public class LocatePoint {

    public static Coordinate pointAlongSegment(Coordinate p0, Coordinate p1, double distance) {
        double segLen = p1.distance(p0);
        double frac = distance / segLen;
        if (frac <= 0.0) {
            return p0;
        }
        if (frac >= 1.0) {
            return p1;
        }

        double x = (p1.x - p0.x) * frac + p0.x;
        double y = (p1.y - p0.y) * frac + p0.y;
        return new Coordinate(x, y);
    }

    public static Coordinate pointAlongLine(LineString line, double distance) {
        LocatePoint loc = new LocatePoint(line, distance);
        return loc.getPoint();
    }

    private Coordinate pt;

    private int index;

    public LocatePoint(LineString line, double distance) {
        compute(line, distance);
    }

    private void compute(LineString line, double distance) {
        // <TODO> handle negative distances (measure from opposite end of line)
        double totalDist = 0.0;
        Coordinate[] coord = line.getCoordinates();
        for (int i = 0; i < coord.length - 1; i++) {
            Coordinate p0 = coord[i];
            Coordinate p1 = coord[i + 1];
            double segLen = p1.distance(p0);
            if (totalDist + segLen > distance) {
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

    public Coordinate getPoint() {
        return pt;
    }

    /**
     * Returns the index of the segment containing the computed point
     * @return the index
     */
    public int getIndex() {
        return index;
    }

}
