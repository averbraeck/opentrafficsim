
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
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.StripeLateralSyncAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.bindings.types.StripeLateralSyncType;


/**
 * <p>Java-Klasse für CseStripe complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="CseStripe">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice minOccurs="0">
 *           <element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *           <sequence>
 *             <element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *             <element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *           </sequence>
 *         </choice>
 *         <choice>
 *           <element name="DefinedStripe" type="{http://www.opentrafficsim.org/ots}string"/>
 *           <element name="Custom">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <sequence>
 *                     <element name="Elements" type="{http://www.opentrafficsim.org/ots}StripeElements"/>
 *                     <element name="DashOffset" type="{http://www.opentrafficsim.org/ots}DashOffset" minOccurs="0"/>
 *                     <element name="Compatibility" type="{http://www.opentrafficsim.org/ots}StripeCompatibility" maxOccurs="unbounded" minOccurs="0"/>
 *                   </sequence>
 *                   <attribute name="LeftChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
 *                   <attribute name="RightChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
 *                   <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *         </choice>
 *       </sequence>
 *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CseStripe", propOrder = {
    "centerOffset",
    "centerOffsetStart",
    "centerOffsetEnd",
    "definedStripe",
    "custom"
})
@SuppressWarnings("all") public class CseStripe implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "CenterOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected LengthType centerOffset;
    @XmlElement(name = "CenterOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected LengthType centerOffsetStart;
    @XmlElement(name = "CenterOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected LengthType centerOffsetEnd;
    @XmlElement(name = "DefinedStripe", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType definedStripe;
    @XmlElement(name = "Custom")
    protected CseStripe.Custom custom;
    @XmlAttribute(name = "Id")
    protected String id;

    /**
     * Ruft den Wert der centerOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthType getCenterOffset() {
        return centerOffset;
    }

    /**
     * Legt den Wert der centerOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffset(LengthType value) {
        this.centerOffset = value;
    }

    /**
     * Ruft den Wert der centerOffsetStart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthType getCenterOffsetStart() {
        return centerOffsetStart;
    }

    /**
     * Legt den Wert der centerOffsetStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffsetStart(LengthType value) {
        this.centerOffsetStart = value;
    }

    /**
     * Ruft den Wert der centerOffsetEnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthType getCenterOffsetEnd() {
        return centerOffsetEnd;
    }

    /**
     * Legt den Wert der centerOffsetEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffsetEnd(LengthType value) {
        this.centerOffsetEnd = value;
    }

    /**
     * Ruft den Wert der definedStripe-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDefinedStripe() {
        return definedStripe;
    }

    /**
     * Legt den Wert der definedStripe-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinedStripe(StringType value) {
        this.definedStripe = value;
    }

    /**
     * Ruft den Wert der custom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CseStripe.Custom }
     *     
     */
    public CseStripe.Custom getCustom() {
        return custom;
    }

    /**
     * Legt den Wert der custom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CseStripe.Custom }
     *     
     */
    public void setCustom(CseStripe.Custom value) {
        this.custom = value;
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
     *         <element name="Compatibility" type="{http://www.opentrafficsim.org/ots}StripeCompatibility" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="LeftChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
     *       <attribute name="RightChangeLane" use="required" type="{http://www.opentrafficsim.org/ots}boolean" />
     *       <attribute name="LateralSync" type="{http://www.opentrafficsim.org/ots}LateralSync" />
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
        "dashOffset",
        "compatibility"
    })
    public static class Custom
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Elements", required = true)
        protected StripeElements elements;
        @XmlElement(name = "DashOffset")
        protected DashOffset dashOffset;
        @XmlElement(name = "Compatibility")
        protected List<StripeCompatibility> compatibility;
        @XmlAttribute(name = "LeftChangeLane", required = true)
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType leftChangeLane;
        @XmlAttribute(name = "RightChangeLane", required = true)
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType rightChangeLane;
        @XmlAttribute(name = "LateralSync")
        @XmlJavaTypeAdapter(StripeLateralSyncAdapter.class)
        protected StripeLateralSyncType lateralSync;

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

    }

}
