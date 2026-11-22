
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.StripeLateralSyncAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.StripeLateralSyncType;


/**
 * <p>Java class for StripeCustom complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="StripeCustom">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Elements" type="{http://www.opentrafficsim.org/ots}StripeElements" minOccurs="0"/>
 *         <element name="DashOffset" type="{http://www.opentrafficsim.org/ots}DashOffset" minOccurs="0"/>
 *         <element name="Compatibility" type="{http://www.opentrafficsim.org/ots}StripeCompatibility" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="LeftChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="RightChangeLane" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="Block" type="{http://www.opentrafficsim.org/ots}boolean" />
 *       <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StripeCustom", propOrder = {
    "elements",
    "dashOffset",
    "compatibility"
})
@SuppressWarnings("all") public class StripeCustom
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Elements")
    protected StripeElements elements;
    @XmlElement(name = "DashOffset")
    protected DashOffset dashOffset;
    @XmlElement(name = "Compatibility")
    protected List<StripeCompatibility> compatibility;
    @XmlAttribute(name = "LeftChangeLane")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType leftChangeLane;
    @XmlAttribute(name = "RightChangeLane")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType rightChangeLane;
    @XmlAttribute(name = "Block")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType block;
    @XmlAttribute(name = "LateralSync")
    @XmlJavaTypeAdapter(StripeLateralSyncAdapter.class)
    protected StripeLateralSyncType lateralSync;

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
     * Gets the value of the compatibility property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatibility property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCompatibility().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StripeCompatibility }
     * </p>
     * 
     * 
     * @return
     *     The value of the compatibility property.
     */
    public List<StripeCompatibility> getCompatibility() {
        if (compatibility == null) {
            compatibility = new ArrayList<>();
        }
        return this.compatibility;
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
     * Gets the value of the block property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getBlock() {
        return block;
    }

    /**
     * Sets the value of the block property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBlock(BooleanType value) {
        this.block = value;
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

}
