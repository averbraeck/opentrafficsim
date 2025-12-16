
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.StringType;


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
 *       <attribute name="Compatible" type="{http://www.opentrafficsim.org/ots}boolean" default="true" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Compatibility")
@SuppressWarnings("all") public class Compatibility
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;
    /**
     * When false, the GTU type is explicitly subtracted from the allowed GTU
     *             types.
     * 
     */
    @XmlAttribute(name = "Compatible")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType compatible;

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
     * When false, the GTU type is explicitly subtracted from the allowed GTU
     *             types.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getCompatible() {
        if (compatible == null) {
            return new BooleanAdapter().unmarshal("true");
        } else {
            return compatible;
        }
    }

    /**
     * Sets the value of the compatible property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCompatible()
     */
    public void setCompatible(BooleanType value) {
        this.compatible = value;
    }

}
