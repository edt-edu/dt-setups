package org.example.api;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

// This mixin adds polymorphic type handling to SubmodelElement.
// It instructs Jackson to look for the "$type" property in JSON.
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "$type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty.class, name = "Property"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty.class, name = "MultiLanguageProperty"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange.class, name = "Range"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob.class, name = "Blob"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile.class, name = "File"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation.class, name = "Operation"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity.class, name = "Entity"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection.class, name = "SubmodelElementCollection"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBasicEventElement.class, name = "BasicEvent"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement.class, name = "ReferenceElement"),
        @JsonSubTypes.Type(value = org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultCapability.class, name = "Capability")
})
public abstract class SubmodelElementMixin implements SubmodelElement {
    // No methods needed; this mixin only supplies annotations.
}
