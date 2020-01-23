package org.opentrafficsim.road.gtu.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Markov Chain functionality using state auto-correlations. Rather than specifying a Transition Matrix, this matrix is
 * calculated from a steady-state and from given auto-correlations. Auto-correlation increases the probability that state S
 * returns after state S. Correlation between states can be captured with grouping several states under a super state. This
 * creates a Transition Matrix within a cell of a Transition Matrix. To the super matrix, this is simply a single
 * state-supplying element, applicable to all previous states that are concerned within the element.<br>
 * <br>
 * This class is oblivious to intensity data of the states, in the sense that it must be provided to draw the next state. This
 * class actually only remembers state auto-correlations. Together with input intensities, a part of the Transition Matrix is
 * calculated on the fly. This flexibility over a fixed Transition Matrix allows 1) dynamic intensities, and 2) a single object
 * of this class to be used for multiple processes in which intensities differ, but correlations are equal. This class is
 * therefore also fairly flexible in terms of which states are concerned. Only states with correlation need to be added. For any
 * state included in the input, for which no correlation is defined, a correlation of 0 is assumed. Reversely, states that are
 * included in the class, but that are not part of the input states, are ignored.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <S> state type
 * @param <I> intensity type
 */
public class MarkovCorrelation<S, I extends Number>
{

    /** Leaf node for each state present in the Markov Chain, including in all sub-groups. */
    private Map<S, FixedState<S, I>> leaves = new LinkedHashMap<>();

    /** Transition Matrix for each super state, i.e. the matrix within which states are put that have the given super state. */
    private Map<S, TransitionMatrix<S, I>> superMatrices = new LinkedHashMap<>();

    /** Matrix in which each state is located. */
    private Map<S, TransitionMatrix<S, I>> containingMatrices = new LinkedHashMap<>();

    /** Root matrix. */
    private TransitionMatrix<S, I> root = new TransitionMatrix<>(null, 0.0); // input not important, it's for sub-groups

