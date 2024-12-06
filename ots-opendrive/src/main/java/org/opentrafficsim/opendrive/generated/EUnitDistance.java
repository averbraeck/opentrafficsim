
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_unitDistance</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_unitDistance">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="m"/>
 *     <enumeration value="km"/>
 *     <enumeration value="ft"/>
 *     <enumeration value="mile"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_unitDistance")
@XmlEnum
@SuppressWarnings("all") public enum EUnitDistance {

    @XmlEnumValue("m")
    M("m"),
    @XmlEnumValue("km")
    KM("km"),
    @XmlEnumValue("ft")
    FT("ft"),
    @XmlEnumValue("mile")
    MILE("mile");
    private final String value;

    EUnitDistance(String v) {
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
    public static EUnitDistance fromValue(String v) {
        for (EUnitDistance c: EUnitDistance.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
