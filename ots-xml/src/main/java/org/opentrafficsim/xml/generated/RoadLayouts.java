
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.StringType;


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
 *         <element name="RoadLayout" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <choice maxOccurs="unbounded">
 *                     <element name="Stripe" maxOccurs="unbounded" minOccurs="0">
 *                       <complexType>
 *                         <complexContent>
 *                           <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             <sequence>
 *                               <choice minOccurs="0">
 *                                 <element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                                 <sequence>
 *                                   <element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                                   <element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *                                 </sequence>
 *                               </choice>
 *                             </sequence>
 *                             <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *                             <attribute name="StripeType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           </restriction>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Lane" maxOccurs="unbounded" minOccurs="0">
 *                       <complexType>
 *                         <complexContent>
 *                           <extension base="{http://www.opentrafficsim.org/ots}CrossSectionElement">
 *                             <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *                           </extension>
 *                         </complexContent>
 *                       </complexType>
 *                     </element>
 *                     <element name="Shoulder" type="{http://www.opentrafficsim.org/ots}CseShoulder" maxOccurs="unbounded" minOccurs="0"/>
 *                   </choice>
 *                 </sequence>
 *                 <attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" />
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *                 <attribute name="Builtin" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
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
@XmlType(name = "", propOrder = {
    "roadLayout"
})
@XmlRootElement(name = "RoadLayouts")
@SuppressWarnings("all") public class RoadLayouts implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Road layout. Lanes to the right of the design line have negative offset.
     *               Those to the left have positive offset. When not specifying offsets, sequence the lines from right (most
     *               negative) to left (most positive).
     * 
     */
    @XmlElement(name = "RoadLayout")
    protected List<RoadLayouts.RoadLayout> roadLayout;

    /**
     * Road layout. Lanes to the right of the design line have negative offset.
     *               Those to the left have positive offset. When not specifying offsets, sequence the lines from right (most
     *               negative) to left (most positive).
     * 
     * Gets the value of the roadLayout property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadLayout property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRoadLayout().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RoadLayouts.RoadLayout }
     * </p>
     * 
     * 
     * @return
     *     The value of the roadLayout property.
     */
    public List<RoadLayouts.RoadLayout> getRoadLayout() {
        if (roadLayout == null) {
            roadLayout = new ArrayList<>();
        }
        return this.roadLayout;
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
     *         <choice maxOccurs="unbounded">
     *           <element name="Stripe" maxOccurs="unbounded" minOccurs="0">
     *             <complexType>
     *               <complexContent>
     *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                   <sequence>
     *                     <choice minOccurs="0">
     *                       <element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                       <sequence>
     *                         <element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                         <element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
     *                       </sequence>
     *                     </choice>
     *                   </sequence>
     *                   <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
     *                   <attribute name="StripeType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 </restriction>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Lane" maxOccurs="unbounded" minOccurs="0">
     *             <complexType>
     *               <complexContent>
     *                 <extension base="{http://www.opentrafficsim.org/ots}CrossSectionElement">
     *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
     *                 </extension>
     *               </complexContent>
     *             </complexType>
     *           </element>
     *           <element name="Shoulder" type="{http://www.opentrafficsim.org/ots}CseShoulder" maxOccurs="unbounded" minOccurs="0"/>
     *         </choice>
     *       </sequence>
     *       <attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" />
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
     *       <attribute name="Builtin" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "stripeOrLaneOrShoulder"
    })
    public static class RoadLayout
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElements({
            @XmlElement(name = "Stripe", type = RoadLayouts.RoadLayout.Stripe.class),
            @XmlElement(name = "Lane", type = RoadLayouts.RoadLayout.Lane.class),
            @XmlElement(name = "Shoulder", type = CseShoulder.class)
        })
        protected List<Serializable> stripeOrLaneOrShoulder;
        @XmlAttribute(name = "LaneKeeping")
        @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
        protected LaneKeepingPolicyType laneKeeping;
        @XmlAttribute(name = "Id", required = true)
        protected String id;
        @XmlAttribute(name = "Builtin")
        protected Boolean builtin;

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
         * {@link CseShoulder }
         * {@link RoadLayouts.RoadLayout.Lane }
         * {@link RoadLayouts.RoadLayout.Stripe }
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
         * Gets the value of the laneKeeping property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LaneKeepingPolicyType getLaneKeeping() {
            return laneKeeping;
        }

        /**
         * Sets the value of the laneKeeping property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLaneKeeping(LaneKeepingPolicyType value) {
            this.laneKeeping = value;
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
         * Gets the value of the builtin property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isBuiltin() {
            if (builtin == null) {
                return false;
            } else {
                return builtin;
            }
        }

        /**
         * Sets the value of the builtin property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setBuiltin(Boolean value) {
            this.builtin = value;
        }


        /**
         * <p>Java class for anonymous complex type</p>.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <complexContent>
         *     <extension base="{http://www.opentrafficsim.org/ots}CrossSectionElement">
         *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
         *     </extension>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Lane
            extends CrossSectionElement
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Id", required = true)
            protected String id;

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
         *         <choice minOccurs="0">
         *           <element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *           <sequence>
         *             <element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *             <element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
         *           </sequence>
         *         </choice>
         *       </sequence>
         *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
         *       <attribute name="StripeType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "centerOffset",
            "centerOffsetStart",
            "centerOffsetEnd"
        })
        public static class Stripe implements Serializable
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
            @XmlAttribute(name = "Id")
            protected String id;
            @XmlAttribute(name = "StripeType", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType stripeType;

            /**
             * Gets the value of the centerOffset property.
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
             * Sets the value of the centerOffset property.
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
             * Gets the value of the centerOffsetStart property.
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
             * Sets the value of the centerOffsetStart property.
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
             * Gets the value of the centerOffsetEnd property.
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
             * Sets the value of the centerOffsetEnd property.
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
             * Gets the value of the stripeType property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getStripeType() {
                return stripeType;
            }

            /**
             * Sets the value of the stripeType property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStripeType(StringType value) {
                this.stripeType = value;
            }

        }

    }

}
