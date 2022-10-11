package io.apicurio.umg.models.concept;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import io.apicurio.umg.beans.Specification;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityModel {

    private Specification spec;
    @Include
    private NamespaceModel namespace;
    @Include
    private String name;
    private EntityModel parent;
    private final Collection<TraitModel> traits = new LinkedHashSet<>();
    private final Map<String, PropertyModel> properties = new LinkedHashMap<>();
    private boolean leaf;

    public String fullyQualifiedName() {
        return namespace.fullName() + "." + name;
    }

    public void addProperty(PropertyModel property) {
        this.properties.put(property.getName(), property);
    }

    public boolean hasProperty(String propertyName) {
        return this.properties.containsKey(propertyName);
    }

}