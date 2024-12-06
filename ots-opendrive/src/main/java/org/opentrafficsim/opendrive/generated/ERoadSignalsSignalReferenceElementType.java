
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_road_signals_signal_reference_elementType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_road_signals_signal_reference_elementType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="object"/>
 *     <enumeration value="signal"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_road_signals_signal_reference_elementType")
@XmlEnum
@SuppressWarnings("all") public enum ERoadSignalsSignalReferenceElementType {

    @XmlEnumValue("object")
    OBJECT("object"),
    @XmlEnumValue("signal")
    SIGNAL("signal");
    private final String value;

    ERoadSignalsSignalReferenceElementType(String v) {
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
    public static ERoadSignalsSignalReferenceElementType fromValue(String v) {
        for (ERoadSignalsSignalReferenceElementType c: ERoadSignalsSignalReferenceElementType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
