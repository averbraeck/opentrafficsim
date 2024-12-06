
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_road_objects_object_parkingSpace_access</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_road_objects_object_parkingSpace_access">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="all"/>
 *     <enumeration value="car"/>
 *     <enumeration value="women"/>
 *     <enumeration value="handicapped"/>
 *     <enumeration value="bus"/>
 *     <enumeration value="truck"/>
 *     <enumeration value="electric"/>
 *     <enumeration value="residents"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_road_objects_object_parkingSpace_access")
@XmlEnum
@SuppressWarnings("all") public enum ERoadObjectsObjectParkingSpaceAccess {

    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("car")
    CAR("car"),
    @XmlEnumValue("women")
    WOMEN("women"),
    @XmlEnumValue("handicapped")
    HANDICAPPED("handicapped"),
    @XmlEnumValue("bus")
    BUS("bus"),
    @XmlEnumValue("truck")
    TRUCK("truck"),
    @XmlEnumValue("electric")
    ELECTRIC("electric"),
    @XmlEnumValue("residents")
    RESIDENTS("residents");
    private final String value;

    ERoadObjectsObjectParkingSpaceAccess(String v) {
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
    public static ERoadObjectsObjectParkingSpaceAccess fromValue(String v) {
        for (ERoadObjectsObjectParkingSpaceAccess c: ERoadObjectsObjectParkingSpaceAccess.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
