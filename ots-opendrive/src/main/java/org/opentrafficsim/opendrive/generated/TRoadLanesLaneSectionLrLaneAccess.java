
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Defines access restrictions for certain types of road users.
 * Each element is valid in direction of the increasing s co-ordinate until a new element is defined. If multiple elements are defined, they must be listed in increasing order.
 * 
 * <p>Java class for t_road_lanes_laneSection_lr_lane_access complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_access">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="rule" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_lanes_laneSection_lr_lane_access_rule" />
 *       <attribute name="restriction" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_accessRestrictionType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane_access")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneAccess
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Specifies whether the participant given in the attribute @restriction is allowed or denied access to the given lane
     * 
     */
    @XmlAttribute(name = "rule")
    protected ERoadLanesLaneSectionLrLaneAccessRule rule;
    /**
     * Identifier of the participant to whom the restriction applies. For values, see UML Model
     * 
     */
    @XmlAttribute(name = "restriction", required = true)
    protected EAccessRestrictionType restriction;

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
     * Specifies whether the participant given in the attribute @restriction is allowed or denied access to the given lane
     * 
     * @return
     *     possible object is
     *     {@link ERoadLanesLaneSectionLrLaneAccessRule }
     *     
     */
    public ERoadLanesLaneSectionLrLaneAccessRule getRule() {
        return rule;
    }

    /**
     * Sets the value of the rule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadLanesLaneSectionLrLaneAccessRule }
     *     
     * @see #getRule()
     */
    public void setRule(ERoadLanesLaneSectionLrLaneAccessRule value) {
        this.rule = value;
    }

    /**
     * Identifier of the participant to whom the restriction applies. For values, see UML Model
     * 
     * @return
     *     possible object is
     *     {@link EAccessRestrictionType }
     *     
     */
    public EAccessRestrictionType getRestriction() {
        return restriction;
    }

    /**
     * Sets the value of the restriction property.
     * 
     * @param value
     *     allowed object is
     *     {@link EAccessRestrictionType }
     *     
     * @see #getRestriction()
     */
    public void setRestriction(EAccessRestrictionType value) {
        this.restriction = value;
    }

}
