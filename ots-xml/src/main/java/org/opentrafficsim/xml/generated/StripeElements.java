
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ColorAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.types.ColorType;


/**
 * <p>Java-Klasse für StripeElements complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="StripeElements">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice maxOccurs="unbounded">
 *         <element name="Line">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <choice>
 *                     <element name="Continuous" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                     <element name="Dashed">
 *                       <complexType>
 *                         <complexContent>
 *                           <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             <sequence>
 *                               <sequence maxOccurs="unbounded">
 *                                 <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                                 <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                               </sequence>
 *                             </sequence>
 *                           </restriction>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                   </choice>
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
    "lineOrGap"
})
@SuppressWarnings("all") public class StripeElements
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Line", type = StripeElements.Line.class),
        @XmlElement(name = "Gap", type = StripeElements.Gap.class)
    })
    protected List<Serializable> lineOrGap;

    /**
     * Gets the value of the lineOrGap property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineOrGap property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLineOrGap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StripeElements.Gap }
     * {@link StripeElements.Line }
     * </p>
     * 
     * 
     * @return
     *     The value of the lineOrGap property.
     */
    public List<Serializable> getLineOrGap() {
        if (lineOrGap == null) {
            lineOrGap = new ArrayList<>();
        }
        return this.lineOrGap;
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
         * Ruft den Wert der width-Eigenschaft ab.
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
         * Legt den Wert der width-Eigenschaft fest.
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
     *         <choice>
     *           <element name="Continuous" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *           <element name="Dashed">
     *             <complexType>
     *               <complexContent>
     *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                   <sequence>
     *                     <sequence maxOccurs="unbounded">
     *                       <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                       <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                     </sequence>
     *                   </sequence>
     *                 </restriction>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *         </choice>
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
        "continuous",
        "dashed"
    })
    public static class Line implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Continuous")
        protected EmptyType continuous;
        /**
         * Dashes per line. If the pattern involves multiple dash and gap
         *                     lengths, include additional gap/dash pairs.
         * 
         */
        @XmlElement(name = "Dashed")
        protected StripeElements.Line.Dashed dashed;
        @XmlAttribute(name = "Width", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.LengthType width;
        @XmlAttribute(name = "Color")
        @XmlJavaTypeAdapter(ColorAdapter.class)
        protected ColorType color;

        /**
         * Ruft den Wert der continuous-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getContinuous() {
            return continuous;
        }

        /**
         * Legt den Wert der continuous-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setContinuous(EmptyType value) {
            this.continuous = value;
        }

        /**
         * Dashes per line. If the pattern involves multiple dash and gap
         *                     lengths, include additional gap/dash pairs.
         * 
         * @return
         *     possible object is
         *     {@link StripeElements.Line.Dashed }
         *     
         */
        public StripeElements.Line.Dashed getDashed() {
            return dashed;
        }

        /**
         * Legt den Wert der dashed-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link StripeElements.Line.Dashed }
         *     
         * @see #getDashed()
         */
        public void setDashed(StripeElements.Line.Dashed value) {
            this.dashed = value;
        }

        /**
         * Ruft den Wert der width-Eigenschaft ab.
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
         * Legt den Wert der width-Eigenschaft fest.
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
         * Ruft den Wert der color-Eigenschaft ab.
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
         * Legt den Wert der color-Eigenschaft fest.
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
         * <p>Java-Klasse für anonymous complex type.</p>
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <complexContent>
         *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       <sequence>
         *         <sequence maxOccurs="unbounded">
         *           <element name="Gap" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *           <element name="Dash" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *         </sequence>
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
            "gapAndDash"
        })
        public static class Dashed
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElementRefs({
                @XmlElementRef(name = "Gap", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class),
                @XmlElementRef(name = "Dash", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class)
            })
            protected List<JAXBElement<org.opentrafficsim.xml.bindings.types.LengthType>> gapAndDash;

            /**
             * Gets the value of the gapAndDash property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the gapAndDash property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getGapAndDash().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link JAXBElement }{@code <}{@link org.opentrafficsim.xml.bindings.types.LengthType }{@code >}
             * {@link JAXBElement }{@code <}{@link org.opentrafficsim.xml.bindings.types.LengthType }{@code >}
             * </p>
             * 
             * 
             * @return
             *     The value of the gapAndDash property.
             */
            public List<JAXBElement<org.opentrafficsim.xml.bindings.types.LengthType>> getGapAndDash() {
                if (gapAndDash == null) {
                    gapAndDash = new ArrayList<>();
                }
                return this.gapAndDash;
            }

        }

    }

}
