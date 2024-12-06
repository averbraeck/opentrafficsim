
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Lane elements are included in left/center/right elements. Lane elements should represent the lanes from left to right, that is, with descending ID.
 * 
 * <p>Java class for t_road_lanes_laneSection_center_lane complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_center_lane">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane">
 *       <sequence>
 *       </sequence>
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" fixed="0" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_center_lane")
@SuppressWarnings("all") public class TRoadLanesLaneSectionCenterLane
    extends TRoadLanesLaneSectionLrLane
{

    /**
     * ID of the lane
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected BigInteger id;

    /**
     * ID of the lane
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        if (id == null) {
            return new BigInteger("0");
        } else {
            return id;
        }
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getId()
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

}
