
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_tunnelType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_tunnelType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="standard"/>
 *     <enumeration value="underpass"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_tunnelType")
@XmlEnum
@SuppressWarnings("all") public enum ETunnelType {

    @XmlEnumValue("standard")
    STANDARD("standard"),

    /**
     * i.e. sides are open for daylight
     * 
     */
    @XmlEnumValue("underpass")
    UNDERPASS("underpass");
    private final String value;

    ETunnelType(String v) {
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
    public static ETunnelType fromValue(String v) {
        for (ETunnelType c: ETunnelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
