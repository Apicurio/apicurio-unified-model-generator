package io.apicurio.umg.pipe.concept;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.apicurio.umg.models.concept.EntityModel;
import io.apicurio.umg.models.concept.PropertyModel;
import io.apicurio.umg.models.concept.TraitModel;
import io.apicurio.umg.pipe.AbstractStage;

public class NormalizePropertiesStage extends AbstractStage {
    @Override
    protected void doProcess() {

        // Normalize the Entity Properties
        List<EntityModel> branchEntities = getState().getConceptIndex().findEntities("").stream().filter(model -> !model.isLeaf()).collect(Collectors.toList());
        int changesMade;
        do {
            changesMade = 0;
            for (EntityModel parentEntity : branchEntities) {
                // Get all direct children of this parent entity.
                Collection<EntityModel> childEntities = getState().findChildEntitiesFor(parentEntity);
                // Get a collection of all properties for the children
                Set<PropertyModel> allProperties = new HashSet<>();
                childEntities.forEach(child -> allProperties.addAll(child.getProperties().values()));

                // Filter the full list of properties - only keep the properties that exist in *all* children.
                List<PropertyModel> propertiesToPullup = allProperties.stream()
                        .filter(property -> childEntities.stream().map(c -> hasProperty(c.getProperties(), property)).reduce(true, (sub, element) -> sub && element))
                        .collect(Collectors.toList());

                // Now pull up each of the properties in the above list
                propertiesToPullup.forEach(property -> {
                    parentEntity.addProperty(property);
                    childEntities.forEach(childEntity -> childEntity.getProperties().remove(property.getName()));
                });

                // Did we find any properties to pull up?  If yes, increment "changes made".  We're going to keep
                // going through our model hierarchy until we've pulled up all the shared properties we can.
                changesMade += propertiesToPullup.size();
            }
        } while (changesMade > 0);

        // Normalize the Trait Properties
        List<TraitModel> branchTraits = getState().getConceptIndex().findTraits("").stream().filter(model -> !model.isLeaf()).collect(Collectors.toList());
        do {
            changesMade = 0;
            for (TraitModel parentTrait : branchTraits) {
                // Get all direct children of this parent trait.
                Collection<TraitModel> childTraits = getState().findChildTraitsFor(parentTrait);
                // Get a collection of all properties for the children
                Set<PropertyModel> allProperties = new HashSet<>();
                childTraits.forEach(child -> allProperties.addAll(child.getProperties().values()));

                // Filter the full list of properties - only keep the properties that exist in *all* children.
                List<PropertyModel> propertiesToPullup = allProperties.stream()
                        .filter(property -> childTraits.stream().map(c -> hasProperty(c.getProperties(), property)).reduce(true, (sub, element) -> sub && element))
                        .collect(Collectors.toList());

                // Now pull up each of the properties in the above list
                propertiesToPullup.forEach(property -> {
                    parentTrait.addProperty(property);
                    childTraits.forEach(childTrait -> childTrait.getProperties().remove(property.getName()));
                });

                // Did we find any properties to pull up?  If yes, increment "changes made".  We're going to keep
                // going through our model hierarchy until we've pulled up all the shared properties we can.
                changesMade += propertiesToPullup.size();
            }
        } while (changesMade > 0);
    }

    /**
     * Checks if the given collection of properties contains the given property.  It must have a property with
     * the same name and the property must have the same type.
     * @param properties
     * @param property
     */
    private boolean hasProperty(Map<String, PropertyModel> properties, PropertyModel property) {
        PropertyModel otherProperty = properties.get(property.getName());
        return otherProperty != null && otherProperty.getType().equals(property.getType());
    }
}
