
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_road_lanes_laneSection_lr_lane_access_rule</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_road_lanes_laneSection_lr_lane_access_rule">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="allow"/>
 *     <enumeration value="deny"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_road_lanes_laneSection_lr_lane_access_rule")
@XmlEnum
@SuppressWarnings("all") public enum ERoadLanesLaneSectionLrLaneAccessRule {

    @XmlEnumValue("allow")
    ALLOW("allow"),
    @XmlEnumValue("deny")
    DENY("deny");
    private final String value;

    ERoadLanesLaneSectionLrLaneAccessRule(String v) {
        value = v;
    }

    /**
     * Gets the value associated to the enum constant.
     * 
     * @return
     *     The value linked to the enum.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum associated to the value passed as parameter.
     * 
     * @param v
     *     The value to get the enum from.
     * @return
     *     The enum which corresponds to the value, if it exists.
     * @throws IllegalArgumentException
     *     If no value matches in the enum declaration.
     */
    public static ERoadLanesLaneSectionLrLaneAccessRule fromValue(String v) {
        for (ERoadLanesLaneSectionLrLaneAccessRule c: ERoadLanesLaneSectionLrLaneAccessRule.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