    /**
     * Adds a state to the root group of the Markov Chain. The correlation increases the chance that the state will occur after
     * itself. We have: <br>
     * 
     * <pre>
     * p_ii = ss_i + (1 - ss_i) * c_i {eq. 1}
     * </pre>
     * 
     * where,
     * 
     * <pre>
     *   p_ii: the probability state i returns after state i
     *   ss_i: the steady-state (overall mean) probability of state i
     *   c_i:  correlation of state i
     * </pre>
     * 
     * Effective correlations of states depend on correlations of other states as well, so the given correlation is not
     * guaranteed to result. One can easily see this from a system with 2 states: A and B. Suppose B has correlation. In order
     * to maintain the same overall steady-state (occurrence proportion of A and B), it follows that state A also must be seen
     * more frequently to follow itself.
     * 
     * <pre>
     * not correlated:  A B B A A A B A A A B A A B B
     * very correlated: A A A A A A A A A B B B B B B (all B's grouped together, hence all A's grouped together and correlated)
     * </pre>
     * 
     * The effective correlation <i>c_i</i> of any state <i>i</i> can be calculated by reversing equation {@code eq. 1} using
     * for <i>p_ii</i> the effective value after all correlations are applied. The procedure to derive the various probabilities
     * from state <i>i</i> to state <i>j</i> (<i>p_ij</i>) is explained below. The procedure is based on the transition matrix
     * <i>T</i>, in which each value gives the probability that the state changes from <i>i</i> (the row) to state <i>j</i> (the
     * column). Consequently, the values in each row must sum to 1, as each state will be followed by another state.<br>
     * <br>
     * It is important that the transition matrix <i>T</i> results in a steady-state as provided. In particular we have for
     * steady state <i>S</i> that <b><i>S</i>*<i>T</i> &#61; <i>S</i></b> should hold. Suppose we have <i>S</i> &#61; [0.7, 0.2,
     * 0.1] for states A, B and C. Without any correlation this would give the base transition matrix:
     * 
     * <pre>
     *     | p_11  p_12  p_13 |   | 0.70  0.20  0.10 |
     * T = | p_21  p_22  p_23 | = | 0.70  0.20  0.10 | 
     *     | p_31  p_32  p_33 |   | 0.70  0.20  0.10 |
     * </pre>
     * 
     * Our steady-state results as for whatever the previous state was, the steady-state probabilities are applied. Now suppose
     * that state C has a correlation of 0.4. This would give that <i>p_33</i> :&#61; <i>p_33</i> + (1 - <i>p_33</i>) * <i>c</i>
     * &#61; 0.46. With this increased value, the probabilities of row 3 no longer add up to 1. Hence, <i>p_31</i> and
     * <i>p_32</i> should be reduced. However, we require that the same steady-state <i>S</i> is maintained. This will remain
     * the case for as long as <i>T</i> remains a <i>reversible</i> Markov Chain. This means that each state has as much input
     * probability, as it has output probability. A matrix where, except for the values on the diagonal, all column values are
     * equal, is reversible. So the base <i>T</i> without correlation is reversible, and we only need to maintain reversibility.
     * A method to maintain reversibility is to <i>scale symmetric pairs</i>. Hence, if we reduce <i>p_32</i>, we should reduce
     * <i>p_23</i> by the same <i>factor</i>. Forcing row 3 to sum to 1, and scaling <i>p_31</i>, <i>p_13</i>, <i>p_32</i> and
     * <i>p_23</i> by the same factor 0.6 we obtain the third matrix below.
     * 
     * <pre>
     *      | 0.70  0.20  0.10 |    | 0.70  0.20  0.10 |    | 0.70  0.20  0.06 |    | 0.74  0.20  0.06 |
     * T =&gt; | 0.70  0.20  0.10 | =&gt; | 0.70  0.20  0.10 | =&gt; | 0.70  0.20  0.06 | =&gt; | 0.70  0.24  0.06 |
     *      | 0.70  0.20  0.10 |    | 0.70  0.20  0.46 |    | 0.42  0.12  0.46 |    | 0.42  0.12  0.46 |
     * </pre>
     * 
     * As we reduce <i>p_13</i> and <i>p_23</i>, we also reduce the probability sums of rows 1 and 2. These reductions can be
     * compensated by increasing the values on the diagonals, as is done in the fourth matrix. Note that changing the diagonal
     * values does not affect reversibility. For example, 0.7*0.74 + 0.2*0.70 + 0.1*0.42 &#61; 0.7 for the first column.<br>
     * <br>
     * Changing the diagonal values <i>p_11</i> and <i>p_22</i> as the result of correlation for state C, shows that correlation
     * of one state automatically introduces correlation at other states, as should also intuitively occur from the A-B example.
     * The procedure can be started with the base <i>T</i> from steady-state <i>S</i> and can be repeated for each state
     * <i>i</i> with correlation:
     * <ol>
     * <li>Increase <i>p_ii</i> to <i>p_ii</i> + (1-<i>p_ii</i>) * <i>c_i</i>.</li>
     * <li>Reduce <i>p_ij</i> for all <i>j</i> unequal to <i>i</i> such that row <i>i</i> sums to 1, use one factor <i>f</i> for
     * all.</li>
     * <li>Reduce <i>p_ji</i> for all <i>j</i> unequal to <i>i</i> to maintain reversibility. Use factor <i>f</i> again.</li>
     * <li>Set all <i>p_jj</i> for all <i>j</i> unequal to <i>i</i> such that row <i>j</i> sums to 1.</li>
     * </ol>
     * Knowing that each value <i>p_ij</i> gets reduced for correlation of state <i>i</i> and <i>j</i>, and realizing that the
     * reduction factors <i>f</i> equal (1 - <i>c_i</i>) and (1 - <i>c_j</i>) respectively, the effective correlation can be
     * calculated by adding all reductions in a row to the diagonal value <i>p_ii</i> and using {@code eq. 1}.<br>
     * <br>
     * See also "Construction of Transition Matrices of Reversible Markov Chains" by Qian Jiang.
     * @param state S; state
     * @param correlation double; correlation
     * @throws IllegalArgumentException if correlation is not within the range (-1 ... 1), or the state is already defined
     * @throws NullPointerException if state is null
     */
    public synchronized void addState(final S state, final double correlation)
    {
        Throw.whenNull(state, "State may not be null.");
        Throw.when(this.leaves.containsKey(state), IllegalArgumentException.class, "State %s already defined.", state);
        Throw.when(correlation <= -1.0 || correlation >= 1.0, IllegalArgumentException.class,
                "Correlation at root level need to be in the range (-1 ... 1).");
        FixedState<S, I> node = new FixedState<>(state, correlation);
        this.root.addNode(state, node);
        this.containingMatrices.put(state, this.root);
        this.leaves.put(state, node);
    }

