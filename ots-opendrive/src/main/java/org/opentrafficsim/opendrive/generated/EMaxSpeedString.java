
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_maxSpeedString</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_maxSpeedString">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="no limit"/>
 *     <enumeration value="undefined"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_maxSpeedString")
@XmlEnum
@SuppressWarnings("all") public enum EMaxSpeedString {

    @XmlEnumValue("no limit")
    NO_LIMIT("no limit"),
    @XmlEnumValue("undefined")
    UNDEFINED("undefined");
    private final String value;

    EMaxSpeedString(String v) {
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
    public static EMaxSpeedString fromValue(String v) {
        for (EMaxSpeedString c: EMaxSpeedString.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
