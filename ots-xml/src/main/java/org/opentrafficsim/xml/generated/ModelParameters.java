
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.LongAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationType;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.LongType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for ModelParameters complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ModelParameters">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence maxOccurs="unbounded" minOccurs="0">
 *         <choice maxOccurs="unbounded">
 *           <element name="Duration">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>DurationType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="DurationDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}DurationDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Length">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>LengthType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="LengthDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}LengthDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Speed">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="SpeedDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}SpeedDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Acceleration">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="AccelerationDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="LinearDensity">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="LinearDensityDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Frequency">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="FrequencyDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Double">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>double">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="DoubleDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Fraction">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>FractionType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="Integer">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>integer">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="IntegerDist">
 *             <complexType>
 *               <complexContent>
 *                 <extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Boolean">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>boolean">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="String">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>string">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *           <element name="Class">
 *             <complexType>
 *               <simpleContent>
 *                 <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
 *                   <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 </extension>
 *               </simpleContent>
 *             </complexType>
 *           </element>
 *         </choice>
 *         <element name="Correlation" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/>
 *                   <element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/>
 *                 </sequence>
 *                 <attribute name="Expression">
 *                   <simpleType>
 *                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       <pattern value="[^{}]+"/>
 *                     </restriction>
 *                   </simpleType>
 *                 </attribute>
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
@XmlType(name = "ModelParameters", propOrder = {
    "durationOrDurationDistOrLength"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.GtuTypeParameters.class
})
@SuppressWarnings("all") public class ModelParameters
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Duration", type = ModelParameters.Duration.class),
        @XmlElement(name = "DurationDist", type = ModelParameters.DurationDist.class),
        @XmlElement(name = "Length", type = ModelParameters.Length.class),
        @XmlElement(name = "LengthDist", type = ModelParameters.LengthDist.class),
        @XmlElement(name = "Speed", type = ModelParameters.Speed.class),
        @XmlElement(name = "SpeedDist", type = ModelParameters.SpeedDist.class),
        @XmlElement(name = "Acceleration", type = ModelParameters.Acceleration.class),
        @XmlElement(name = "AccelerationDist", type = ModelParameters.AccelerationDist.class),
        @XmlElement(name = "LinearDensity", type = ModelParameters.LinearDensity.class),
        @XmlElement(name = "LinearDensityDist", type = ModelParameters.LinearDensityDist.class),
        @XmlElement(name = "Frequency", type = ModelParameters.Frequency.class),
        @XmlElement(name = "FrequencyDist", type = ModelParameters.FrequencyDist.class),
        @XmlElement(name = "Double", type = ModelParameters.Double.class),
        @XmlElement(name = "DoubleDist", type = ModelParameters.DoubleDist.class),
        @XmlElement(name = "Fraction", type = ModelParameters.Fraction.class),
        @XmlElement(name = "Integer", type = ModelParameters.Integer.class),
        @XmlElement(name = "IntegerDist", type = ModelParameters.IntegerDist.class),
        @XmlElement(name = "Boolean", type = ModelParameters.Boolean.class),
        @XmlElement(name = "String", type = ModelParameters.String.class),
        @XmlElement(name = "Class", type = ModelParameters.Class.class),
        @XmlElement(name = "Correlation", type = ModelParameters.Correlation.class)
    })
    protected List<Serializable> durationOrDurationDistOrLength;

    /**
     * Gets the value of the durationOrDurationDistOrLength property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the durationOrDurationDistOrLength property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDurationOrDurationDistOrLength().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelParameters.Acceleration }
     * {@link ModelParameters.AccelerationDist }
     * {@link ModelParameters.Boolean }
     * {@link ModelParameters.Class }
     * {@link ModelParameters.Correlation }
     * {@link ModelParameters.Double }
     * {@link ModelParameters.DoubleDist }
     * {@link ModelParameters.Duration }
     * {@link ModelParameters.DurationDist }
     * {@link ModelParameters.Fraction }
     * {@link ModelParameters.Frequency }
     * {@link ModelParameters.FrequencyDist }
     * {@link ModelParameters.Integer }
     * {@link ModelParameters.IntegerDist }
     * {@link ModelParameters.Length }
     * {@link ModelParameters.LengthDist }
     * {@link ModelParameters.LinearDensity }
     * {@link ModelParameters.LinearDensityDist }
     * {@link ModelParameters.Speed }
     * {@link ModelParameters.SpeedDist }
     * {@link ModelParameters.String }
     * </p>
     * 
     * 
     * @return
     *     The value of the durationOrDurationDistOrLength property.
     */
    public List<Serializable> getDurationOrDurationDistOrLength() {
        if (durationOrDurationDistOrLength == null) {
            durationOrDurationDistOrLength = new ArrayList<>();
        }
        return this.durationOrDurationDistOrLength;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Acceleration implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(AccelerationAdapter.class)
        protected AccelerationType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public AccelerationType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(AccelerationType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}AccelerationDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AccelerationDist
        extends AccelerationDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>boolean">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Boolean implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public BooleanType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(BooleanType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Class implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(ClassAdapter.class)
        protected ClassType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public ClassType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(ClassType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *         <element name="First" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType" minOccurs="0"/>
     *         <element name="Then" type="{http://www.opentrafficsim.org/ots}CorrelationParameterType"/>
     *       </sequence>
     *       <attribute name="Expression">
     *         <simpleType>
     *           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             <pattern value="[^{}]+"/>
     *           </restriction>
     *         </simpleType>
     *       </attribute>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "first",
        "then"
    })
    public static class Correlation implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "First")
        protected CorrelationParameterType first;
        @XmlElement(name = "Then", required = true)
        protected CorrelationParameterType then;
        /**
         * Expression with a local context. Use 'first' and 'then' to refer to
         *                 the two base parameter values. The expression result will become the value for 'then'. Argument 'first' may be
         *                 empty. For example "then + 0.2*first" increases the 'then' value by 20% of 'first'.
         * 
         */
        @XmlAttribute(name = "Expression")
        protected java.lang.String expression;

        /**
         * Gets the value of the first property.
         * 
         * @return
         *     possible object is
         *     {@link CorrelationParameterType }
         *     
         */
        public CorrelationParameterType getFirst() {
            return first;
        }

        /**
         * Sets the value of the first property.
         * 
         * @param value
         *     allowed object is
         *     {@link CorrelationParameterType }
         *     
         */
        public void setFirst(CorrelationParameterType value) {
            this.first = value;
        }

        /**
         * Gets the value of the then property.
         * 
         * @return
         *     possible object is
         *     {@link CorrelationParameterType }
         *     
         */
        public CorrelationParameterType getThen() {
            return then;
        }

        /**
         * Sets the value of the then property.
         * 
         * @param value
         *     allowed object is
         *     {@link CorrelationParameterType }
         *     
         */
        public void setThen(CorrelationParameterType value) {
            this.then = value;
        }

        /**
         * Expression with a local context. Use 'first' and 'then' to refer to
         *                 the two base parameter values. The expression result will become the value for 'then'. Argument 'first' may be
         *                 empty. For example "then + 0.2*first" increases the 'then' value by 20% of 'first'.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getExpression() {
            return expression;
        }

        /**
         * Sets the value of the expression property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         * @see #getExpression()
         */
        public void setExpression(java.lang.String value) {
            this.expression = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>double">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Double implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DoubleDist
        extends ConstantDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>DurationType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Duration implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(DurationAdapter.class)
        protected DurationType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public DurationType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(DurationType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}DurationDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DurationDist
        extends DurationDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>FractionType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Fraction implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Frequency implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(FrequencyAdapter.class)
        protected FrequencyType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public FrequencyType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(FrequencyType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}FrequencyDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class FrequencyDist
        extends FrequencyDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>integer">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Integer implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LongAdapter.class)
        protected LongType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LongType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LongType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}DiscreteDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class IntegerDist
        extends DiscreteDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>LengthType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Length implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected LengthType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LengthType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LengthType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}LengthDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LengthDist
        extends LengthDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class LinearDensity implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        protected LinearDensityType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LinearDensityType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LinearDensityType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}LinearDensityDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LinearDensityDist
        extends LinearDensityDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Speed implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(SpeedAdapter.class)
        protected SpeedType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public SpeedType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(SpeedType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}SpeedDistType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SpeedDist
        extends SpeedDistType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
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
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>string">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class String implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType value;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public StringType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(StringType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
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
         *     {@link java.lang.String }
         *     
         */
        public void setId(StringType value) {
            this.id = value;
        }

    }

}
