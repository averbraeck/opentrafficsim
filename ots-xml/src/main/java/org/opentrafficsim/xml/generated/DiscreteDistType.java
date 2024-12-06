
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.IntegerAdapter;
import org.opentrafficsim.xml.bindings.LongAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.types.LongType;


/**
 * <p>Java class for DiscreteDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="DiscreteDistType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice>
 *           <element name="Constant">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}long" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="BernoulliI">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Binomial">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
 *                   <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Uniform">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}int" />
 *                   <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}int" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Geometric">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="NegBinomial">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
 *                   <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Poisson">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *         </choice>
 *         <element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiscreteDistType", propOrder = {
    "constant",
    "bernoulliI",
    "binomial",
    "uniform",
    "geometric",
    "negBinomial",
    "poisson",
    "randomStream"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.IntegerDist.class
})
@SuppressWarnings("all") public class DiscreteDistType implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Constant")
    protected DiscreteDistType.Constant constant;
    @XmlElement(name = "BernoulliI")
    protected DiscreteDistType.BernoulliI bernoulliI;
    @XmlElement(name = "Binomial")
    protected DiscreteDistType.Binomial binomial;
    @XmlElement(name = "Uniform")
    protected DiscreteDistType.Uniform uniform;
    @XmlElement(name = "Geometric")
    protected DiscreteDistType.Geometric geometric;
    @XmlElement(name = "NegBinomial")
    protected DiscreteDistType.NegBinomial negBinomial;
    @XmlElement(name = "Poisson")
    protected DiscreteDistType.Poisson poisson;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Constant }
     *     
     */
    public DiscreteDistType.Constant getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Constant }
     *     
     */
    public void setConstant(DiscreteDistType.Constant value) {
        this.constant = value;
    }

    /**
     * Gets the value of the bernoulliI property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.BernoulliI }
     *     
     */
    public DiscreteDistType.BernoulliI getBernoulliI() {
        return bernoulliI;
    }

    /**
     * Sets the value of the bernoulliI property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.BernoulliI }
     *     
     */
    public void setBernoulliI(DiscreteDistType.BernoulliI value) {
        this.bernoulliI = value;
    }

    /**
     * Gets the value of the binomial property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Binomial }
     *     
     */
    public DiscreteDistType.Binomial getBinomial() {
        return binomial;
    }

    /**
     * Sets the value of the binomial property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Binomial }
     *     
     */
    public void setBinomial(DiscreteDistType.Binomial value) {
        this.binomial = value;
    }

    /**
     * Gets the value of the uniform property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Uniform }
     *     
     */
    public DiscreteDistType.Uniform getUniform() {
        return uniform;
    }

    /**
     * Sets the value of the uniform property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Uniform }
     *     
     */
    public void setUniform(DiscreteDistType.Uniform value) {
        this.uniform = value;
    }

    /**
     * Gets the value of the geometric property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Geometric }
     *     
     */
    public DiscreteDistType.Geometric getGeometric() {
        return geometric;
    }

    /**
     * Sets the value of the geometric property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Geometric }
     *     
     */
    public void setGeometric(DiscreteDistType.Geometric value) {
        this.geometric = value;
    }

    /**
     * Gets the value of the negBinomial property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.NegBinomial }
     *     
     */
    public DiscreteDistType.NegBinomial getNegBinomial() {
        return negBinomial;
    }

    /**
     * Sets the value of the negBinomial property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.NegBinomial }
     *     
     */
    public void setNegBinomial(DiscreteDistType.NegBinomial value) {
        this.negBinomial = value;
    }

    /**
     * Gets the value of the poisson property.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Poisson }
     *     
     */
    public DiscreteDistType.Poisson getPoisson() {
        return poisson;
    }

    /**
     * Sets the value of the poisson property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Poisson }
     *     
     */
    public void setPoisson(DiscreteDistType.Poisson value) {
        this.poisson = value;
    }

    /**
     * Gets the value of the randomStream property.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreamSource }
     *     
     */
    public RandomStreamSource getRandomStream() {
        return randomStream;
    }

    /**
     * Sets the value of the randomStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreamSource }
     *     
     */
    public void setRandomStream(RandomStreamSource value) {
        this.randomStream = value;
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
     *       <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class BernoulliI
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       <attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
     *       <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Binomial
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.IntegerType n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Gets the value of the n property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.IntegerType getN() {
            return n;
        }

        /**
         * Sets the value of the n property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setN(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.n = value;
        }

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       <attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}long" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Constant
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "C", required = true)
        @XmlJavaTypeAdapter(LongAdapter.class)
        protected LongType c;

        /**
         * Gets the value of the c property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LongType getC() {
            return c;
        }

        /**
         * Sets the value of the c property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setC(LongType value) {
            this.c = value;
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
     *       <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Geometric
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       <attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
     *       <attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NegBinomial
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.IntegerType n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Gets the value of the n property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.IntegerType getN() {
            return n;
        }

        /**
         * Sets the value of the n property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setN(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.n = value;
        }

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       <attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Poisson
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Lambda", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType lambda;

        /**
         * Gets the value of the lambda property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getLambda() {
            return lambda;
        }

        /**
         * Sets the value of the lambda property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLambda(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.lambda = value;
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
     *       <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}int" />
     *       <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}int" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Uniform
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(IntegerAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.IntegerType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(IntegerAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.IntegerType max;

        /**
         * Gets the value of the min property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.IntegerType getMin() {
            return min;
        }

        /**
         * Sets the value of the min property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMin(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.min = value;
        }

        /**
         * Gets the value of the max property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.IntegerType getMax() {
            return max;
        }

        /**
         * Sets the value of the max property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMax(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.max = value;
        }

    }

}
