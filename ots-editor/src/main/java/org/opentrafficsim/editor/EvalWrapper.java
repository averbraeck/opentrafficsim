package org.opentrafficsim.editor;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;
import org.opentrafficsim.road.network.factory.xml.CircularDependencyException;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser.ParameterWrapper;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser.ScenariosWrapper;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;

/**
 * Wraps an evaluator for the editor. Any editor component that has content that depends on evaluation, may listen to this
 * object via the editor and be notified of any change. In particular, changes involve changes in the input parameters. This
 * wrapper makes sure that it returns an evaluator based on the current input parameters and selected scenario in the editor.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EvalWrapper extends AbstractNodeDecoratorRemove
{

    /** */
    private static final long serialVersionUID = 20231005L;

    /** Mask of full class names where type adapters are to be found, depending on node name for %s. */
    private static final String ADAPTER_MASK = "org.opentrafficsim.xml.bindings.%sAdapter";

    /** Whether the evaluator is dirty, i.e. a parameter was added, removed, or changed. */
    private boolean dirty = true;

    /** Scenario for which the most recent evaluator was returned. */
    private ScenarioWrapper lastScenario;

    /** Last valid evaluator. */
    private Eval eval;

    /** List of parameter wrappers for default parameters. */
    private final List<ParameterWrapper> defaultParamaters = new ArrayList<>();

    /** List of parameter wrappers per scenario tree node. */
    private final Map<XsdTreeNode, List<ParameterWrapper>> scenarioParameters = new LinkedHashMap<>();

    /** Parameter wrapper per parameter tree node. */
    private final Map<XsdTreeNode, ParameterWrapper> parameterMap = new LinkedHashMap<>();

    /** Listeners for a dirt evaluator. */
    private final Set<EvalListener> listeners = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     */
    public EvalWrapper(final OtsEditor editor)
    {
        super(editor);
    }

    /**
     * Returns expression evaluator.
     * @param scenario ScenarioWrapper; selected scenario (of type as listed in dropdown menu).
     * @return Eval; expression evaluator.
     * @throws CircularDependencyException when input parameters have a circular dependency.
     */
    public Eval getEval(final ScenarioWrapper scenario) throws CircularDependencyException
    {
        boolean becomesDirty = this.dirty || !Objects.equals(this.lastScenario, scenario);
        if (becomesDirty)
        {
            this.lastScenario = scenario;
            if (scenario == null)
            {
                this.eval = new Eval();
            }
            else
            {
                this.eval = ScenarioParser.parseInputParameters(new ScenariosWrapper()
                {
                    /** {@inheritDoc} */
                    @Override
                    public Iterable<ParameterWrapper> getDefaultInputParameters()
                    {
                        return EvalWrapper.this.defaultParamaters;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Iterable<ParameterWrapper> getScenarioInputParameters()
                    {
                        return EvalWrapper.this.scenarioParameters.get(scenario.getScenarioNode());
                    }
                });
            }
            this.dirty = false;
        }
        if (becomesDirty)
        {
            this.listeners.forEach((listener) -> listener.evalChanged());
        }
        return this.eval;
    }

    /**
     * Returns the last evaluator that was valid, i.e. did not have a circular dependency between input parameters.
     * @return Eval; last valid evaluator.
     */
    public Eval getLastValidEval()
    {
        if (this.eval == null)
        {
            return new Eval();
        }
        return this.eval;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.SCENARIO))
        {
            this.scenarioParameters.put(node, new ArrayList<>());
            setDirty();
        }
        else if (node.getPathString().equals(XsdPaths.INPUT_PARAMETERS)
                || node.getPathString().equals(XsdPaths.DEFAULT_INPUT_PARAMETERS))
        {
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        else if ((node.getPathString().startsWith(XsdPaths.INPUT_PARAMETERS + ".")
                || node.getPathString().startsWith(XsdPaths.DEFAULT_INPUT_PARAMETERS + "."))
                && !node.getNodeName().equals("xsd:choice"))
        {
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.addListener(this, XsdTreeNode.VALUE_CHANGED);
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            registerParameter(node); // node may be valid when its created as part of an undo, so we need to register it if so
            setDirty();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.SCENARIO))
        {
            this.scenarioParameters.remove(node);
            setDirty();
        }
        else if (node.getPathString().equals(XsdPaths.INPUT_PARAMETERS)
                || node.getPathString().equals(XsdPaths.DEFAULT_INPUT_PARAMETERS))
        {
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        else if (node.getPathString().startsWith(XsdPaths.DEFAULT_INPUT_PARAMETERS + "."))
        {
            this.defaultParamaters.remove(this.parameterMap.remove(node));
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            setDirty();
        }
        else if (node.getPathString().startsWith(XsdPaths.INPUT_PARAMETERS + "."))
        {
            this.scenarioParameters.forEach((s, list) -> list.remove(this.parameterMap.remove(node)));
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            setDirty();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED) || event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            registerParameter(node);
            setDirty();
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            boolean activated = (boolean) content[1];
            if (node.getPathString().equals(XsdPaths.INPUT_PARAMETERS)
                    || node.getPathString().equals(XsdPaths.DEFAULT_INPUT_PARAMETERS))
            {
                if (activated)
                {
                    node.getChildren().forEach((child) -> notifyCreated(child));
                }
                else
                {
                    node.getChildren().forEach((child) -> notifyRemoved(child));
                }
            }
            else if ((node.getPathString().startsWith(XsdPaths.INPUT_PARAMETERS + ".")
                    || node.getPathString().startsWith(XsdPaths.DEFAULT_INPUT_PARAMETERS + "."))
                    && !node.getNodeName().equals("xsd:choice"))
            {
                if (activated)
                {
                    notifyCreated(node);
                }
                else
                {
                    notifyRemoved(node);
                }
            }
        }
        else
        {
            super.notify(event);
        }
    }

    /**
     * Register the given node as a parameter.
     * @param node XsdTreeNode; node (default or scenario input parameter).
     */
    private void registerParameter(final XsdTreeNode node)
    {
        if (node.getPathString().startsWith(XsdPaths.DEFAULT_INPUT_PARAMETERS + "."))
        {
            this.defaultParamaters.remove(this.parameterMap.remove(node));
            if (node.isValid())
            {
                ParameterWrapper parameter = wrap(node);
                this.parameterMap.put(node, parameter);
                this.defaultParamaters.add(parameter);
            }
        }
        else if (node.getPathString().startsWith(XsdPaths.INPUT_PARAMETERS + "."))
        {
            this.scenarioParameters.forEach((s, list) -> list.remove(this.parameterMap.remove(node)));
            if (node.isValid())
            {
                ParameterWrapper parameter = wrap(node);
                this.parameterMap.put(node, parameter);
                XsdTreeNode scenarioNode = node.getParent().getParent();
                if (scenarioNode != null)
                {
                    this.scenarioParameters.get(scenarioNode).add(parameter);
                }
            }
        }
    }

    /**
     * Sets the evaluator as being dirty, i.e. some input parameter was added, removed or changed. All listeners are notified.
     */
    private void setDirty()
    {
        this.dirty = true;
        this.listeners.forEach((listener) -> listener.evalChanged());
    }

    /**
     * Adds listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener EvalListener; listener.
     */
    public void addListener(final EvalListener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Removes listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener EvalListener; listener.
     */
    public void removeListener(final EvalListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Parameter representation of a node suitable for parsing an {@code Eval}.
     * @param node XsdTreeNode; node, must be a default or scenario input parameter node.
     * @return ParameterWrapper; parameter representation of a node suitable for parsing an {@code Eval}.
     */
    private ParameterWrapper wrap(final XsdTreeNode node)
    {
        try
        {
            Class<?> clazz = Class.forName(String.format(ADAPTER_MASK, node.getNodeName()));
            Constructor<?> constructor = ClassUtil.resolveConstructor(clazz, new Object[0]);
            ExpressionAdapter<?, ?> adapter = (ExpressionAdapter<?, ?>) constructor.newInstance();
            return new ParameterWrapper(node.getId(), adapter.unmarshal(node.getValue()));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to wrap node " + node + " as a parameter for Eval.");
        }
    }

    /**
     * Interface for listeners that need to know when evaluation results may have changed.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    @FunctionalInterface
    public interface EvalListener
    {
        /**
         * Notifies the listener that evaluation results may have changed.
         */
        public void evalChanged();
    }

}
