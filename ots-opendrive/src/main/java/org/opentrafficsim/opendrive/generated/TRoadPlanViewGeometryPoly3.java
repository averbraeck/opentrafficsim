
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * In OpenDRIVE, a cubic polynom is represented by a <poly3> element within the <geometry> element.
 * 
 * <p>Java-Klasse für t_road_planView_geometry_poly3 complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_planView_geometry_poly3">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
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
@XmlType(name = "t_road_planView_geometry_poly3")
@SuppressWarnings("all") public class TRoadPlanViewGeometryPoly3
    extends OpenDriveElement
{

    /**
     * Polynom parameter a
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
     * Polynom parameter a
     * 
     */
    public double getA() {
        return a;
    }

    /**
     * Legt den Wert der a-Eigenschaft fest.
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
     * Legt den Wert der b-Eigenschaft fest.
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
     * Legt den Wert der c-Eigenschaft fest.
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
     * Legt den Wert der d-Eigenschaft fest.
     * 
     */
    public void setD(double value) {
        this.d = value;
    }

}
