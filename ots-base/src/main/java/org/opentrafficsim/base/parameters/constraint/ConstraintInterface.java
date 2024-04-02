package org.opentrafficsim.base.parameters.constraint;

/**
 * In order to define default constraints within a Parameter Type, an <code>enum</code> is available. This interface supplies
 * easy access to the values of this <code>enum</code>. To use this interface, simply implement it as below. The value
 * <code>POSITIVE</code> is a property of this interface pointing to the <code>enum</code> field <code>POSITIVE</code>. As a
 * result, the value that is set for <code>X</code> is checked to be above zero. Note that model and parameterType do not have
 * to be defined in the same class.
 * 
 * <pre>
 * public class myModel implements ConstraintInterface
 * {
 * 
 *     public static final ParameterTypeLength X = new ParameterTypeLength(&quot;x&quot;, &quot;My x parameter.&quot;, POSITIVE);
 * 
 *     // ... model that uses parameter of type X.
 * 
 * }
 * </pre>
 * 
 * Another way to access the <code>enum</code> fields is to import them, e.g.:
 * 
 * <pre>
 * import static org.opentrafficsim.core.gtu.drivercharacteristics.AbstractParameterType.Constraint.POSITIVE;
 * </pre>
 * 
 * <br>
 * In order to implement <i>custom</i> checks, any Parameter Type must extend the <code>check</code> method of its super. An
 * example is given below. The method should throw a <code>ParameterException</code> whenever a constraint is not met. The
 * static <code>throwIf</code> method is used to do this. The first check is a simple check on the SI value being above 2. The
 * second check compares the value with the value of another parameter in the <code>Parameters</code>. These checks can only be
 * performed if the other parameter is present in the <code>Parameters</code>. <b>Checks with other parameter type values should
 * always check whether <code>Parameters</code> contains the other parameter type</b>. i.e. <code>params.contains()</code>.<br>
 * 
 * <pre>
 * public static final ParameterTypeLength X = new ParameterTypeLength(&quot;x&quot;, &quot;My x parameter.&quot;)
 * {
 *     public void check(Length value, Parameters params) throws ParameterException
 *     {
 *         Throw.when(value.si &lt;= 2, ParameterException.class, &quot;Value of X is not above 2.&quot;);
 *         Throw.when(params.contains(Y) &amp;&amp; value.si &gt; params.getParameter(Y).si, ParameterException.class,
 *                 &quot;Value of X is larger than value of Y.&quot;);
 *     }
 * };
 * </pre>
 * 
 * Checks are invoked on default values (if given), in which case an empty <code>Parameters</code> is forwarded. At construction
 * of a Parameter Type, no <code>Parameters</code> is available. Checks are also invoked when value are set into
 * <code>Parameters</code>, in which case that <code>Parameters</code> forwards itself. Even still, if in the above case X is
 * set before Y, the check is never performed. Therefore, Y should also compare itself to X. <b>Two parameters that are checked
 * with each other, should both implement a check which is consistent with and mirrored to the the others' check.</b> <br>
 * <br>
 * The type of the first argument in the <code>check()</code> method depends on the super Parameter Type. For example:<br>
 * <ul>
 * <li><code>double</code> for <code>ParameterTypeDouble</code></li>
 * <li><code>int</code> for <code>ParameterTypeInteger</code></li>
 * <li><code>Speed</code> for <code>ParameterTypeSpeed</code></li>
 * <li><code>Length</code> for <code>ParameterTypeLength</code></li>
 * <li><code>T</code> for <code>ParameterType&lt;T&gt;</code></li>
 * </ul>
 * Note that <code>ParameterTypeBoolean</code> has no check method as checks on booleans are senseless.<br>
 * <br>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings({"checkstyle:interfaceistype", "checkstyle:javadoctype", "checkstyle:javadocvariable", "javadoc"})
public interface ConstraintInterface
{

    // @formatter:off
    Constraint<Number> POSITIVE     = NumericConstraint.POSITIVE;
    Constraint<Number> NEGATIVE     = NumericConstraint.NEGATIVE;
    Constraint<Number> POSITIVEZERO = NumericConstraint.POSITIVEZERO;
    Constraint<Number> NEGATIVEZERO = NumericConstraint.NEGATIVEZERO;
    Constraint<Number> NONZERO      = NumericConstraint.NONZERO;
    Constraint<Number> ATLEASTONE   = NumericConstraint.ATLEASTONE;
    Constraint<Number> UNITINTERVAL = DualBound.UNITINTERVAL;
    // @formatter:on
}
