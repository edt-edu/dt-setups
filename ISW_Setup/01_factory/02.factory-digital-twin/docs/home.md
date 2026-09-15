## Digital-Twin Architecture V2 Documentation

Welcome to the documentation for the Digital-Twin V2 architecture. This documentation serves as a central resource to understand the DT-V2 software architecture. DTs, in this context, refer to sophisticated software systems employed for the representation, monitoring, and control of Cyber-Physical-Systems. 

The overarching goal of the project is to develop a versatile framework enabling bidirectional data synchronization across diverse models and systems. This framework is designed to be highly reusable and flexible, providing a generalized solution for data synchronization within a wide range of contexts.

Look [here](Get Started) on how to get started.

***

### Project Overview:

The architectural framework comprises four principal components, namely, the Digital Twin Engine (DTE), Model Managers (MM), Gateways (GW), and Services.

``` mermaid 
classDiagram
AbstractGateway <-- DigitalTwinEgine
AbstractModelManager <-- DigitalTwinEgine
AbstractServiceConnection <-- DigitalTwinEgine
ServiceEngineAPI <-- DigitalTwinEgine
DigitalTwinEgine *--> Synchroizer
Synchroizer *--> Mapping
DigitalTwinEgine --> DTEvent
```

The subsequent sections will provide a comprehensive overview of these components. For detailed insights into their implementation, refer to the appended links at the end of each respective section.

---

##### Digital Twin Engine 
The DTE consists of three classes in a compositional relationship: the DTE itself, coupled with the Synchronizer and Mapping classes. Functioning as the project‘s central hub, the DTE class establishes relationships with instances of other classes and functions as the governing body over them.

Learn more about [Digital Twin Engine](Digital Twin Engine).

---

##### Model Managers
Model Managers assume the critical function of establishing connections and facilitating communication with diverse models intended for deployment as representations of Cyber-Physical-Systems (CPS) or analogous entities. Each Model Manager assumes sole responsibility for a specific model, encompassing all associated attributes.

Learn more about [Model Managers](Model Manager).

---

##### Gateways
Gateways serve as pathways to CPSs or other entities that are part of the Digital Twin System. Similar to MMs, they encapsulate attributes; however, in contrast, they are not obliged to encompass the entire spectrum of attributes associated with a CPS. Furthermore, GWs possess the capability to interconnect DTs, thereby enabling composability without prescribing the specific implementation thereof.

Learn more about [Gateway](Gateway).

---

##### Services
Services play a crucial role in delivering functionalities beyond the fundamental task of synchronizing data between GWs and MMs. In this context, Services can seen as micro-services, focusing on a limited set of tasks. By segregating foundational and supplementary functionalities from the core component (the DTE), a notable level of flexibility and reusability is attained.

Learn more about [Services](Services).

---

##### General

Communication between components is currently achieved through an Event system using the Observer pattern. 
Learn more about [Events](Event).

---



[Implementation Examples](Example)

[[User Stories]]