//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.05 at 11:08:09 AM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="GTUCOLORERS"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="GTUCOLORER" type="{http://www.opentrafficsim.org/ots}GTUCOLORERTYPE" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="GTUCOLORER" type="{http://www.opentrafficsim.org/ots}GTUCOLORERTYPE"/&gt;
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
    "gtucolorers",
    "gtucolorer"
})
@XmlRootElement(name = "ANIMATION")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
public class ANIMATION
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GTUCOLORERS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    protected ANIMATION.GTUCOLORERS gtucolorers;
    @XmlElement(name = "GTUCOLORER")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    protected GTUCOLORERTYPE gtucolorer;

    /**
     * Gets the value of the gtucolorers property.
     * 
     * @return
     *     possible object is
     *     {@link ANIMATION.GTUCOLORERS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    public ANIMATION.GTUCOLORERS getGTUCOLORERS() {
        return gtucolorers;
    }

    /**
     * Sets the value of the gtucolorers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ANIMATION.GTUCOLORERS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    public void setGTUCOLORERS(ANIMATION.GTUCOLORERS value) {
        this.gtucolorers = value;
    }

    /**
     * Gets the value of the gtucolorer property.
     * 
     * @return
     *     possible object is
     *     {@link GTUCOLORERTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    public GTUCOLORERTYPE getGTUCOLORER() {
        return gtucolorer;
    }

    /**
     * Sets the value of the gtucolorer property.
     * 
     * @param value
     *     allowed object is
     *     {@link GTUCOLORERTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    public void setGTUCOLORER(GTUCOLORERTYPE value) {
        this.gtucolorer = value;
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
     *       &lt;sequence&gt;
     *         &lt;element name="GTUCOLORER" type="{http://www.opentrafficsim.org/ots}GTUCOLORERTYPE" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "gtucolorer"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
    public static class GTUCOLORERS
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "GTUCOLORER", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
        protected List<GTUCOLORERTYPE> gtucolorer;

        /**
         * Gets the value of the gtucolorer property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the gtucolorer property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getGTUCOLORER().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GTUCOLORERTYPE }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T11:08:09+02:00", comments = "JAXB RI v2.3.0")
        public List<GTUCOLORERTYPE> getGTUCOLORER() {
            if (gtucolorer == null) {
                gtucolorer = new ArrayList<GTUCOLORERTYPE>();
            }
            return this.gtucolorer;
        }

    }

}
