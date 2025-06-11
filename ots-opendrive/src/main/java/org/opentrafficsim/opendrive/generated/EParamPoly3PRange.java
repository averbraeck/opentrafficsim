
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java-Klasse für e_paramPoly3_pRange.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * <pre>{@code
 * <simpleType name="e_paramPoly3_pRange">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="arcLength"/>
 *     <enumeration value="normalized"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_paramPoly3_pRange")
@XmlEnum
@SuppressWarnings("all") public enum EParamPoly3PRange {

    @XmlEnumValue("arcLength")
    ARC_LENGTH("arcLength"),
    @XmlEnumValue("normalized")
    NORMALIZED("normalized");
    private final String value;

    EParamPoly3PRange(String v) {
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
    public static EParamPoly3PRange fromValue(String v) {
        for (EParamPoly3PRange c: EParamPoly3PRange.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
