
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_contactPoint</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_contactPoint">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="start"/>
 *     <enumeration value="end"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_contactPoint")
@XmlEnum
@SuppressWarnings("all") public enum EContactPoint {

    @XmlEnumValue("start")
    START("start"),
    @XmlEnumValue("end")
    END("end");
    private final String value;

    EContactPoint(String v) {
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
    public static EContactPoint fromValue(String v) {
        for (EContactPoint c: EContactPoint.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
