
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_roadMarkRule</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_roadMarkRule">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="no passing"/>
 *     <enumeration value="caution"/>
 *     <enumeration value="none"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_roadMarkRule")
@XmlEnum
@SuppressWarnings("all") public enum ERoadMarkRule {

    @XmlEnumValue("no passing")
    NO_PASSING("no passing"),
    @XmlEnumValue("caution")
    CAUTION("caution"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    ERoadMarkRule(String v) {
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
    public static ERoadMarkRule fromValue(String v) {
        for (ERoadMarkRule c: ERoadMarkRule.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
