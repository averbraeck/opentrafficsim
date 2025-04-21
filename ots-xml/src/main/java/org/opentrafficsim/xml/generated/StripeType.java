
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
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the elements property.
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
     * Sets the value of the elements property.
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
     * Gets the value of the dashOffset property.
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
     * Sets the value of the dashOffset property.
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
     * Gets the value of the leftChangeLane property.
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
     * Sets the value of the leftChangeLane property.
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
     * Gets the value of the rightChangeLane property.
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
     * Sets the value of the rightChangeLane property.
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
     * Gets the value of the lateralSync property.
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
     * Sets the value of the lateralSync property.
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
     * Gets the value of the default property.
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
     * Sets the value of the default property.
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
