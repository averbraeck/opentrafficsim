
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.StripeLateralSyncAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.StripeLateralSyncType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Elements" type="{http://www.opentrafficsim.org/ots}StripeElements"/>
 *         <element name="DashOffset" type="{http://www.opentrafficsim.org/ots}DashOffset" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="LeftChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="RightChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Default" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "elements",
    "dashOffset"
})
@XmlRootElement(name = "StripeType")
@SuppressWarnings("all") public class StripeType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Elements", required = true)
    protected StripeElements elements;
    @XmlElement(name = "DashOffset")
    protected DashOffset dashOffset;
    @XmlAttribute(name = "LeftChangeLane", required = true)
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType leftChangeLane;
    @XmlAttribute(name = "RightChangeLane", required = true)
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType rightChangeLane;
    @XmlAttribute(name = "LateralSync")
    @XmlJavaTypeAdapter(StripeLateralSyncAdapter.class)
    protected StripeLateralSyncType lateralSync;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Default")
    protected Boolean _default;

    /**
     * Ruft den Wert der elements-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StripeElements }
     *     
     */
    public StripeElements getElements() {
        return elements;
    }

    /**
     * Legt den Wert der elements-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StripeElements }
     *     
     */
    public void setElements(StripeElements value) {
        this.elements = value;
    }

    /**
     * Ruft den Wert der dashOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DashOffset }
     *     
     */
    public DashOffset getDashOffset() {
        return dashOffset;
    }

    /**
     * Legt den Wert der dashOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DashOffset }
     *     
     */
    public void setDashOffset(DashOffset value) {
        this.dashOffset = value;
    }

    /**
     * Ruft den Wert der leftChangeLane-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getLeftChangeLane() {
        return leftChangeLane;
    }

    /**
     * Legt den Wert der leftChangeLane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftChangeLane(BooleanType value) {
        this.leftChangeLane = value;
    }

    /**
     * Ruft den Wert der rightChangeLane-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getRightChangeLane() {
        return rightChangeLane;
    }

    /**
     * Legt den Wert der rightChangeLane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightChangeLane(BooleanType value) {
        this.rightChangeLane = value;
    }

    /**
     * Ruft den Wert der lateralSync-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StripeLateralSyncType getLateralSync() {
        return lateralSync;
    }

    /**
     * Legt den Wert der lateralSync-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLateralSync(StripeLateralSyncType value) {
        this.lateralSync = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
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
     * Legt den Wert der id-Eigenschaft fest.
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
     * Ruft den Wert der default-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefault() {
        if (_default == null) {
            return false;
        } else {
            return _default;
        }
    }

    /**
     * Legt den Wert der default-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

}
