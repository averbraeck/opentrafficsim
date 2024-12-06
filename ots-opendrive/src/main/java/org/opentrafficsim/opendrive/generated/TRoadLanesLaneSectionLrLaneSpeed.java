
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.opendrive.bindings.SpeedUnitAdapter;


/**
 * Defines the maximum allowed speed on a given lane. Each element is valid in direction of the increasing s-coordinate until a new element is defined.
 * 
 * <p>Java class for t_road_lanes_laneSection_lr_lane_speed complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_speed">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="max" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="unit" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_unitSpeed" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane_speed")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneSpeed
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Maximum allowed speed. If the attribute unit is not specified, m/s is used as default.
     * 
     */
    @XmlAttribute(name = "max", required = true)
    protected double max;
    /**
     * Unit of the attribute max. For values, see UML Model
     * 
     */
    @XmlAttribute(name = "unit")
    @XmlJavaTypeAdapter(SpeedUnitAdapter.class)
    protected SpeedUnit unit;

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
     * Maximum allowed speed. If the attribute unit is not specified, m/s is used as default.
     * 
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(double value) {
        this.max = value;
    }

    /**
     * Unit of the attribute max. For values, see UML Model
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedUnit getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getUnit()
     */
    public void setUnit(SpeedUnit value) {
        this.unit = value;
    }

}
