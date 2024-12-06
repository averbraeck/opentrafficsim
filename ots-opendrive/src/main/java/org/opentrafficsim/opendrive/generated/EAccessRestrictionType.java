
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_accessRestrictionType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_accessRestrictionType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="simulator"/>
 *     <enumeration value="autonomousTraffic"/>
 *     <enumeration value="pedestrian"/>
 *     <enumeration value="passengerCar"/>
 *     <enumeration value="bus"/>
 *     <enumeration value="delivery"/>
 *     <enumeration value="emergency"/>
 *     <enumeration value="taxi"/>
 *     <enumeration value="throughTraffic"/>
 *     <enumeration value="truck"/>
 *     <enumeration value="bicycle"/>
 *     <enumeration value="motorcycle"/>
 *     <enumeration value="none"/>
 *     <enumeration value="trucks"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_accessRestrictionType")
@XmlEnum
@SuppressWarnings("all") public enum EAccessRestrictionType {

    @XmlEnumValue("simulator")
    SIMULATOR("simulator"),
    @XmlEnumValue("autonomousTraffic")
    AUTONOMOUS_TRAFFIC("autonomousTraffic"),
    @XmlEnumValue("pedestrian")
    PEDESTRIAN("pedestrian"),
    @XmlEnumValue("passengerCar")
    PASSENGER_CAR("passengerCar"),
    @XmlEnumValue("bus")
    BUS("bus"),
    @XmlEnumValue("delivery")
    DELIVERY("delivery"),
    @XmlEnumValue("emergency")
    EMERGENCY("emergency"),
    @XmlEnumValue("taxi")
    TAXI("taxi"),
    @XmlEnumValue("throughTraffic")
    THROUGH_TRAFFIC("throughTraffic"),
    @XmlEnumValue("truck")
    TRUCK("truck"),
    @XmlEnumValue("bicycle")
    BICYCLE("bicycle"),
    @XmlEnumValue("motorcycle")
    MOTORCYCLE("motorcycle"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("trucks")
    TRUCKS("trucks");
    private final String value;

    EAccessRestrictionType(String v) {
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
    public static EAccessRestrictionType fromValue(String v) {
        for (EAccessRestrictionType c: EAccessRestrictionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
