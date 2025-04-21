
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The known keywords for the road type information are:
 * 
 * <p>Java class for e_roadType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_roadType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="unknown"/>
 *     <enumeration value="rural"/>
 *     <enumeration value="motorway"/>
 *     <enumeration value="town"/>
 *     <enumeration value="lowSpeed"/>
 *     <enumeration value="pedestrian"/>
 *     <enumeration value="bicycle"/>
 *     <enumeration value="townExpressway"/>
 *     <enumeration value="townCollector"/>
 *     <enumeration value="townArterial"/>
 *     <enumeration value="townPrivate"/>
 *     <enumeration value="townLocal"/>
 *     <enumeration value="townPlayStreet"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_roadType")
@XmlEnum
@SuppressWarnings("all") public enum ERoadType {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("rural")
    RURAL("rural"),
    @XmlEnumValue("motorway")
    MOTORWAY("motorway"),
    @XmlEnumValue("town")
    TOWN("town"),

    /**
     * In Germany, lowSpeed is equivalent to a 30km/h zone
     * 
     */
    @XmlEnumValue("lowSpeed")
    LOW_SPEED("lowSpeed"),
    @XmlEnumValue("pedestrian")
    PEDESTRIAN("pedestrian"),
    @XmlEnumValue("bicycle")
    BICYCLE("bicycle"),
    @XmlEnumValue("townExpressway")
    TOWN_EXPRESSWAY("townExpressway"),
    @XmlEnumValue("townCollector")
    TOWN_COLLECTOR("townCollector"),
    @XmlEnumValue("townArterial")
    TOWN_ARTERIAL("townArterial"),
    @XmlEnumValue("townPrivate")
    TOWN_PRIVATE("townPrivate"),
    @XmlEnumValue("townLocal")
    TOWN_LOCAL("townLocal"),
    @XmlEnumValue("townPlayStreet")
    TOWN_PLAY_STREET("townPlayStreet");
    private final String value;

    ERoadType(String v) {
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
    public static ERoadType fromValue(String v) {
        for (ERoadType c: ERoadType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
