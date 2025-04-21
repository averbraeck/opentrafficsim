
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_unitSpeed</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_unitSpeed">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="m/s"/>
 *     <enumeration value="mph"/>
 *     <enumeration value="km/h"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_unitSpeed")
@XmlEnum
@SuppressWarnings("all") public enum EUnitSpeed {

    @XmlEnumValue("m/s")
    M_S("m/s"),
    @XmlEnumValue("mph")
    MPH("mph"),
    @XmlEnumValue("km/h")
    KM_H("km/h");
    private final String value;

    EUnitSpeed(String v) {
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
    public static EUnitSpeed fromValue(String v) {
        for (EUnitSpeed c: EUnitSpeed.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
