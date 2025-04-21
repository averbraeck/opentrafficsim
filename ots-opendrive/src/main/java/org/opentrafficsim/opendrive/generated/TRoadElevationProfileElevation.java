
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Defines an elevation element at a given position on the reference line. Elements shall be defined in increasing order along the reference line. The s length does not change with the elevation.
 * 
 * <p>Java class for t_road_elevationProfile_elevation complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_elevationProfile_elevation">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="a" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="b" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="c" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="d" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_elevationProfile_elevation")
@SuppressWarnings("all") public class TRoadElevationProfileElevation
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * Polynom parameter a, elevation at @s (ds=0)
     * 
     */
    @XmlAttribute(name = "a", required = true)
    protected double a;
    /**
     * Polynom parameter b
     * 
     */
    @XmlAttribute(name = "b", required = true)
    protected double b;
    /**
     * Polynom parameter c
     * 
     */
    @XmlAttribute(name = "c", required = true)
    protected double c;
    /**
     * Polynom parameter d
     * 
     */
    @XmlAttribute(name = "d", required = true)
    protected double d;

    /**
     * s-coordinate of start position
     * 
     */
    public double getS() {
        return s;
    }

    /**
     * Sets the value of the s property.
     * 
     */
    public void setS(double value) {
        this.s = value;
    }

    /**
     * Polynom parameter a, elevation at @s (ds=0)
     * 
     */
    public double getA() {
        return a;
    }

    /**
     * Sets the value of the a property.
     * 
     */
    public void setA(double value) {
        this.a = value;
    }

    /**
     * Polynom parameter b
     * 
     */
    public double getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(double value) {
        this.b = value;
    }

    /**
     * Polynom parameter c
     * 
     */
    public double getC() {
        return c;
    }

    /**
     * Sets the value of the c property.
     * 
     */
    public void setC(double value) {
        this.c = value;
    }

    /**
     * Polynom parameter d
     * 
     */
    public double getD() {
        return d;
    }

    /**
     * Sets the value of the d property.
     * 
     */
    public void setD(double value) {
        this.d = value;
    }

}
