# Lane level

To contain lane information there is a specific type of link in the class `CrossSectionLink`. It divides a link laterally, i.e. a cross-section, by `CrossSectionElement`s. Cross-section elements have an offset and width, and contain their own center line. Both offset and width are defined in a `CrossSectionSlice` that is present at a given longitudinal location along the cross-section element. At the least there is one at the start and one at the end, but width and offset can vary over the length of the cross-section element.

<pre>
OTS Link
&lfloor; <b>Cross section link</b>
  &lfloor; {Cross section element}
    &lfloor; Id
    &lfloor; {Cross section slice}
      &lfloor; Width
      &lfloor; Offset
</pre>

Cross-section elements come in two main forms: lanes and stripes. Behavior is undefined if cross-section elements cross as their offsets switch lateral order. Similar to links, lanes have a `LaneType`, which is hierarchical and defines which GTU types are allowed in which directions. Consistency with the link type is not checked. The class `Lane` has several methods to obtain information on both laterally and longitudinally connected lanes, including both physical and legal lane change possibility. Legal lane change possibility is determined by assessing the type of `Stripe` (if any) that has a lateral offset between that of the considered lanes. Whether lanes are longitudinally or laterally connected is determined spatially with margins allowable between endpoints. Lanes may overlap in which case they are considered to be adjacent.

<pre>
OTS Link
&lfloor; Cross section link
  &lfloor; <b>Lane</b>
    &lfloor; Lane type
    &lfloor; {GTUs}
</pre>
