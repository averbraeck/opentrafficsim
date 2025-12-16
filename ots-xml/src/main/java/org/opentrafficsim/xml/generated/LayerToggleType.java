
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for LayerToggleType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LayerToggleType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Toggle" use="required">
 *         <simpleType>
 *           <restriction base="{http://www.opentrafficsim.org/ots}string">
 *             <enumeration value="ON"/>
 *             <enumeration value="OFF"/>
 *             <enumeration value="INVISIBLE"/>
 *           </restriction>
 *         </simpleType>
 *       </attribute>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LayerToggleType")
@SuppressWarnings("all") public class LayerToggleType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Toggle", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType toggle;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the toggle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getToggle() {
        return toggle;
    }

    /**
     * Sets the value of the toggle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToggle(StringType value) {
        this.toggle = value;
    }

}
