
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ColorAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.ColorType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for LinkAnimationType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LinkAnimationType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConnectorAnimationType">
 *       <choice maxOccurs="unbounded">
 *         <element name="Stripe">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Lane">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Shoulder">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="NoTrafficLane">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </choice>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinkAnimationType", propOrder = {
    "stripeOrLaneOrShoulder"
})
@SuppressWarnings("all") public class LinkAnimationType
    extends ConnectorAnimationType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Stripe", type = LinkAnimationType.Stripe.class),
        @XmlElement(name = "Lane", type = LinkAnimationType.Lane.class),
        @XmlElement(name = "Shoulder", type = LinkAnimationType.Shoulder.class),
        @XmlElement(name = "NoTrafficLane", type = LinkAnimationType.NoTrafficLane.class)
    })
    protected List<Serializable> stripeOrLaneOrShoulder;

    /**
     * Gets the value of the stripeOrLaneOrShoulder property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stripeOrLaneOrShoulder property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getStripeOrLaneOrShoulder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkAnimationType.Lane }
     * {@link LinkAnimationType.NoTrafficLane }
     * {@link LinkAnimationType.Shoulder }
     * {@link LinkAnimationType.Stripe }
     * </p>
     * 
     * 
     * @return
     *     The value of the stripeOrLaneOrShoulder property.
     */
    public List<Serializable> getStripeOrLaneOrShoulder() {
        if (stripeOrLaneOrShoulder == null) {
            stripeOrLaneOrShoulder = new ArrayList<>();
        }
        return this.stripeOrLaneOrShoulder;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Lane implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
            this.id = value;
        }

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public ColorType getColor() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColor(ColorType value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NoTrafficLane implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
            this.id = value;
        }

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public ColorType getColor() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColor(ColorType value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Shoulder implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
            this.id = value;
        }

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public ColorType getColor() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColor(ColorType value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Stripe implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
            this.id = value;
        }

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public ColorType getColor() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColor(ColorType value) {
            this.color = value;
        }

    }

}
