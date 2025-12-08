
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="LegalSpeedLimit" use="required" type="{http://www.opentrafficsim.org/ots}SpeedType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "SpeedLimit")
@SuppressWarnings("all") public class SpeedLimit
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;
    @XmlAttribute(name = "LegalSpeedLimit", required = true)
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    protected SpeedType legalSpeedLimit;

    /**
     * Gets the value of the gtuType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getGtuType() {
        return gtuType;
    }

    /**
     * Sets the value of the gtuType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGtuType(StringType value) {
        this.gtuType = value;
    }

    /**
     * Gets the value of the legalSpeedLimit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedType getLegalSpeedLimit() {
        return legalSpeedLimit;
    }

    /**
     * Sets the value of the legalSpeedLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLegalSpeedLimit(SpeedType value) {
        this.legalSpeedLimit = value;
    }

}