    /**
     * Adds a state to the group of the Markov Chain indicated by a super state. If the super state is not yet placed in a
     * sub-group, the sub-group is created. Grouping is useful to let a set of states correlate to any other of the states in
     * the set. For example, after state A, both states A and B can occur with some correlation, while state C is not correlated
     * to states A and B. The same correlation is applied when the previous state was B, as it is also part of the same group.
     * <br>
     * <br>
     * To explain sub-groups, suppose we have the following 3-state matrix in which the super state <i>s_2</i> is located (this
     * can be the root matrix, or any sub-matrix). In this matrix, state <i>s_2</i> is replaced by a matrix <i>S_2</i>.
     * 
     * <pre>
     *       s_1   s_2   s_3             s_1   S_2   s_3
     * s_1 | p_11  p_12  p_13 |    s_1 | p_11  p_12  p_13 |
     * s_2 | p_21  p_22  p_23 | =&gt; S_2 | p_21  p_22  p_23 |
     * s_3 | p_31  p_32  p_33 |    s_3 | p_31  p_32  p_33 |
     * </pre>
     * 
     * From the level of this matrix, nothing changes. Whenever the prior state was any of those inside <i>S_2</i>, row 2 is
     * applied to determine the next state. If the next state is matrix <i>S_2</i>, the state is further specified by
     * <i>S_2</i>. Matrix <i>S_2</i> itself will be:
     * 
     * <pre>
     *       s_2   s_4
     * s_2 | p_22' p_24 |
     * s_4 | p_42  p_44 |
     * </pre>
     * 
     * It will thus result in either state <i>s_2</i> or state <i>s_4</i>. More states can now be added to <i>S_2</i>, using the
     * same super state <i>s_2</i>. In case the prior state was either <i>s_1</i> or <i>s_3</i>, i.e. no state included in the
     * sub-group, the matrix of the sub-group defaults to fractions based on the steady-state only. Correlations are then also
     * ignored.<br>
     * <br>
     * Correlation of <i>s_2</i> is applied to the whole group, and all other states of the group can be seen as sub-types of
     * the group's super state. Correlations as considered inside the group (<i>c'_2</i> and <i>c'_4</i>) are mapped from the
     * range <i>c_2</i> to 1. So, <i>c'_2</i> = 0, meaning that within the group there is no further correlation for the super
     * state. Sub states, who are required to have an equal or larger correlation than the super state of the group, map
     * linearly between <i>c_2</i> and 1.<br>
     * <br>
     * If the super state is only a virtual layer that should not in itself be a valid state of the system, it can simply be
     * excluded from obtaining a new state using {@code getState()}.<br>
     * <br>
     * @param superState S; state of group
     * @param state S; state to add
     * @param correlation double; correlation
     * @throws IllegalArgumentException if correlation is not within the range (0 ... 1), the state is already defined, or
     *             superState is not yet a state
     * @throws NullPointerException if an input is null
     */
    public synchronized void addState(final S superState, final S state, final double correlation)
    {
        Throw.whenNull(superState, "Super-state may not be null.");
        Throw.whenNull(state, "State may not be null.");
        Throw.when(this.leaves.containsKey(state), IllegalArgumentException.class, "State %s already defined.", state);
        Throw.when(correlation < 0.0 || correlation >= 1.0, IllegalArgumentException.class,
                "Correlation at root level need to be in the range (-1 ... 1).");
        if (!this.superMatrices.containsKey(superState))
        {
            // branch the super state in to a matrix
            FixedState<S, I> superOriginal = this.leaves.get(superState);
            // remove original from it's matrix
            TransitionMatrix<S, I> superMatrix = this.containingMatrices.get(superState);
            Throw.when(superMatrix == null, IllegalArgumentException.class, "No state has been defined for super-state %s.",
                    superState);
            superMatrix.removeNode(superState);
            // replace with matrix
            TransitionMatrix<S, I> matrix = new TransitionMatrix<>(superState, superOriginal.getCorrelation());
            superMatrix.addNode(superState, matrix);
            this.superMatrices.put(superState, matrix);
            // add original node to that matrix
            superOriginal.clearCorrelation();
            matrix.addNode(superState, superOriginal);
            this.containingMatrices.put(superState, matrix);
        }
        // add node
        TransitionMatrix<S, I> superMatrix = this.superMatrices.get(superState);
        Throw.when(correlation < superMatrix.getCorrelation(), IllegalArgumentException.class,
                "Sub states in a group can not have a lower correlation than the super state of the group.");
        FixedState<S, I> node =
                new FixedState<>(state, (correlation - superMatrix.getCorrelation()) / (1.0 - superMatrix.getCorrelation()));
        superMatrix.addNode(state, node);
        this.containingMatrices.put(state, superMatrix);
        this.leaves.put(state, node);
        // register state as part of matrix node
        this.root.registerInGroup(superState, state);
        for (TransitionMatrix<S, I> matrix : this.superMatrices.values())
        {
            if (matrix.getState() != superState)
            {
                matrix.registerInGroup(superState, state);
            }
        }
    }

