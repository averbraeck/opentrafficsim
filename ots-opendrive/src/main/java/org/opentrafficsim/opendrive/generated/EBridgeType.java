
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_bridgeType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_bridgeType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="concrete"/>
 *     <enumeration value="steel"/>
 *     <enumeration value="brick"/>
 *     <enumeration value="wood"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_bridgeType")
@XmlEnum
@SuppressWarnings("all") public enum EBridgeType {

    @XmlEnumValue("concrete")
    CONCRETE("concrete"),
    @XmlEnumValue("steel")
    STEEL("steel"),
    @XmlEnumValue("brick")
    BRICK("brick"),
    @XmlEnumValue("wood")
    WOOD("wood");
    private final String value;

    EBridgeType(String v) {
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
    public static EBridgeType fromValue(String v) {
        for (EBridgeType c: EBridgeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
