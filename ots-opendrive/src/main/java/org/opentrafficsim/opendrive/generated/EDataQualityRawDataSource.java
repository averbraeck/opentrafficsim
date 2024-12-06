
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_dataQuality_RawData_Source</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_dataQuality_RawData_Source">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="sensor"/>
 *     <enumeration value="cadaster"/>
 *     <enumeration value="custom"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_dataQuality_RawData_Source")
@XmlEnum
@SuppressWarnings("all") public enum EDataQualityRawDataSource {

    @XmlEnumValue("sensor")
    SENSOR("sensor"),
    @XmlEnumValue("cadaster")
    CADASTER("cadaster"),
    @XmlEnumValue("custom")
    CUSTOM("custom");
    private final String value;

    EDataQualityRawDataSource(String v) {
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
    public static EDataQualityRawDataSource fromValue(String v) {
        for (EDataQualityRawDataSource c: EDataQualityRawDataSource.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