    /**
     * Draws a next state from this Markov Chain process, with predefined state correlations, but dynamic intensities. Any
     * states that are present in the underlying Transition Matrix, but not present in the given states, are ignored. States
     * that are not present in the underlying Transition Matrix, are added to it with a correlation of 0.
     * @param previousState S; previous state
     * @param states S[]; set of states to consider
     * @param steadyState I[]; current steady-state intensities of the states
     * @param stream StreamInterface; to draw random numbers
     * @return S; next state
     * @throws IllegalArgumentException if number of states is not the same as the stead-state length
     * @throws NullPointerException if states, steadyState or stream is null
     */
    public synchronized S drawState(final S previousState, final S[] states, final I[] steadyState,
            final StreamInterface stream)
    {
        Throw.whenNull(states, "States may not be null.");
        Throw.whenNull(steadyState, "Steady-state may not be null.");
        Throw.whenNull(stream, "Stream for random numbers may not be null.");
        Throw.when(states.length != steadyState.length, IllegalArgumentException.class,
                "Number of states should match the length of the steady state.");
        for (FixedState<S, I> node : this.leaves.values())
        {
            node.clearIntensity();
        }
        int n = states.length;
        for (int i = 0; i < n; i++)
        {
            S state = states[i];
            FixedState<S, I> leaf = this.leaves.get(state);
            if (leaf == null)
            {
                addState(state, 0.0);
                leaf = this.leaves.get(state);
            }
            leaf.setIntensity(steadyState[i]);
        }
        return this.root.drawState(previousState, stream);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "MarkovCorrelation [ " + this.root + " ]";
    }

    /**
     * Base class for elements inside a Markov {@code TransitionMatrix}.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> state type
     * @param <I> intensity type
     */
    private abstract static class MarkovNode<S, I extends Number>
    {

        /** State. */
        private final S state;

        /** Correlation. */
        private double correlation;

        /**
         * Constructor.
         * @param state S; state
         * @param correlation double; correlation
         */
        MarkovNode(final S state, final double correlation)
        {
            this.state = state;
            this.correlation = correlation;
        }

        /**
         * Returns the encapsulated state, which is either a fixed state, or the super-state of a group.
         * @return S; encapsulated state, which is either a fixed state, or the super-state of a group
         */
        final S getState()
        {
            return this.state;
        }

        /**
         * Returns the correlation.
         * @return double; correlation
         */
        final double getCorrelation()
        {
            return this.correlation;
        }

        /**
         * Clears the correlation.
         */
        protected final void clearCorrelation()
        {
            this.correlation = 0.0;
        }

        /**
         * Returns the current intensity, used for the Markov Chain process.
         * @return current intensity, used for the Markov Chain process, 0.0 if no intensity was provided
         */
        abstract double getIntensity();

        /**
         * Returns a state from this node, which is either a fixed state, or a randomly drawn state from a sub-group.
         * @param previousState S; previous state
         * @param stream StreamInterface; to draw random numbers
         * @return S; state from this node, which is either a fixed state, or a randomly drawn state from a sub-group
         */
        abstract S drawState(S previousState, StreamInterface stream);

    }

    /**
     * Transition matrix with functionality to return a next state, and to entail a set of fixed states mixed with matrices for
     * sub-groups.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> state type
     * @param <I> intensity type
     */
    private static final class TransitionMatrix<S, I extends Number> extends MarkovNode<S, I>
    {

        /** List of state-defining states, where sub-groups are defined by a set of state. */
        private List<Set<S>> states = new ArrayList<>();

        /** List of nodes (fixed state or sub-group matrix). */
        private List<MarkovNode<S, I>> nodes = new ArrayList<>();

        /**
         * Constructor.
         * @param state S; super state representing the sub-group, or {@code null} for the root matrix.
         * @param correlation double; correlation for the sub-group, or any value for the root matrix.
         */
        TransitionMatrix(final S state, final double correlation)
        {
            super(state, correlation);
        }

        /**
         * Adds a node to the matrix.
         * @param state S; state of the node
         * @param node MarkovNode&lt;S, I&gt;; node
         */
        void addNode(final S state, final MarkovNode<S, I> node)
        {
            Set<S> set = new LinkedHashSet<>();
            set.add(state);
            this.states.add(set);
            this.nodes.add(node);
        }

        /**
         * Registers the state to belong to the group of super-state. This is used such that the matrix knows which previous
         * states to map to the group.
         * @param superState S; super-state of the group
         * @param state S; state inside the group
         */
        void registerInGroup(final S superState, final S state)
        {
            for (Set<S> set : this.states)
            {
                if (set.contains(superState))
                {
                    set.add(state);
                    return;
                }
            }
        }

