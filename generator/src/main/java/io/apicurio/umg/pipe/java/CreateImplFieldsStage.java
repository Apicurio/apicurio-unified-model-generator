package io.apicurio.umg.pipe.java;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import io.apicurio.umg.models.concept.EntityModel;
import io.apicurio.umg.models.concept.PropertyModel;
import io.apicurio.umg.models.concept.PropertyModelWithOrigin;
import io.apicurio.umg.models.concept.PropertyType;

/**
 * Creates the fields for each entity implementation.  This is done by iterating over all leaf entities
 * and collecting all the properties for it.  One field is created for each property.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateImplFieldsStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getConceptIndex().findEntities("").stream().filter(entity -> entity.isLeaf()).forEach(entity -> {
            createEntityImplFields(entity);
        });
    }

    private void createEntityImplFields(EntityModel entity) {
        JavaClassSource javaEntityImpl = lookupJavaEntityImpl(entity);
        Collection<PropertyModelWithOrigin> allProperties = getState().getConceptIndex().getAllEntityProperties(entity);

        allProperties.forEach(property -> {
            createEntityImplField(javaEntityImpl, property);
        });
    }

    private void createEntityImplField(JavaClassSource javaEntityImpl, PropertyModelWithOrigin propertyWithOrigin) {
        PropertyModel property = propertyWithOrigin.getProperty();

        boolean isStarProperty = false;
        if (isStarProperty(property)) {
            PropertyType mappedType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            property = PropertyModel.builder().name("_items").type(mappedType).build();
            isStarProperty = true;
        } else if (isRegexProperty(property) && (isEntity(property) || isPrimitive(property))) {
            if (property.getCollection() == null) {
                error("Regex property defined without a collection name: " + javaEntityImpl.getCanonicalName() + "::" + property);
                return;
            }
            PropertyType collectionPropertyType = PropertyType.builder()
                    .nested(Collections.singleton(property.getType()))
                    .map(true)
                    .build();
            property = PropertyModel.builder().name(property.getCollection()).type(collectionPropertyType).build();
        }

        String fieldName = getFieldName(property);
        String fieldType = "String";

        if (fieldName == null) {
            warn("Could not figure out field name for property: " + property);
            return;
        }

        if (isPrimitive(property)) {
            Class<?> pType = primitiveTypeToClass(property.getType());
            javaEntityImpl.addImport(pType);
            fieldType = pType.getSimpleName();
        } else if (isEntity(property)) {
            JavaInterfaceSource javaTypeEntity = resolveJavaEntity(propertyWithOrigin.getOrigin().getNamespace().fullName(), property.getType().getSimpleType());
            javaEntityImpl.addImport(javaTypeEntity);
            fieldType = javaTypeEntity.getName();
        } else if (isPrimitiveList(property)) {
            Class<?> pType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntityImpl.addImport(pType);
            javaEntityImpl.addImport(List.class);
            fieldType = "List<" + pType.getSimpleName() + ">";
        } else if (isPrimitiveMap(property)) {
            Class<?> pType = primitiveTypeToClass(property.getType().getNested().iterator().next());
            javaEntityImpl.addImport(pType);
            javaEntityImpl.addImport(Map.class);
            fieldType = "Map<String, " + pType.getSimpleName() + ">";
        } else if (isEntityList(property)) {
            PropertyType listType = property.getType().getNested().iterator().next();
            JavaInterfaceSource javaTypeEntity = resolveJavaEntity(propertyWithOrigin.getOrigin().getNamespace().fullName(), listType.getSimpleType());
            javaEntityImpl.addImport(javaTypeEntity);
            javaEntityImpl.addImport(List.class);
            fieldType = "List<" + javaTypeEntity.getName() + ">";
        } else if (isEntityMap(property)) {
            PropertyType mapType = property.getType().getNested().iterator().next();
            JavaInterfaceSource javaTypeEntity = resolveJavaEntity(propertyWithOrigin.getOrigin().getNamespace().fullName(), mapType.getSimpleType());
            javaEntityImpl.addImport(javaTypeEntity);
            javaEntityImpl.addImport(Map.class);
            fieldType = "Map<String, " + javaTypeEntity.getName() + ">";
        } else if (property.getType().isMap() && property.getType().getNested().iterator().next().isList() &&
                property.getType().getNested().iterator().next().getNested().iterator().next().isPrimitiveType()) {
            // Handle map of list of primitives.
            PropertyType listType = property.getType().getNested().iterator().next().getNested().iterator().next();
            Class<?> pType = primitiveTypeToClass(listType);
            javaEntityImpl.addImport(pType);
            javaEntityImpl.addImport(List.class);
            javaEntityImpl.addImport(Map.class);
            fieldType = "Map<String, List<" + pType.getSimpleName() + ">>";
        } else {
            warn("Field not created - property type not supported: " + property);
            return;
        }

        FieldSource<JavaClassSource> field = javaEntityImpl.addField().setPrivate().setType(fieldType).setName(fieldName);
        if (isStarProperty) {
            javaEntityImpl.addImport(LinkedHashMap.class);
            field.setLiteralInitializer("new LinkedHashMap<>()");
        }
    }
}