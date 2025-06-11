
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The width of a lane is defined along the t-coordinate. The width of a lane may change within a lane section.
 * Lane width and lane border elements are mutually exclusive within the same lane group. If both width and lane border elements are present for a lane section in the OpenDRIVE file, the application must use the information from the <width> elements.
 * In OpenDRIVE, lane width is described by the <width> element within the <lane> element.
 * 
 * <p>Java-Klasse für t_road_lanes_laneSection_lr_lane_width complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_width">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
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
@XmlType(name = "t_road_lanes_laneSection_lr_lane_width")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneWidth
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position of the <width> element, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Polynom parameter a, width at @s (ds=0)
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
     * s-coordinate of start position of the <width> element, relative to the position of the preceding <laneSection> element
     * 
     */
    public double getSOffset() {
        return sOffset;
    }

    /**
     * Legt den Wert der sOffset-Eigenschaft fest.
     * 
     */
    public void setSOffset(double value) {
        this.sOffset = value;
    }

    /**
     * Polynom parameter a, width at @s (ds=0)
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
