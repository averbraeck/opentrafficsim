//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 08:30:33 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.ClassNameAdapter;


/**
 * <p>Java class for CARFOLLOWINGMODELHEADWAYSPEEDTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CARFOLLOWINGMODELHEADWAYSPEEDTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DESIREDHEADWAYMODEL" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="IDM" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *                   &lt;element name="CLASS" type="{http://www.opentrafficsim.org/ots}CLASSNAMETYPE"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="DESIREDSPEEDMODEL" type="{http://www.opentrafficsim.org/ots}DESIREDSPEEDMODELTYPE" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CARFOLLOWINGMODELHEADWAYSPEEDTYPE", propOrder = {
    "desiredheadwaymodel",
    "desiredspeedmodel"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
public class CARFOLLOWINGMODELHEADWAYSPEEDTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "DESIREDHEADWAYMODEL")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected CARFOLLOWINGMODELHEADWAYSPEEDTYPE.DESIREDHEADWAYMODEL desiredheadwaymodel;
    @XmlElement(name = "DESIREDSPEEDMODEL")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected DESIREDSPEEDMODELTYPE desiredspeedmodel;

    /**
     * Gets the value of the desiredheadwaymodel property.
     * 
     * @return
     *     possible object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE.DESIREDHEADWAYMODEL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public CARFOLLOWINGMODELHEADWAYSPEEDTYPE.DESIREDHEADWAYMODEL getDESIREDHEADWAYMODEL() {
        return desiredheadwaymodel;
    }

    /**
     * Sets the value of the desiredheadwaymodel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE.DESIREDHEADWAYMODEL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setDESIREDHEADWAYMODEL(CARFOLLOWINGMODELHEADWAYSPEEDTYPE.DESIREDHEADWAYMODEL value) {
        this.desiredheadwaymodel = value;
    }

    /**
     * Gets the value of the desiredspeedmodel property.
     * 
     * @return
     *     possible object is
     *     {@link DESIREDSPEEDMODELTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public DESIREDSPEEDMODELTYPE getDESIREDSPEEDMODEL() {
        return desiredspeedmodel;
    }

    /**
     * Sets the value of the desiredspeedmodel property.
     * 
     * @param value
     *     allowed object is
     *     {@link DESIREDSPEEDMODELTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setDESIREDSPEEDMODEL(DESIREDSPEEDMODELTYPE value) {
        this.desiredspeedmodel = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="IDM" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
     *         &lt;element name="CLASS" type="{http://www.opentrafficsim.org/ots}CLASSNAMETYPE"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "idm",
        "_class"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public static class DESIREDHEADWAYMODEL
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "IDM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected Object idm;
        @XmlElement(name = "CLASS", type = String.class)
        @XmlJavaTypeAdapter(ClassNameAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected Class _class;

        /**
         * Gets the value of the idm property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public Object getIDM() {
            return idm;
        }

        /**
         * Sets the value of the idm property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public void setIDM(Object value) {
            this.idm = value;
        }

        /**
         * Gets the value of the class property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public Class getCLASS() {
            return _class;
        }

        /**
         * Sets the value of the class property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public void setCLASS(Class value) {
            this._class = value;
        }

    }

}
