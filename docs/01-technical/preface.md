# Preface

This manual is intended for users of Open Traffic Sim (OTS) as a means to get started in both coding and using OTS, as well as for a deeper understanding of how OTS works internally. Using OTS depends on the comprehension of OTS, which can be hard to obtain just from the source code. This manual will functionally describe many of the important internal mechanisms of OTS, and give some code examples. Components in OTS are presented using a functional notation as in the box below. The &lfloor; symbol has no strict meaning, i.e. it does not specify inheritance, a java attribute, etc. Rather, it’s a generic functional indication of intuitive relations between elements. This allows the description to take some shortcuts while the code is more complex. This level of understanding is sufficient for using the component. For detailed understanding one can always look at the source code. With the explanation of this manual, as well as the comments and Javadoc provided with the source code, the code should be self-explanatory. 

<pre>
<b>Name of component</b>
&lfloor; Name; an element (function, property or sub-component) of the component
&lfloor; <i>Name</i>; italics indicate an optional element, example value, or example implementation. 
  &lfloor; element of the above sub-component
&lfloor; {Name}; element of which there are multiple
  &lfloor; element of each of the above sub-components
&lfloor; Name (Type) single element of type Type
&lfloor; Name {Type}; single element which contains multiple objects of type Type, for example as an array, set or map
</pre>

Java classes, methods and property names are formatted as `code`. For methods, (…) indicates ‘with some input’, which may or may not involve multiple equally-named methods with different input.

!!! Note
    Note that this is the manual of OpenTrafficSim version 1. New developments are carried out in OpenTrafficSim version 2, which can be found at [GitHub opentrafficsim2](https://github.com/averbraeck/opentrafficsim2). The manual is at [ReadTheDocs opentrafficsim2](https://opentrafficsim2.readthedocs.io).