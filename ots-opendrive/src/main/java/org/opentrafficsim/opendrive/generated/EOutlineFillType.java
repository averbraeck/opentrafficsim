
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_outlineFillType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_outlineFillType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="grass"/>
 *     <enumeration value="concrete"/>
 *     <enumeration value="cobble"/>
 *     <enumeration value="asphalt"/>
 *     <enumeration value="pavement"/>
 *     <enumeration value="gravel"/>
 *     <enumeration value="soil"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_outlineFillType")
@XmlEnum
@SuppressWarnings("all") public enum EOutlineFillType {

    @XmlEnumValue("grass")
    GRASS("grass"),
    @XmlEnumValue("concrete")
    CONCRETE("concrete"),
    @XmlEnumValue("cobble")
    COBBLE("cobble"),
    @XmlEnumValue("asphalt")
    ASPHALT("asphalt"),
    @XmlEnumValue("pavement")
    PAVEMENT("pavement"),
    @XmlEnumValue("gravel")
    GRAVEL("gravel"),
    @XmlEnumValue("soil")
    SOIL("soil");
    private final String value;

    EOutlineFillType(String v) {
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
    public static EOutlineFillType fromValue(String v) {
        for (EOutlineFillType c: EOutlineFillType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
