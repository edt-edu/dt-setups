### Model Manager

Similar to the situation with Cyber-Physical Systems (CPS), there may be constraints or preferences against directly incorporating models into the Digital Twin System (DTS). Consequently, a framework is required to enable communication with a diverse range of models while maintaining a level of standardization for seamless integration within the DTE. This framework is realized through the implementation of the Model Manager (MM):

```` mermaid
classDiagram

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

class AbstractModelManager~T~{
modelManagerID String
observer : MutabbleList<IModelObserver>


writeToModel(property : String, value : T)*
readFromModel(property : String)* ~T~ 
getValueAtIndex(index : String, property : String)* Any
getModel()* Any
}


IDataSource <|-- AbstractModelManager
````


To ensure high compatibility within the Digital Twin Engine (DTE), the Model Manager implements the IDatasource Interface, which is the same interface implemented by the Gateway. For a detailed understanding of how the functions of this interface operate, please refer to the explanation provided on the wiki page of the [Gateway](Gateway), as they are already detailed there.

In addition to the `getValue()` and `setValue()` operations from the IDataSource Interface, the Model Manager (MM) introduces the `writeToModel()` and `readToModel()` functions. These functions are necessitated by the fact that the `AbstractModelManager` is a generic class. The use of generics enables the reading and writing of complex objects from and to the models without the need for casting parameters or arguments, as would be the case with `Any`. This enhances type safety and clarity in handling complex data structures within the Model Manager.

Given that the Model Manager is an abstract class within the Digital Twin Architecture, a detailed explanation of the functionality of different functions and their optimal implementation is best illustrated through an example. For further insights, please refer to [here](Example).