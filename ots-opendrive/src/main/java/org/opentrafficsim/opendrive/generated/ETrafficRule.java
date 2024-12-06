
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_trafficRule</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_trafficRule">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="RHT"/>
 *     <enumeration value="LHT"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_trafficRule")
@XmlEnum
@SuppressWarnings("all") public enum ETrafficRule {

    RHT,
    LHT;

    public String value() {
        return name();
    }

    public static ETrafficRule fromValue(String v) {
        return valueOf(v);
    }

}
