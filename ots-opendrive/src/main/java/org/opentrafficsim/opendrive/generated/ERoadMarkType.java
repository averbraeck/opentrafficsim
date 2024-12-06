
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The known keywords for the simplified road mark type information are:
 * 
 * <p>Java class for e_roadMarkType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_roadMarkType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="none"/>
 *     <enumeration value="solid"/>
 *     <enumeration value="broken"/>
 *     <enumeration value="solid solid"/>
 *     <enumeration value="solid broken"/>
 *     <enumeration value="broken solid"/>
 *     <enumeration value="broken broken"/>
 *     <enumeration value="botts dots"/>
 *     <enumeration value="grass"/>
 *     <enumeration value="curb"/>
 *     <enumeration value="custom"/>
 *     <enumeration value="edge"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_roadMarkType")
@XmlEnum
@SuppressWarnings("all") public enum ERoadMarkType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("solid")
    SOLID("solid"),
    @XmlEnumValue("broken")
    BROKEN("broken"),

    /**
     * for double solid line
     * 
     */
    @XmlEnumValue("solid solid")
    SOLID_SOLID("solid solid"),

    /**
     * from inside to outside, exception: center lane – from left to right
     * 
     */
    @XmlEnumValue("solid broken")
    SOLID_BROKEN("solid broken"),

    /**
     * from inside to outside, exception: center lane – from left to right
     * 
     */
    @XmlEnumValue("broken solid")
    BROKEN_SOLID("broken solid"),

    /**
     * from inside to outside, exception: center lane – from left to right
     * 
     */
    @XmlEnumValue("broken broken")
    BROKEN_BROKEN("broken broken"),
    @XmlEnumValue("botts dots")
    BOTTS_DOTS("botts dots"),

    /**
     * meaning a grass edge
     * 
     */
    @XmlEnumValue("grass")
    GRASS("grass"),
    @XmlEnumValue("curb")
    CURB("curb"),

    /**
     * if detailed description is given in child tags
     * 
     */
    @XmlEnumValue("custom")
    CUSTOM("custom"),

    /**
     * describing the limit of usable space on a road
     * 
     */
    @XmlEnumValue("edge")
    EDGE("edge");
    private final String value;

    ERoadMarkType(String v) {
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
    public static ERoadMarkType fromValue(String v) {
        for (ERoadMarkType c: ERoadMarkType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
