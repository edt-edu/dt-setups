### Overview 

The fundamental concept behind Gateways (GW) is to establish a standardized framework to seamlessly integrate external components into the Digital Twin System (DTS) while preserving their integral connection to the digital twin system. A prime example is the inclusion of Cyber-Physical Systems (CPS). 

CPSs may originate from diverse manufacturers and exhibit distinct communication interfaces. Hence, a translational layer is imperative, and this is provided by the Gateway implementation.

The necessity for this framework also arises from the inability to directly integrate CPSs into the DTS, while simultaneously maintaining a high degree of reusability and flexibility. Consequently, the framework serves a function akin to that of Model Managers, acting as an intermediary layer to facilitate the integration of CPSs within the DTS.

It's crucial to emphasize that Gateways maintain a distinction from services, despite the apparent similarity in the definition. The differentiation lies in the type of external components they introduce to the system. Services contribute additional functionalities that extend beyond the fundamental task of data synchronization. In contrast, Gateways specifically enable this synchronization functionality. This dissimilarity in definition manifests itself when examining the types of events thrown and the actions observers can undertake with them.




``` mermaid
classDiagram
class AbstractGateway{
propertyIDs: Set~String~
gatewayID : String
observer : MutableList~IGatewayObserver~

listen()
sendError(error: DTError)
jsonStringToMap(json: String) Map~String,Any~ 

}

class IDataSource{
propertyIDs: Set~String~
lastPropertyUpdateMap : Map~String,Long~

getValue(id: String) Any
getValues(idSet: Set~String~) Map~StringAny~
getAllValues() Map~StringAny~
setValue(id: String, value: Any)
setValues(values: Map~String,Any~)
getLastUpdate(id: String): String
responsibleForID(id: String): Boolean
}
IDataSource <|-- AbstractGateway

```


It's important to acknowledge the inherent ambiguity in the definition of a Gateway, as it opens up the connectivity scope beyond Cyber-Physical Systems (CPSs) to encompass various other entities within the digital twin system. This inclusive approach facilitates composability between different Digital Twin Systems (DTS), allowing for flexible composition methods. One can choose to construct a Master DTS that governs and coordinates other DTSs or opt for interconnecting them, offering a versatile approach to system composition.

Most of the functions within the Gateway are inherited from the IDataSource Interface, a interface also implemented by the ModelManager class. This design allows for a generalized approach to instances within the Digital Twin Engine (DTE) that can be read from and written to.

The `getValue()` function straightforwardly returns the value of the given property. The `getValues()` function, when provided with a set of desired properties, returns a map of the requested property/value pairs. Internally, the `getValue()` function is executed for each entry in the set. The same principle applies to the set functions.

The `getAllValues` function requires no arguments and simply returns a map encompassing all property/value pairs. Lastly, the `propertyUpdateMap` is employed in the  [Digital Twin Engine](Digital Twin Engine) to facilitate bidirectional synchronization.

The `listen` function of the Gateway is arguably its most crucial, as it empowers nearly half of its functionality. The Gateway isn't merely a framework facilitating read and write operations to Cyber-Physical Systems (CPSs) or other Digital Twins (DTs); it can also receive incoming transmissions from them and does the first step in handling them. The implementation of this capability is embedded within the listen function, utilizing the event system. For more information on events, please refer to [here](Event).

It is from paramount importance to keep in mind while implementing a Gateway or Model Manager, that no property id can be used more than once, even when corresponding to the same property of a CPS.


Given that the Gateway is an abstract class within the Digital Twin Architecture, a detailed explanation of the functionality of different functions and their optimal implementation is best illustrated through an example. For further insights, please refer to [here](Example).