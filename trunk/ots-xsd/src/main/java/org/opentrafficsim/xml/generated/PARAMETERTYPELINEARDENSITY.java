//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.08.11 at 09:31:45 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;


/**
 * <p>Java class for PARAMETERTYPELINEARDENSITY complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PARAMETERTYPELINEARDENSITY"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opentrafficsim.org/ots}PARAMETERTYPE"&gt;
 *       &lt;attribute name="DEFAULT" type="{http://www.opentrafficsim.org/ots}LINEARDENSITYTYPE" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PARAMETERTYPELINEARDENSITY")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-08-11T09:31:45+02:00", comments = "JAXB RI v2.3.0")
public class PARAMETERTYPELINEARDENSITY
    extends PARAMETERTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-08-11T09:31:45+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlAttribute(name = "DEFAULT")
    @XmlJavaTypeAdapter(LinearDensityAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-08-11T09:31:45+02:00", comments = "JAXB RI v2.3.0")
    protected LinearDensity _default;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-08-11T09:31:45+02:00", comments = "JAXB RI v2.3.0")
    public LinearDensity getDEFAULT() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-08-11T09:31:45+02:00", comments = "JAXB RI v2.3.0")
    public void setDEFAULT(LinearDensity value) {
        this._default = value;
    }

}
