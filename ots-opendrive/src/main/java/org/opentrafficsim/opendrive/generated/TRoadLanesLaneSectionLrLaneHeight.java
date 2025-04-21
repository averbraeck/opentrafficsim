
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Lane height shall be defined along the h-coordinate. Lane height may be used to elevate a lane independent from the road elevation. Lane height is used to implement small-scale elevation such as raising pedestrian walkways. Lane height is specified as offset from the road (including elevation, superelevation, shape) in z direction.
 * 
 * <p>Java class for t_road_lanes_laneSection_lr_lane_height complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_height">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="inner" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="outer" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane_height")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneHeight
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Inner offset from road level
     * 
     */
    @XmlAttribute(name = "inner", required = true)
    protected double inner;
    /**
     * Outer offset from road level
     * 
     */
    @XmlAttribute(name = "outer", required = true)
    protected double outer;

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    public double getSOffset() {
        return sOffset;
    }

    /**
     * Sets the value of the sOffset property.
     * 
     */
    public void setSOffset(double value) {
        this.sOffset = value;
    }

    /**
     * Inner offset from road level
     * 
     */
    public double getInner() {
        return inner;
    }

    /**
     * Sets the value of the inner property.
     * 
     */
    public void setInner(double value) {
        this.inner = value;
    }

    /**
     * Outer offset from road level
     * 
     */
    public double getOuter() {
        return outer;
    }

    /**
     * Sets the value of the outer property.
     * 
     */
    public void setOuter(double value) {
        this.outer = value;
    }

}
