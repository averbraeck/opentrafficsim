
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Some basic metadata containing information about raw data included in OpenDRIVE is described by the &lt;rawData&gt; element within the &lt;dataQuality&gt; element.
 * 
 * <p>Java class for t_dataQuality_RawData complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_dataQuality_RawData">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *       </sequence>
 *       <attribute name="date" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="source" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_dataQuality_RawData_Source" />
 *       <attribute name="sourceComment" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="postProcessing" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_dataQuality_RawData_PostProcessing" />
 *       <attribute name="postProcessingComment" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_dataQuality_RawData")
@SuppressWarnings("all") public class TDataQualityRawData {

    /**
     * Date of the delivery of raw data, to be given in ISO 8601 notification (YYYY-MM-DDTHH:MM:SS) [9]. Time-of-day may be omitted
     * 
     */
    @XmlAttribute(name = "date", required = true)
    protected String date;
    /**
     * Source that has been used for retrieving the raw data; further sources to be added in upcoming versions. For values see UML Model
     * 
     */
    @XmlAttribute(name = "source", required = true)
    protected EDataQualityRawDataSource source;
    /**
     * Comments concerning the @source . Free text, depending on the application
     * 
     */
    @XmlAttribute(name = "sourceComment")
    protected String sourceComment;
    /**
     * Information about the kind of data handling before exporting data into the ASAM OpenDRIVE file. For values see UML Model
     * 
     */
    @XmlAttribute(name = "postProcessing", required = true)
    protected EDataQualityRawDataPostProcessing postProcessing;
    /**
     * Comments concerning the postprocessing attribute. Free text, depending on the application
     * 
     */
    @XmlAttribute(name = "postProcessingComment")
    protected String postProcessingComment;

    /**
     * Date of the delivery of raw data, to be given in ISO 8601 notification (YYYY-MM-DDTHH:MM:SS) [9]. Time-of-day may be omitted
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getDate()
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Source that has been used for retrieving the raw data; further sources to be added in upcoming versions. For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link EDataQualityRawDataSource }
     *     
     */
    public EDataQualityRawDataSource getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDataQualityRawDataSource }
     *     
     * @see #getSource()
     */
    public void setSource(EDataQualityRawDataSource value) {
        this.source = value;
    }

    /**
     * Comments concerning the @source . Free text, depending on the application
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceComment() {
        return sourceComment;
    }

    /**
     * Sets the value of the sourceComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSourceComment()
     */
    public void setSourceComment(String value) {
        this.sourceComment = value;
    }

    /**
     * Information about the kind of data handling before exporting data into the ASAM OpenDRIVE file. For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link EDataQualityRawDataPostProcessing }
     *     
     */
    public EDataQualityRawDataPostProcessing getPostProcessing() {
        return postProcessing;
    }

    /**
     * Sets the value of the postProcessing property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDataQualityRawDataPostProcessing }
     *     
     * @see #getPostProcessing()
     */
    public void setPostProcessing(EDataQualityRawDataPostProcessing value) {
        this.postProcessing = value;
    }

    /**
     * Comments concerning the postprocessing attribute. Free text, depending on the application
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostProcessingComment() {
        return postProcessingComment;
    }

    /**
     * Sets the value of the postProcessingComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getPostProcessingComment()
     */
    public void setPostProcessingComment(String value) {
        this.postProcessingComment = value;
    }

}
