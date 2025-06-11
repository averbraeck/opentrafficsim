
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
 * <p>Java-Klasse für DiscreteDistType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der constant-Eigenschaft ab.
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
     * Legt den Wert der constant-Eigenschaft fest.
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
     * Ruft den Wert der bernoulliI-Eigenschaft ab.
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
     * Legt den Wert der bernoulliI-Eigenschaft fest.
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
     * Ruft den Wert der binomial-Eigenschaft ab.
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
     * Legt den Wert der binomial-Eigenschaft fest.
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
     * Ruft den Wert der uniform-Eigenschaft ab.
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
     * Legt den Wert der uniform-Eigenschaft fest.
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
     * Ruft den Wert der geometric-Eigenschaft ab.
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
     * Legt den Wert der geometric-Eigenschaft fest.
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
     * Ruft den Wert der negBinomial-Eigenschaft ab.
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
     * Legt den Wert der negBinomial-Eigenschaft fest.
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
     * Ruft den Wert der poisson-Eigenschaft ab.
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
     * Legt den Wert der poisson-Eigenschaft fest.
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
     * Ruft den Wert der randomStream-Eigenschaft ab.
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
     * Legt den Wert der randomStream-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der p-Eigenschaft ab.
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
         * Legt den Wert der p-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der n-Eigenschaft ab.
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
         * Legt den Wert der n-Eigenschaft fest.
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
         * Ruft den Wert der p-Eigenschaft ab.
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
         * Legt den Wert der p-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der c-Eigenschaft ab.
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
         * Legt den Wert der c-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der p-Eigenschaft ab.
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
         * Legt den Wert der p-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der n-Eigenschaft ab.
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
         * Legt den Wert der n-Eigenschaft fest.
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
         * Ruft den Wert der p-Eigenschaft ab.
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
         * Legt den Wert der p-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der lambda-Eigenschaft ab.
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
         * Legt den Wert der lambda-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
         * Ruft den Wert der min-Eigenschaft ab.
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
         * Legt den Wert der min-Eigenschaft fest.
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
         * Ruft den Wert der max-Eigenschaft ab.
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
         * Legt den Wert der max-Eigenschaft fest.
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