        /**
         * Removes the node from the matrix, including group registration. This is used to replace a state with a group.
         * @param state S; state to remove
         */
        void removeNode(final S state)
        {
            int i = -1;
            for (int j = 0; j < this.states.size(); j++)
            {
                if (this.states.get(j).contains(state))
                {
                    i = j;
                    break;
                }
            }
            if (i > -1)
            {
                this.states.remove(i);
                this.nodes.remove(i);
            }
        }

        /**
         * Returns a state from this matrix. This is done by calculating the row of the Markov Transition Chain for the given
         * previous state, and using those probabilities to draw an output state.
         * @param previousState S; previous state
         * @param stream StreamInterface; to draw random numbers
         * @return S; state from this matrix
         * @see MarkovCorrelation#addState(Object, double) algorithm for calculating the Transition Matrix
         */
        @Override
        S drawState(final S previousState, final StreamInterface stream)
        {
            // figure out whether this matrix contains the previous state, and if so the correlation factor and row number i
            boolean contains = false;
            int n = this.states.size();
            double intensitySum = 0.0;
            int i = 0;
            double iFactor = 1.0;
            for (int j = 0; j < n; j++)
            {
                intensitySum += this.nodes.get(j).getIntensity();
                if (this.states.get(j).contains(previousState))
                {
                    i = j;
                    contains = true;
                    iFactor = 1.0 - this.nodes.get(i).getCorrelation();
                }
            }
            // gather probabilities and apply correlation factors
            double[] p = new double[n];
            double pSum = 0.0;
            for (int j = 0; j < n; j++)
            {
                if (i != j || !contains)
                {
                    MarkovNode<S, I> node = this.nodes.get(j);
                    double jFactor = 1.0;
                    if (contains)
                    {
                        jFactor = 1.0 - node.getCorrelation();
                    }
                    p[j] = jFactor * iFactor * node.getIntensity() / intensitySum;
                    pSum += p[j];
                }
            }
            // correct to get row sum = 1.0 by changing the diagonal value
            if (contains)
            {
                p[i] = 1.0 - pSum;
            }
            // make probabilities cumulative
            for (int j = 1; j < n; j++)
            {
                p[j] = p[j - 1] + p[j];
            }
            // draw
            double r = stream.nextDouble();
            for (int j = 0; j < n; j++)
            {
                if (r < p[j])
                {
                    return this.nodes.get(j).drawState(previousState, stream);
                }
            }
            throw new RuntimeException("Unexpected error while drawing state from matrix.");
        }

        /**
         * Returns the current intensity, used for the Markov Chain process, as the sum of matrix elements.
         * @return current intensity, used for the Markov Chain process, 0.0 if no intensity was provided
         */
        @Override
        double getIntensity()
        {
            double intensity = 0;
            for (MarkovNode<S, I> node : this.nodes)
            {
                intensity += node.getIntensity();
            }
            return intensity;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            String superType = this.getState() == null ? "" : "(" + this.getState() + ")";
            String statesStr = "";
            String sep = "";
            for (MarkovNode<S, I> node : this.nodes)
            {
                statesStr += sep + node;
                sep = ", ";
            }
            return "T" + superType + "[ " + statesStr + " ]";
        }
    }

    /**
     * Container for a fixed state. Each state is reflected in a single object of this class. They are grouped in matrices,
     * possibly all in the root. Subsets of all states may be grouped in a matrix, but no state is present in more than one
     * matrix.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> state type
     * @param <I> intensity type
     */
    private static final class FixedState<S, I extends Number> extends MarkovNode<S, I>
    {

        /** Intensity. */
        private I intensity;

        /**
         * Constructor.
         * @param state S; state
         * @param correlation double; correlation
         */
        FixedState(final S state, final double correlation)
        {
            super(state, correlation);
        }

        /**
         * Returns the state.
         * @param previousState S; previous state
         * @param stream StreamInterface; to draw random numbers
         * @return state S; the state
         */
        S drawState(final S previousState, final StreamInterface stream)
        {
            return getState();
        }

        /**
         * Sets the current intensity.
         * @param intensity I; intensity
         */
        void setIntensity(final I intensity)
        {
            this.intensity = intensity;
        }

        /**
         * Clears the intensity.
         */
        void clearIntensity()
        {
            this.intensity = null;
        }

        /** {@inheritDoc} */
        @Override
        double getIntensity()
        {
            return this.intensity == null ? 0.0 : this.intensity.doubleValue();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return String.format(Locale.US, "%s(%.2f)", getState(), getCorrelation());
        }
    }

}
