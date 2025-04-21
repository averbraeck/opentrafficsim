
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_road_surface_CRG_mode</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_road_surface_CRG_mode">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="attached"/>
 *     <enumeration value="attached0"/>
 *     <enumeration value="genuine"/>
 *     <enumeration value="global"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_road_surface_CRG_mode")
@XmlEnum
@SuppressWarnings("all") public enum ERoadSurfaceCrgMode {

    @XmlEnumValue("attached")
    ATTACHED("attached"),
    @XmlEnumValue("attached0")
    ATTACHED_0("attached0"),
    @XmlEnumValue("genuine")
    GENUINE("genuine"),
    @XmlEnumValue("global")
    GLOBAL("global");
    private final String value;

    ERoadSurfaceCrgMode(String v) {
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
    public static ERoadSurfaceCrgMode fromValue(String v) {
        for (ERoadSurfaceCrgMode c: ERoadSurfaceCrgMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
