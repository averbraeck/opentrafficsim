
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
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.types.ColorType;


/**
 * Laterally adjacent stripe elements, e.g. Continuous-Gap-Continuous for a double line.
 * 
 * <p>Java class for StripeElements complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="StripeElements">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice maxOccurs="unbounded">
 *         <element name="Continuous">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                 <attribute name="Color" type="{http://www.opentrafficsim.org/ots}ColorType" default="WHITE" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Dashed">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="GapDash" maxOccurs="unbounded">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                             <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *                 <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                 <attribute name="Color" type="{http://www.opentrafficsim.org/ots}ColorType" default="WHITE" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Gap">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StripeElements", propOrder = {
    "continuousOrDashedOrGap"
})
@SuppressWarnings("all") public class StripeElements
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Continuous", type = StripeElements.Continuous.class),
        @XmlElement(name = "Dashed", type = StripeElements.Dashed.class),
        @XmlElement(name = "Gap", type = StripeElements.Gap.class)
    })
    protected List<Serializable> continuousOrDashedOrGap;

    /**
     * Gets the value of the continuousOrDashedOrGap property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the continuousOrDashedOrGap property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getContinuousOrDashedOrGap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StripeElements.Continuous }
     * {@link StripeElements.Dashed }
     * {@link StripeElements.Gap }
     * </p>
     * 
     * 
     * @return
     *     The value of the continuousOrDashedOrGap property.
     */
    public List<Serializable> getContinuousOrDashedOrGap() {
        if (continuousOrDashedOrGap == null) {
            continuousOrDashedOrGap = new ArrayList<>();
        }
        return this.continuousOrDashedOrGap;
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
     *       <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *       <attribute name="Color" type="{http://www.opentrafficsim.org/ots}ColorType" default="WHITE" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Continuous implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Width", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType width;
        @XmlAttribute(name = "Color")
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.LengthType getWidth() {
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
        public void setWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.width = value;
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
            if (color == null) {
                return new ColorAdapter().unmarshal("WHITE");
            } else {
                return color;
            }
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
     *       <sequence>
     *         <element name="GapDash" maxOccurs="unbounded">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                   <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                 </sequence>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </sequence>
     *       <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *       <attribute name="Color" type="{http://www.opentrafficsim.org/ots}ColorType" default="WHITE" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "gapDash"
    })
    public static class Dashed implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "GapDash", required = true)
        protected List<StripeElements.Dashed.GapDash> gapDash;
        @XmlAttribute(name = "Width", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType width;
        @XmlAttribute(name = "Color")
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Gets the value of the gapDash property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the gapDash property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getGapDash().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StripeElements.Dashed.GapDash }
         * </p>
         * 
         * 
         * @return
         *     The value of the gapDash property.
         */
        public List<StripeElements.Dashed.GapDash> getGapDash() {
            if (gapDash == null) {
                gapDash = new ArrayList<>();
            }
            return this.gapDash;
        }

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.LengthType getWidth() {
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
        public void setWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.width = value;
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
            if (color == null) {
                return new ColorAdapter().unmarshal("WHITE");
            } else {
                return color;
            }
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
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <complexContent>
         *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       <sequence>
         *         <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *         <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *       </sequence>
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "gap",
            "dash"
        })
        public static class GapDash
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "Gap", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.LengthType gap;
            @XmlElement(name = "Dash", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthAdapter.class)
            protected org.opentrafficsim.xml.bindings.types.LengthType dash;

            /**
             * Gets the value of the gap property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.LengthType getGap() {
                return gap;
            }

            /**
             * Sets the value of the gap property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setGap(org.opentrafficsim.xml.bindings.types.LengthType value) {
                this.gap = value;
            }

            /**
             * Gets the value of the dash property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public org.opentrafficsim.xml.bindings.types.LengthType getDash() {
                return dash;
            }

            /**
             * Sets the value of the dash property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDash(org.opentrafficsim.xml.bindings.types.LengthType value) {
                this.dash = value;
            }

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
     *       <attribute name="Width" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Gap implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Width", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType width;

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.LengthType getWidth() {
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
        public void setWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
            this.width = value;
        }

    }

}
