
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_roadMarkWeight</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_roadMarkWeight">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="standard"/>
 *     <enumeration value="bold"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_roadMarkWeight")
@XmlEnum
@SuppressWarnings("all") public enum ERoadMarkWeight {

    @XmlEnumValue("standard")
    STANDARD("standard"),
    @XmlEnumValue("bold")
    BOLD("bold");
    private final String value;

    ERoadMarkWeight(String v) {
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
    public static ERoadMarkWeight fromValue(String v) {
        for (ERoadMarkWeight c: ERoadMarkWeight.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
