
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Raw data or data from external sources that is integrated in OpenDRIVE may be of varying quality. It is possible to describe quality and accuracy of external data in OpenDRIVE.
 * The description of the data quality is represented by <dataQuality> elements. They may be stored at any position in OpenDRIVE.
 * Measurement data derived from external sources like GPS that is integrated in OpenDRIVE may be inaccurate. The error range, given in [m], may be listed in the application.
 * 
 * <p>Java-Klasse für t_dataQuality complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_dataQuality">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="error" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_dataQuality_Error" minOccurs="0"/>
 *         <element name="rawData" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_dataQuality_RawData" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_dataQuality", propOrder = {
    "error",
    "rawData"
})
@SuppressWarnings("all") public class TDataQuality {

    protected TDataQualityError error;
    protected TDataQualityRawData rawData;

    /**
     * Ruft den Wert der error-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TDataQualityError }
     *     
     */
    public TDataQualityError getError() {
        return error;
    }

    /**
     * Legt den Wert der error-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TDataQualityError }
     *     
     */
    public void setError(TDataQualityError value) {
        this.error = value;
    }

    /**
     * Ruft den Wert der rawData-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TDataQualityRawData }
     *     
     */
    public TDataQualityRawData getRawData() {
        return rawData;
    }

    /**
     * Legt den Wert der rawData-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TDataQualityRawData }
     *     
     */
    public void setRawData(TDataQualityRawData value) {
        this.rawData = value;
    }

}
