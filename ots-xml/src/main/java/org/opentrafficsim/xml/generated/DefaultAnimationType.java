
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ColorAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.types.ColorType;
import org.opentrafficsim.xml.bindings.types.LengthType;


/**
 * <p>Java class for DefaultAnimationType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="DefaultAnimationType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Link" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *                 <attribute name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Lane" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Stripe" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Shoulder" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="NoTrafficLane" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefaultAnimationType", propOrder = {
    "link",
    "lane",
    "stripe",
    "shoulder",
    "noTrafficLane"
})
@SuppressWarnings("all") public class DefaultAnimationType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Link")
    protected DefaultAnimationType.Link link;
    @XmlElement(name = "Lane")
    protected DefaultAnimationType.Lane lane;
    @XmlElement(name = "Stripe")
    protected DefaultAnimationType.Stripe stripe;
    @XmlElement(name = "Shoulder")
    protected DefaultAnimationType.Shoulder shoulder;
    @XmlElement(name = "NoTrafficLane")
    protected DefaultAnimationType.NoTrafficLane noTrafficLane;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType.Link }
     *     
     */
    public DefaultAnimationType.Link getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType.Link }
     *     
     */
    public void setLink(DefaultAnimationType.Link value) {
        this.link = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType.Lane }
     *     
     */
    public DefaultAnimationType.Lane getLane() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType.Lane }
     *     
     */
    public void setLane(DefaultAnimationType.Lane value) {
        this.lane = value;
    }

    /**
     * Gets the value of the stripe property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType.Stripe }
     *     
     */
    public DefaultAnimationType.Stripe getStripe() {
        return stripe;
    }

    /**
     * Sets the value of the stripe property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType.Stripe }
     *     
     */
    public void setStripe(DefaultAnimationType.Stripe value) {
        this.stripe = value;
    }

    /**
     * Gets the value of the shoulder property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType.Shoulder }
     *     
     */
    public DefaultAnimationType.Shoulder getShoulder() {
        return shoulder;
    }

    /**
     * Sets the value of the shoulder property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType.Shoulder }
     *     
     */
    public void setShoulder(DefaultAnimationType.Shoulder value) {
        this.shoulder = value;
    }

    /**
     * Gets the value of the noTrafficLane property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType.NoTrafficLane }
     *     
     */
    public DefaultAnimationType.NoTrafficLane getNoTrafficLane() {
        return noTrafficLane;
    }

    /**
     * Sets the value of the noTrafficLane property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType.NoTrafficLane }
     *     
     */
    public void setNoTrafficLane(DefaultAnimationType.NoTrafficLane value) {
        this.noTrafficLane = value;
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
    public static class Lane
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

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
     *       <attribute name="Color" use="required" type="{http://www.opentrafficsim.org/ots}ColorType" />
     *       <attribute name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Link
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;
        @XmlAttribute(name = "Width")
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected LengthType width;

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

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LengthType getWidth() {
            return width;
        }

        /**
         * Sets the value of the width property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWidth(LengthType value) {
            this.width = value;
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
    public static class NoTrafficLane
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

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
    public static class Shoulder
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

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
    public static class Stripe
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Color", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

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
