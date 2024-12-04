package org.opentrafficsim.base.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsLine2d.FractionalFallback;

public class DirectionalPolyLineTest
{

    @Test
    public void testLine()
    {
        int n = 121;
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++)
        {
            x[i] = Math.cos(i * Math.PI / (n - 1));
            y[i] = Math.sin(i * Math.PI / (n - 1));
        }
        PolyLine2d line = new PolyLine2d(x, y);
        Direction startDirection = Direction.instantiateSI(.5 * Math.PI);
        Direction endDirection = Direction.instantiateSI(1.5 * Math.PI);

        Try.testFail(() -> new DirectionalPolyLine(null, startDirection, endDirection), NullPointerException.class);
        Try.testFail(() -> new DirectionalPolyLine(line, null, endDirection), NullPointerException.class);
        Try.testFail(() -> new DirectionalPolyLine(line, startDirection, null), NullPointerException.class);

        DirectionalPolyLine baseLine = new DirectionalPolyLine(line, startDirection, endDirection);
        assertEquals(startDirection, baseLine.getStartDirection());
        assertEquals(endDirection, baseLine.getEndDirection());
        
        DirectionalPolyLine offset = baseLine.directionalOffsetLine(-1.0);
        assertEquals(2.0, offset.getFirst().x, 1e-6);
        assertEquals(0.0, offset.getFirst().y, 1e-6);
        assertEquals(-2.0, offset.getLast().x, 1e-6);
        assertEquals(0.0, offset.getLast().y, 1e-6);
        
        offset = baseLine.directionalOffsetLine(0.5);
        assertEquals(0.5, offset.getFirst().x, 1e-6);
        assertEquals(0.0, offset.getFirst().y, 1e-6);
        assertEquals(-0.5, offset.getLast().x, 1e-6);
        assertEquals(0.0, offset.getLast().y, 1e-6);
        
        offset = baseLine.directionalOffsetLine(-1.0, 0.5);
        assertEquals(2.0, offset.getFirst().x, 1e-6);
        assertEquals(0.0, offset.getFirst().y, 1e-6);
        assertEquals(-0.5, offset.getLast().x, 1e-6);
        assertEquals(0.0, offset.getLast().y, 1e-6);
        
        assertEquals(1.0, baseLine.projectFractional(-2.0, 0.0, FractionalFallback.ENDPOINT), 1e-6);
        assertEquals(0.0, baseLine.projectFractional(2.0, 0.0, FractionalFallback.ENDPOINT), 1e-6);
        
        Ray2d ray = baseLine.getLocationFraction(0.0);
        assertEquals(1.0, ray.x, 1e-6);
        assertEquals(0.0, ray.y, 1e-6);
        assertEquals(startDirection.si, ray.phi, 1e-6);
        
        ray = baseLine.getLocationFraction(0.5);
        assertEquals(0.0, ray.x, 1e-6);
        assertEquals(1.0, ray.y, 1e-6);
        assertEquals(Math.PI, ray.phi < 0.0 ? -ray.phi : ray.phi, 0.1);
        
        ray = baseLine.getLocationFraction(1.0);
        assertEquals(-1.0, ray.x, 1e-6);
        assertEquals(0.0, ray.y, 1e-6);
        assertEquals(endDirection.si, ray.phi, 1e-6);
        
        DirectionalPolyLine extract = baseLine.extractFractional(0.0, 0.5);
        ray = extract.getLocationFraction(0.0);
        assertEquals(1.0, ray.x, 1e-6);
        assertEquals(0.0, ray.y, 1e-6);
        assertEquals(startDirection.si, ray.phi, 1e-6);
        ray = extract.getLocationFraction(1.0);
        assertEquals(0.0, ray.x, 1e-6);
        assertEquals(1.0, ray.y, 1e-6);
        assertEquals(Math.PI, ray.phi < 0.0 ? -ray.phi : ray.phi, 0.1);
        
        extract = baseLine.extractFractional(0.5, 1.0);
        ray = extract.getLocationFraction(0.0);
        assertEquals(0.0, ray.x, 1e-6);
        assertEquals(1.0, ray.y, 1e-6);
        assertEquals(Math.PI, ray.phi < 0.0 ? -ray.phi : ray.phi, 0.1);
        ray = extract.getLocationFraction(1.0);
        assertEquals(-1.0, ray.x, 1e-6);
        assertEquals(0.0, ray.y, 1e-6);
        assertEquals(endDirection.si, ray.phi, 1e-6);
    }

}
