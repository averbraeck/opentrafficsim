
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Relocates the lateral reference position for the following (explicit) type definition and thus defines an offset. The sway offset is relative to the nominal reference position of the lane marking, meaning the lane border.
 * 
 * <p>Java class for t_road_lanes_laneSection_lcr_lane_roadMark_sway complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lcr_lane_roadMark_sway">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="ds" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
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
@XmlType(name = "t_road_lanes_laneSection_lcr_lane_roadMark_sway")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLcrLaneRoadMarkSway
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position of the <sway> element, relative to the @sOffset given in the <roadMark> element
     * 
     */
    @XmlAttribute(name = "ds", required = true)
    protected double ds;
    /**
     * Polynom parameter a, sway value at @s (ds=0)
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
     * s-coordinate of start position of the <sway> element, relative to the @sOffset given in the <roadMark> element
     * 
     */
    public double getDs() {
        return ds;
    }

    /**
     * Sets the value of the ds property.
     * 
     */
    public void setDs(double value) {
        this.ds = value;
    }

    /**
     * Polynom parameter a, sway value at @s (ds=0)
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
