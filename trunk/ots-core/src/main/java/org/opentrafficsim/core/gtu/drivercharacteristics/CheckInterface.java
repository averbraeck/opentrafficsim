package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.opentrafficsim.core.gtu.drivercharacteristics.AbstractParameterType.Check;

/**
 * In order to define default constraints within a Parameter Type, an <tt>enum</tt> is available. This interface supplies easy
 * access to the values of this <tt>enum</tt>. To use this interface, simply implement it as below. The value <tt>POSITIVE</tt>
 * is a property of this interface pointing to the <tt>enum</tt> field <tt>POSITIVE</tt>. As a result, the value that is set for
 * <tt>X</tt> is checked to be above zero. Note that model and parameterType do not have to be defined in the same class.
 * 
 * <pre>
 * public class myModel implements CheckInterface
 * {
 * 
 *     public static final ParameterTypeLength X = new ParameterTypeLength(&quot;x&quot;, &quot;My x parameter.&quot;, POSITIVE);
 * 
 *     // ... model that uses parameter of type X.
 * 
 * }
 * </pre>
 * 
 * Another way to access the <tt>enum</tt> fields is to import them, e.g.:
 * 
 * <pre>
 * import static org.opentrafficsim.core.gtu.drivercharacteristics.AbstractParameterType.Check.POSITIVE;
 * </pre>
 * 
 * <br>
 * In order to implement <i>custom</i> checks, any Parameter Type must extend the <tt>check</tt> method of its super. An example
 * is given below. The method should throw a <tt>ParameterException</tt> whenever a constraint is not met. The static
 * <tt>failIf</tt> method is used to do this. The first check is a simple check on the SI value being above 2. The second check
 * compares the value with the value of another parameter in the <tt>BehavioralCharacteristics</tt>. These checks can only be
 * performed if the other parameter is present in the <tt>BehavioralCharacteristics</tt>. <b>Checks with other parameter type
 * values should always check whether <tt>BehavioralCharacteristics</tt> contains the other parameter type</b>. i.e.
 * <tt>bc.contains()</tt>.<br>
 * 
 * <pre>
 * public static final ParameterTypeLength X = new ParameterTypeLength(&quot;x&quot;, &quot;My x parameter.&quot;)
 * {
 *     public void check(Length.Rel value, BehavioralCharacteristics bc) throws ParameterException
 *     {
 *         ParameterException.failIf(value.si &lt;= 2, &quot;Value of X is not above 2.&quot;);
 *         ParameterException.failIf(bc.contains(Y) &amp;&amp; value.si &gt; bc.getParameter(Y).si,
 *             &quot;Value of X is larger than value of Y.&quot;);
 *     }
 * };
 * </pre>
 * 
 * Checks are invoked on default values (if given), in which case an empty <tt>BehavioralCharacteristics</tt> is forwarded. At
 * construction of a Parameter Type, no <tt>BehavioralCharacteristics</tt> is available. Checks are also invoked when set into
 * <tt>BehavioralCharacteristics</tt>, in which case that <tt>BehavioralCharacteristics</tt> forwards itself. Even still, if in
 * the above case X is set before Y, the check is never performed. Therefore, Y should also compare itself to X. <b>Two
 * parameters that are checked with each other, should both implement a check which is consistent with and mirrored to the the
 * others' check.</b> <br>
 * <br>
 * The type of the first argument in the <tt>check()</tt> method depends on the super Parameter Type. For example:<br>
 * <li><tt>double</tt> for <tt>ParameterTypeDouble</tt></li> <li><tt>int</tt> for <tt>ParameterTypeInteger</tt></li> <li>
 * <tt>Speed</tt> for <tt>ParameterTypeSpeed</tt></li> <li><tt>Length.Rel</tt> for <tt>ParameterTypeLength</tt></li> <li>
 * <tt>T</tt> for <tt>ParameterType&lt;T&gt;</tt></li> Note that <tt>ParameterTypeBoolean</tt> has no check method as checks on
 * booleans are senseless.<br>
 * <br>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version 7 apr. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@SuppressWarnings({"checkstyle:interfaceistype", "checkstyle:javadoctype", "checkstyle:javadocvariable", "javadoc"})
public interface CheckInterface
{
    // @formatter:off
    
    Check POSITIVE     = Check.POSITIVE;
    Check NEGATIVE     = Check.NEGATIVE;
    Check POSITIVEZERO = Check.POSITIVEZERO;
    Check NEGATIVEZERO = Check.NEGATIVEZERO;
    Check NONZERO      = Check.NONZERO;
    Check UNITINTERVAL = Check.UNITINTERVAL;
    
}
