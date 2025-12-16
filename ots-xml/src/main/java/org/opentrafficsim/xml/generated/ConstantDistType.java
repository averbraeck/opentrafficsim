
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveInclusiveAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.types.IntegerType;


/**
 * <p>Java class for ConstantDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ConstantDistType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice>
 *           <element name="Constant">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Exponential">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Triangular">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Mode" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Normal">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="NormalTrunc">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" />
 *                   <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Beta">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Erlang">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Mean" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="K" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Gamma">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="LogNormal">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="LogNormalTrunc">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Pearson5">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Pearson6">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Uniform">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                   <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *           <element name="Weibull">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
 *                   <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
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
@XmlType(name = "ConstantDistType", propOrder = {
    "constant",
    "exponential",
    "triangular",
    "normal",
    "normalTrunc",
    "beta",
    "erlang",
    "gamma",
    "logNormal",
    "logNormalTrunc",
    "pearson5",
    "pearson6",
    "uniform",
    "weibull",
    "randomStream"
})
@XmlSeeAlso({
    LengthDistType.class,
    SpeedDistType.class,
    AccelerationDistType.class,
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.DoubleDist.class,
    PositionDistType.class,
    TimeDistType.class,
    DurationDistType.class,
    LinearDensityDistType.class,
    FrequencyDistType.class
})
@SuppressWarnings("all") public class ConstantDistType implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Constant")
    protected ConstantDistType.Constant constant;
    @XmlElement(name = "Exponential")
    protected ConstantDistType.Exponential exponential;
    @XmlElement(name = "Triangular")
    protected ConstantDistType.Triangular triangular;
    @XmlElement(name = "Normal")
    protected ConstantDistType.Normal normal;
    @XmlElement(name = "NormalTrunc")
    protected ConstantDistType.NormalTrunc normalTrunc;
    @XmlElement(name = "Beta")
    protected ConstantDistType.Beta beta;
    @XmlElement(name = "Erlang")
    protected ConstantDistType.Erlang erlang;
    @XmlElement(name = "Gamma")
    protected ConstantDistType.Gamma gamma;
    @XmlElement(name = "LogNormal")
    protected ConstantDistType.LogNormal logNormal;
    @XmlElement(name = "LogNormalTrunc")
    protected ConstantDistType.LogNormalTrunc logNormalTrunc;
    @XmlElement(name = "Pearson5")
    protected ConstantDistType.Pearson5 pearson5;
    @XmlElement(name = "Pearson6")
    protected ConstantDistType.Pearson6 pearson6;
    @XmlElement(name = "Uniform")
    protected ConstantDistType.Uniform uniform;
    @XmlElement(name = "Weibull")
    protected ConstantDistType.Weibull weibull;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Constant }
     *     
     */
    public ConstantDistType.Constant getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Constant }
     *     
     */
    public void setConstant(ConstantDistType.Constant value) {
        this.constant = value;
    }

    /**
     * Gets the value of the exponential property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Exponential }
     *     
     */
    public ConstantDistType.Exponential getExponential() {
        return exponential;
    }

    /**
     * Sets the value of the exponential property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Exponential }
     *     
     */
    public void setExponential(ConstantDistType.Exponential value) {
        this.exponential = value;
    }

    /**
     * Gets the value of the triangular property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Triangular }
     *     
     */
    public ConstantDistType.Triangular getTriangular() {
        return triangular;
    }

    /**
     * Sets the value of the triangular property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Triangular }
     *     
     */
    public void setTriangular(ConstantDistType.Triangular value) {
        this.triangular = value;
    }

    /**
     * Gets the value of the normal property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Normal }
     *     
     */
    public ConstantDistType.Normal getNormal() {
        return normal;
    }

    /**
     * Sets the value of the normal property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Normal }
     *     
     */
    public void setNormal(ConstantDistType.Normal value) {
        this.normal = value;
    }

    /**
     * Gets the value of the normalTrunc property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.NormalTrunc }
     *     
     */
    public ConstantDistType.NormalTrunc getNormalTrunc() {
        return normalTrunc;
    }

    /**
     * Sets the value of the normalTrunc property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.NormalTrunc }
     *     
     */
    public void setNormalTrunc(ConstantDistType.NormalTrunc value) {
        this.normalTrunc = value;
    }

    /**
     * Gets the value of the beta property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Beta }
     *     
     */
    public ConstantDistType.Beta getBeta() {
        return beta;
    }

    /**
     * Sets the value of the beta property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Beta }
     *     
     */
    public void setBeta(ConstantDistType.Beta value) {
        this.beta = value;
    }

    /**
     * Gets the value of the erlang property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Erlang }
     *     
     */
    public ConstantDistType.Erlang getErlang() {
        return erlang;
    }

    /**
     * Sets the value of the erlang property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Erlang }
     *     
     */
    public void setErlang(ConstantDistType.Erlang value) {
        this.erlang = value;
    }

    /**
     * Gets the value of the gamma property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Gamma }
     *     
     */
    public ConstantDistType.Gamma getGamma() {
        return gamma;
    }

    /**
     * Sets the value of the gamma property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Gamma }
     *     
     */
    public void setGamma(ConstantDistType.Gamma value) {
        this.gamma = value;
    }

    /**
     * Gets the value of the logNormal property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.LogNormal }
     *     
     */
    public ConstantDistType.LogNormal getLogNormal() {
        return logNormal;
    }

    /**
     * Sets the value of the logNormal property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.LogNormal }
     *     
     */
    public void setLogNormal(ConstantDistType.LogNormal value) {
        this.logNormal = value;
    }

    /**
     * Gets the value of the logNormalTrunc property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.LogNormalTrunc }
     *     
     */
    public ConstantDistType.LogNormalTrunc getLogNormalTrunc() {
        return logNormalTrunc;
    }

    /**
     * Sets the value of the logNormalTrunc property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.LogNormalTrunc }
     *     
     */
    public void setLogNormalTrunc(ConstantDistType.LogNormalTrunc value) {
        this.logNormalTrunc = value;
    }

    /**
     * Gets the value of the pearson5 property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Pearson5 }
     *     
     */
    public ConstantDistType.Pearson5 getPearson5() {
        return pearson5;
    }

    /**
     * Sets the value of the pearson5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Pearson5 }
     *     
     */
    public void setPearson5(ConstantDistType.Pearson5 value) {
        this.pearson5 = value;
    }

    /**
     * Gets the value of the pearson6 property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Pearson6 }
     *     
     */
    public ConstantDistType.Pearson6 getPearson6() {
        return pearson6;
    }

    /**
     * Sets the value of the pearson6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Pearson6 }
     *     
     */
    public void setPearson6(ConstantDistType.Pearson6 value) {
        this.pearson6 = value;
    }

    /**
     * Gets the value of the uniform property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Uniform }
     *     
     */
    public ConstantDistType.Uniform getUniform() {
        return uniform;
    }

    /**
     * Sets the value of the uniform property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Uniform }
     *     
     */
    public void setUniform(ConstantDistType.Uniform value) {
        this.uniform = value;
    }

    /**
     * Gets the value of the weibull property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Weibull }
     *     
     */
    public ConstantDistType.Weibull getWeibull() {
        return weibull;
    }

    /**
     * Sets the value of the weibull property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Weibull }
     *     
     */
    public void setWeibull(ConstantDistType.Weibull value) {
        this.weibull = value;
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
     *       <attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Beta
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha1", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha1;
        @XmlAttribute(name = "Alpha2", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha2;

        /**
         * Gets the value of the alpha1 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha1() {
            return alpha1;
        }

        /**
         * Sets the value of the alpha1 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha1(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha1 = value;
        }

        /**
         * Gets the value of the alpha2 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha2() {
            return alpha2;
        }

        /**
         * Sets the value of the alpha2 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha2(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha2 = value;
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
     *       <attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}double" />
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
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType c;

        /**
         * Gets the value of the c property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getC() {
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
        public void setC(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
     *       <attribute name="Mean" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="K" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Erlang
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mean", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mean;
        @XmlAttribute(name = "K", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        protected IntegerType k;

        /**
         * Gets the value of the mean property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMean() {
            return mean;
        }

        /**
         * Sets the value of the mean property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMean(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mean = value;
        }

        /**
         * Gets the value of the k property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public IntegerType getK() {
            return k;
        }

        /**
         * Sets the value of the k property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setK(IntegerType value) {
            this.k = value;
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
    public static class Exponential
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
     *       <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Gamma
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Gets the value of the alpha property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Sets the value of the alpha property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Gets the value of the beta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Sets the value of the beta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LogNormal
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;

        /**
         * Gets the value of the mu property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Sets the value of the mu property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Gets the value of the sigma property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Sets the value of the sigma property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
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
     *       <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LogNormalTrunc
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Gets the value of the mu property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Sets the value of the mu property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Gets the value of the sigma property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Sets the value of the sigma property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
        }

        /**
         * Gets the value of the min property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Normal
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveInclusiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;

        /**
         * Gets the value of the mu property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Sets the value of the mu property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Gets the value of the sigma property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Sets the value of the sigma property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
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
     *       <attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" />
     *       <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NormalTrunc
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveInclusiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Gets the value of the mu property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Sets the value of the mu property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Gets the value of the sigma property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Sets the value of the sigma property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
        }

        /**
         * Gets the value of the min property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Pearson5
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Gets the value of the alpha property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Sets the value of the alpha property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Gets the value of the beta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Sets the value of the beta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       <attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Pearson6
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha1", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha1;
        @XmlAttribute(name = "Alpha2", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha2;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Gets the value of the alpha1 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha1() {
            return alpha1;
        }

        /**
         * Sets the value of the alpha1 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha1(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha1 = value;
        }

        /**
         * Gets the value of the alpha2 property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha2() {
            return alpha2;
        }

        /**
         * Sets the value of the alpha2 property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha2(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha2 = value;
        }

        /**
         * Gets the value of the beta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Sets the value of the beta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Mode" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Triangular
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Mode", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType mode;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Gets the value of the min property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.min = value;
        }

        /**
         * Gets the value of the mode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMode() {
            return mode;
        }

        /**
         * Sets the value of the mode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMode(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mode = value;
        }

        /**
         * Gets the value of the max property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       <attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *       <attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" />
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
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Gets the value of the min property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       <attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *       <attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Weibull
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Gets the value of the alpha property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Sets the value of the alpha property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Gets the value of the beta property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Sets the value of the beta property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
        }

    }

}
