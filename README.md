# OpenTrafficSim — MiRoVA Research Fork

This repository is a research-oriented fork of [OpenTrafficSim (OTS)](https://opentrafficsim.org), maintained by **Marvin Baumann** at the **Karlsruhe Institute of Technology (KIT)**. It is developed as part of the **MiRoVA** project (*Migration of Road Vehicle Automation*), focusing on the modeling of human driving behavior and its interaction with automated vehicles.

## Purpose of this Fork

The core objective of this fork is to extend the traditional reactive stimulus-response modeling paradigm in microscopic traffic simulation. By integrating a modular tactical architecture, this framework enables driver agents to perform explicit **tactical maneuver planning**.

Key research goals include:
* Modeling structured, multi-step maneuvers using Finite State Machines (FSM).
* Decoupling tactical intentions (cognition) from operational control (execution).
* Representing human-like behavioral adaptations and continuity in decision-making.

## The MiRoVA Architecture

The simulation logic extends the OTS tactical layer into a modular four-layer cognitive loop. This structure ensures a clear separation between perception, internal motivation, decision-making, and physical execution.

| Layer             | Component         | Responsibility                                                                            |
| :---------------- | :---------------- | :---------------------------------------------------------------------------------------- |
| **1: Perception** | `ContextManager`  | Filters raw simulation data into semantic contexts (Ego, Neighbors, Infrastructure).      |
| **2: Cognition**  | `KnowledgeChunk`  | Evaluates the environment and computes physical or dimensionless desires/motivations.     |
| **3: Decision**   | `PatternSelector` | Selects the appropriate tactical behavior (Maneuver Pattern) based on aggregated desires. |
| **4: Procedure**  | `ManeuverPattern` | Implements the tactical logic via State Machines and generates the operational plan.      |

## Methodology

### Tactical Maneuver Planning
Instead of re-evaluating behavior purely based on instantaneous stimuli, agents follow structured tactical procedures. This approach allows for consistent behavior during complex interactions, such as merging or lane changes, by maintaining internal maneuver states over time.

### Decoupled Logic
By separating the "Motivation" (Layer 2) from the "Action" (Layer 4), the architecture allows for a more flexible modeling of driver behavior. Different motivations can lead to the same maneuver, and the same maneuver can be executed with varying operational parameters depending on the driver's state.

### OTS
OpenTrafficSim is an open-source traffic simulation framework developed at TU Delft. It supports multi-modal micro-, meso-, and macro-simulation.
* **Official Website**: [opentrafficsim.org](https://opentrafficsim.org)
* **Documentation**: [opentrafficsim.org/docs/latest](https://opentrafficsim.org/docs/latest/)

## Contact

**Marvin Baumann** [marvin.baumann@kit.edu](mailto:marvin.baumann@kit.edu)  
Institute for Transport Studies (IfV)  
Karlsruhe Institute of Technology (KIT)