
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for LaneLinkType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LaneLinkType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Lane" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Link" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LaneLinkType")
@SuppressWarnings("all") public class LaneLinkType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Lane")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType lane;
    @XmlAttribute(name = "Link")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType link;

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLane() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLane(StringType value) {
        this.lane = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(StringType value) {
        this.link = value;
    }

}
