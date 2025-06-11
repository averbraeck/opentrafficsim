
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Used to add rules that are not covered by any of the other lane attributes that are described in this specification.
 * 
 * <p>Java-Klasse für t_road_lanes_laneSection_lr_lane_rule complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_rule">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane_rule")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneRule
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Free text; currently recommended values are
     * "no stopping at any time"
     * "disabled parking"
     * "car pool"
     * 
     */
    @XmlAttribute(name = "value", required = true)
    protected String value;

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
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
     * Free text; currently recommended values are
     * "no stopping at any time"
     * "disabled parking"
     * "car pool"
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getValue()
     */
    public void setValue(String value) {
        this.value = value;
    }

}
