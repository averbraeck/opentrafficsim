
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_dataQuality_RawData_PostProcessing</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_dataQuality_RawData_PostProcessing">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="raw"/>
 *     <enumeration value="cleaned"/>
 *     <enumeration value="processed"/>
 *     <enumeration value="fused"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_dataQuality_RawData_PostProcessing")
@XmlEnum
@SuppressWarnings("all") public enum EDataQualityRawDataPostProcessing {

    @XmlEnumValue("raw")
    RAW("raw"),
    @XmlEnumValue("cleaned")
    CLEANED("cleaned"),
    @XmlEnumValue("processed")
    PROCESSED("processed"),
    @XmlEnumValue("fused")
    FUSED("fused");
    private final String value;

    EDataQualityRawDataPostProcessing(String v) {
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
    public static EDataQualityRawDataPostProcessing fromValue(String v) {
        for (EDataQualityRawDataPostProcessing c: EDataQualityRawDataPostProcessing.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
