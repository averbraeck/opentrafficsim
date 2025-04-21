
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Lane elements are included in left/center/right elements. Lane elements should represent the lanes from left to right, that is, with descending ID.
 * 
 * <p>Java class for t_road_lanes_laneSection_right_lane complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_right_lane">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection_lr_lane">
 *       <sequence>
 *       </sequence>
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}negativeInteger" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_right_lane")
@SuppressWarnings("all") public class TRoadLanesLaneSectionRightLane
    extends TRoadLanesLaneSectionLrLane
{

    /**
     * ID of the lane
     * 
     */
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "negativeInteger")
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
        return id;
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
