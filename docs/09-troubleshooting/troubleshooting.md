# Troubleshooting

This chapter discusses some common issues that occur when developing or using OTS.

1. _Eclipse complains about a missing java project / class._<br/>
When Eclipse is unable to find a specific class it will give an error stating ‘X cannot be resolved to a type’. The first thing you want to make sure is to organize imports (Ctrl + Shift + O). Especially in the case you have downloaded projects from subversion, it may be the case that you haven’t yet downloaded a project on which the downloaded project depends. Most important project dependencies are given in [Figure 2.2](../02-model-structure/java#eclipse).

2. _During simulation I get a missing parameter exception._<br/>
In section [Setting default parameters in factories](../06-behavior/parameters#setting-default-parameters-in-factories) it is explained how the strategical planner factory and all its underlying `ModelComponentFactory`’s have the task of supplying the (default) parameters that the specific model part uses. This occurs during vehicle generation. It is not a water tight system and it may be that some parameter that is used, is never set and thus not available in simulation. The most likely solution is to add the parameter in the lowest-level factory for GTU generation possible. For example, return the parameter in `getParameters()` of a car-following model factory, for the accompanying car-following model that is returned by the factory. However, parameters should only be set if they are always used by the model component. If this is not a given, an alternative is to set the parameter using the `ParameterFactory` which is part of a strategical planner factory.

3. _NullPointerException: Missing model component._<br/>
Starting from the strategical planner downward there is a structure of model components. This structure is explained in section [GTU characteristics generator](../04-demand/gtu-characteristics.md) and detailed out in the chapter [Behavioral models](../06-behavior/introduction.md). The structure has optional components, and one component may or may not have further sub components. When you run in to an exception, likely a `NullPointerException`, as such a component is not defined, the factories that generate GTU characteristics for vehicle generation are likely improperly set up.

4. _Persistent inexplicable errors in Eclipse._<br/>
OTS is a combination of many java projects that are coupled through maven. Sometimes these connections get corrupted causing Eclipse to be unable to make important connections between projects and inheriting classes. If likely causes have been checked it may be required to clean projects using ‘Project’ > ‘Clean…’.
