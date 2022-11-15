package io.apicurio.umg.pipe.java;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.apicurio.umg.beans.SpecificationVersion;
import io.apicurio.umg.logging.Logger;
import io.apicurio.umg.models.concept.EntityModel;
import io.apicurio.umg.models.concept.PropertyModel;
import io.apicurio.umg.models.concept.PropertyType;
import io.apicurio.umg.pipe.java.method.BodyBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Creates the i/o writer classes.  There is a bespoke writer for each specification
 * version.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateWritersStage extends AbstractJavaStage {

    @Override
    protected void doProcess() {
        getState().getSpecIndex().getAllSpecificationVersions().forEach(specVersion -> {
            String writerPackageName = specVersion.getNamespace() + ".io";
            String writerClassName = specVersion.getPrefix() + "ModelWriter";

            // Create java source code for the writer
            JavaClassSource writerClassSource = Roaster.create(JavaClassSource.class)
                    .setPackage(writerPackageName)
                    .setName(writerClassName)
                    .setPublic();
            writerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "JsonUtil");
            writerClassSource.addImport(getState().getConfig().getRootNamespace() + ".util." + "WriterUtil");

            // Create the writeXYZ methods - one for each entity
            createWriteMethods(specVersion, writerClassSource);

            getState().getJavaIndex().index(writerClassSource);
        });
    }

    /**
     * Creates a "write" method for each entity in the spec version.
     *
     * @param specVersion
     * @param writerClassSource
     */
    private void createWriteMethods(SpecificationVersion specVersion, JavaClassSource writerClassSource) {
        specVersion.getEntities().forEach(entity -> {
            EntityModel entityModel = getState().getConceptIndex().lookupEntity(specVersion.getNamespace() + "." + entity.getName());
            if (entityModel == null) {
                Logger.warn("[CreateWritersStage] Entity model not found for entity: " + entity);
            } else {
                createWriteMethodFor(specVersion, writerClassSource, entityModel);
            }
        });
    }

    /**
     * Creates a single "writeXyx" method for the given entity.
     *
     * @param specVersion
     * @param writerClassSource
     * @param entityModel
     */
    private void createWriteMethodFor(SpecificationVersion specVersion, JavaClassSource writerClassSource, EntityModel entityModel) {
        String writeMethodName = writeMethodName(entityModel);

        JavaInterfaceSource javaEntityModel = getState().getJavaIndex().lookupInterface(getEntityInterfaceFQN(entityModel));
        if (javaEntityModel == null) {
            Logger.warn("[CreateWritersStage] Java entity not found for: " + entityModel.fullyQualifiedName());
            return;
        }

        writerClassSource.addImport(javaEntityModel.getQualifiedName());
        addImportTo(ObjectNode.class, writerClassSource);
        MethodSource<JavaClassSource> methodSource = writerClassSource.addMethod()
                .setName(writeMethodName)
                .setReturnTypeVoid()
                .setPublic();
        methodSource.addParameter(javaEntityModel.getName(), "node");
        methodSource.addParameter(ObjectNode.class.getSimpleName(), "json");

        // Now create the body content for the writer.
        BodyBuilder body = new BodyBuilder();
        // Write each property of the entity
        Collection<PropertyModel> allProperties = getState().getConceptIndex().getAllEntityProperties(entityModel);
        allProperties.forEach(property -> {
            createWritePropertyCode(body, property, entityModel, javaEntityModel, writerClassSource);
        });
        // Write "extra" properties
        createWriteExtraPropertiesCode(body);

        methodSource.setBody(body.toString());
    }

    /**
     * Generates the right java code for writing a single property of an entity.
     *
     * @param body
     * @param property
     * @param javaEntity
     * @param javaEntity
     * @param writerClassSource
     */
    private void createWritePropertyCode(BodyBuilder body, PropertyModel property, EntityModel entityModel,
            JavaInterfaceSource javaEntity, JavaClassSource writerClassSource) {
        CreateWriteProperty crp = new CreateWriteProperty(property, entityModel, javaEntity, writerClassSource);
        body.clearContext();
        crp.writeTo(body);
    }

    /**
     * Generates code that will write the extra properties from the model to the JSON output.
     *
     * @param body
     */
    private void createWriteExtraPropertiesCode(BodyBuilder body) {
        body.append("WriterUtil.writeExtraProperties(node, json);");
    }

    private static String writeMethodName(EntityModel entityModel) {
        return writeMethodName(entityModel.getName());
    }

    private static String writeMethodName(String entityName) {
        return "write" + StringUtils.capitalize(entityName);
    }

    @Data
    @AllArgsConstructor
    private class CreateWriteProperty {
        PropertyModel property;
        EntityModel entityModel;
        JavaInterfaceSource javaEntityModel;
        JavaClassSource writerClassSource;

        /**
         * Generates code to write a property from a JSON node into the data model.
         *
         * @param body
         */
        public void writeTo(BodyBuilder body) {
            if ("*".equals(property.getName())) {
                handleStarProperty(body);
            } else if (property.getName().startsWith("/")) {
                handleRegexProperty(body);
            } else if (property.getType().isEntityType()) {
                handleEntityProperty(body);
            } else if (property.getType().isPrimitiveType()) {
                handlePrimitiveTypeProperty(body);
            } else if (property.getType().isList()) {
                handleListProperty(body);
            } else if (property.getType().isMap()) {
                handleMapProperty(body);
            } else if (property.getType().isUnion()) {
                handleUnionProperty(body);
            } else {
                Logger.warn("[CreateWritersStage] Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
            }
        }

        private void handleStarProperty(BodyBuilder body) {
            if (property.getType().isEntityType()) {
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
                EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(entityTypeName);
                if (propertyTypeEntity == null) {
                    Logger.warn("[CreateWritersStage] STAR Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type: " + property.getType());
                    return;
                }
                addImportTo(List.class, writerClassSource);

                body.addContext("writeMethodName", writeMethodName(propertyTypeEntity));

                body.append("{");
                body.append("    List<String> propertyNames = node.getItemNames();");
                body.append("    propertyNames.forEach(propertyName -> {");
                body.append("        ObjectNode object = JsonUtil.objectNode();");
                body.append("        this.${writeMethodName}(node.getItem(propertyName), object);");
                body.append("        JsonUtil.setObjectProperty(json, propertyName, object);");
                body.append("    });");
                body.append("}");
            } else if (property.getType().isPrimitiveType() ||
                    (property.getType().isList() && property.getType().getNested().iterator().next().isPrimitiveType()) ||
                    (property.getType().isMap() && property.getType().getNested().iterator().next().isPrimitiveType())) {
                addImportTo(List.class, writerClassSource);

                body.addContext("valueType", determineValueType(property.getType()));
                body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getType()));

                body.append("{");
                body.append("    List<String> propertyNames = node.getItemNames();");
                body.append("    propertyNames.forEach(propertyName -> {");
                body.append("        ${valueType} value = node.getItem(propertyName);");
                body.append("        JsonUtil.${setPropertyMethodName}(json, propertyName, value);");
                body.append("    });");
                body.append("}");
            } else {
                Logger.warn("[CreateWritersStage] STAR Entity property '" + property.getName() + "' not written (unhandled) for entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
            }
        }

        private void handleRegexProperty(BodyBuilder body) {
            if (property.getType().isEntityType()) {
                String entityTypeName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
                EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(entityTypeName);
                if (propertyTypeEntity == null) {
                    Logger.warn("[CreateWritersStage] REGEX Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type: " + property.getType());
                    return;
                }
                JavaInterfaceSource entityTypeJavaModel = getState().getJavaIndex().lookupInterface(getEntityInterfaceFQN(propertyTypeEntity));
                if (entityTypeJavaModel == null) {
                    Logger.warn("[CreateWritersStage] REGEX Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type is entity but not found in JAVA index: " + property.getType());
                    return;
                }
                addImportTo(Map.class, writerClassSource);
                addImportTo(entityTypeJavaModel, writerClassSource);

                body.addContext("mapValueJavaType", entityTypeJavaModel.getName());
                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("writeMethodName", writeMethodName(propertyTypeEntity));

                body.append("{");
                body.append("    Map<String, ${mapValueJavaType}> models = node.${getterMethodName}();");
                body.append("    if (models != null) {");
                body.append("        models.keySet().forEach(propertyName -> {");
                body.append("            ObjectNode object = JsonUtil.objectNode();");
                body.append("            this.${writeMethodName}(models.get(propertyName), object);");
                body.append("            JsonUtil.setObjectProperty(json, propertyName, object);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else if (property.getType().isPrimitiveType() ||
                    (property.getType().isList() && property.getType().getNested().iterator().next().isPrimitiveType()) ||
                    (property.getType().isMap() && property.getType().getNested().iterator().next().isPrimitiveType())) {
                addImportTo(List.class, writerClassSource);

                body.addContext("valueType", determineValueType(property.getType()));
                body.addContext("getterMethodName", getterMethodName(property));
                body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getType()));

                body.append("{");
                body.append("    Map<String, ${valueType}> values = node.${getterMethodName}();");
                body.append("    if (values != null) {");
                body.append("        values.keySet().forEach(propertyName -> {");
                body.append("            ObjectNode object = JsonUtil.objectNode();");
                body.append("            ${valueType} value = values.get(propertyName);");
                body.append("            JsonUtil.${setPropertyMethodName}(json, propertyName, value);");
                body.append("        });");
                body.append("    }");
                body.append("}");
            } else {
                Logger.warn("[CreateWritersStage] REGEX Entity property '" + property.getName() + "' not written (unhandled) for entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
            }
        }

        private void handleEntityProperty(BodyBuilder body) {
            String propertyTypeEntityName = entityModel.getNamespace().fullName() + "." + property.getType().getSimpleType();
            EntityModel propertyTypeEntity = getState().getConceptIndex().lookupEntity(propertyTypeEntityName);
            if (propertyTypeEntity == null) {
                Logger.warn("[CreateWritersStage] Property entity type not found for property: '" + property.getName() + "' of entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
                return;
            }

            body.addContext("propertyName", property.getName());
            body.addContext("getterMethodName", getterMethodName(property));
            body.addContext("writeMethodName", writeMethodName(propertyTypeEntity));

            body.append("{");
            body.append("    ObjectNode object = JsonUtil.objectNode();");
            body.append("    this.${writeMethodName}(node.${getterMethodName}(), object);");
            body.append("    JsonUtil.setObjectProperty(json, \"${propertyName}\", object);");
            body.append("}");
        }

        private void handlePrimitiveTypeProperty(BodyBuilder body) {
            body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getType()));
            body.addContext("propertyName", property.getName());
            body.addContext("getterMethodName", getterMethodName(property));

            body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
        }

        private void handleListProperty(BodyBuilder body) {
            body.addContext("propertyName", property.getName());
            body.addContext("getterMethodName", getterMethodName(property));

            PropertyType listValuePropertyType = property.getType().getNested().iterator().next();
            if (listValuePropertyType.isPrimitiveType()) {
                body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getType()));

                body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
            } else if (listValuePropertyType.isEntityType()) {
                String entityTypeName = listValuePropertyType.getSimpleType();
                String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
                EntityModel entityTypeModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                if (entityTypeModel == null) {
                    Logger.warn("[CreateWritersStage] LIST Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type is entity but not found in index: " + property.getType());
                    return;
                }
                JavaInterfaceSource entityTypeJavaModel = getState().getJavaIndex().lookupInterface(getEntityInterfaceFQN(entityTypeModel));
                if (entityTypeJavaModel == null) {
                    Logger.warn("[CreateWritersStage] LIST Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type is entity but not found in JAVA index: " + property.getType());
                    return;
                }
                addImportTo(entityTypeJavaModel, writerClassSource);
                addImportTo(List.class, writerClassSource);
                addImportTo(ArrayNode.class, writerClassSource);

                body.addContext("listValueJavaType", entityTypeJavaModel.getName());
                body.addContext("writeMethodName", writeMethodName(entityTypeModel));

                body.append("{");
                body.append("    List<${listValueJavaType}> models = node.${getterMethodName}();");
                body.append("    if (models != null) {");
                body.append("        ArrayNode array = JsonUtil.arrayNode();");
                body.append("        models.forEach(model -> {");
                body.append("            ObjectNode object = JsonUtil.objectNode();");
                body.append("            this.${writeMethodName}(model, object);");
                body.append("            array.add(object);");
                body.append("        });");
                body.append("        JsonUtil.setAnyProperty(json, \"children\", array);");
                body.append("    }");
                body.append("}");
            } else {
                Logger.warn("[CreateWritersStage] LIST Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
            }
        }

        private void handleMapProperty(BodyBuilder body) {
            body.addContext("propertyName", property.getName());
            body.addContext("getterMethodName", getterMethodName(property));

            PropertyType mapValuePropertyType = property.getType().getNested().iterator().next();
            if (mapValuePropertyType.isPrimitiveType()) {
                body.addContext("setPropertyMethodName", determineSetPropertyVariant(property.getType()));
                addImportTo(List.class, writerClassSource);

                body.append("JsonUtil.${setPropertyMethodName}(json, \"${propertyName}\", node.${getterMethodName}());");
            } else if (mapValuePropertyType.isEntityType()) {
                String entityTypeName = mapValuePropertyType.getSimpleType();
                String fqEntityName = entityModel.getNamespace().fullName() + "." + entityTypeName;
                EntityModel entityTypeModel = getState().getConceptIndex().lookupEntity(fqEntityName);
                if (entityTypeModel == null) {
                    Logger.warn("[CreateWritersStage] MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type is entity but not found in index: " + property.getType());
                    return;
                }
                JavaInterfaceSource entityTypeJavaModel = getState().getJavaIndex().lookupInterface(getEntityInterfaceFQN(entityTypeModel));
                if (entityTypeJavaModel == null) {
                    Logger.warn("[CreateWritersStage] MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                    Logger.warn("[CreateWritersStage]        property type is entity but not found in JAVA index: " + property.getType());
                    return;
                }
                addImportTo(Map.class, writerClassSource);
                addImportTo(entityTypeJavaModel, writerClassSource);

                body.addContext("mapValueJavaType", entityTypeJavaModel.getName());
                body.addContext("writeMethodName", "write" + entityTypeName);

                body.append("{");
                body.append("    ObjectNode object = JsonUtil.objectNode();");
                body.append("    Map<String, ${mapValueJavaType}> models = node.${getterMethodName}();");
                body.append("    if (models != null) {");
                body.append("        models.keySet().forEach(jsonName -> {");
                body.append("            ObjectNode jsonValue = JsonUtil.objectNode();");
                body.append("            this.${writeMethodName}(models.get(jsonName), jsonValue);");
                body.append("            JsonUtil.setObjectProperty(object, jsonName, jsonValue);");
                body.append("        });");
                body.append("        JsonUtil.setObjectProperty(json, \"${propertyName}\", object);");
                body.append("    }");
                body.append("}");
            } else {
                Logger.warn("[CreateWritersStage] MAP Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
                Logger.warn("[CreateWritersStage]        property type: " + property.getType());
            }
        }

        private void handleUnionProperty(BodyBuilder body) {
            // TODO Auto-generated method stub
            Logger.warn("[CreateWritersStage] UNION Entity property '" + property.getName() + "' not written (unsupported) for entity: " + entityModel.fullyQualifiedName());
            Logger.warn("[CreateWritersStage]        property type: " + property.getType());
        }

        /**
         * Figure out which variant of "writeProperty" from "JsonUtil" we should use for
         * this property.  The property might be a primitive type, or a list/map of primitive
         * types.
         *
         * @param type
         */
        private String determineSetPropertyVariant(PropertyType type) {
            if (type.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(type);
                if (ObjectNode.class.equals(_class)) {
                    addImportTo(_class, writerClassSource);
                    return "setObjectProperty";
                } else if (JsonNode.class.equals(_class)) {
                    addImportTo(_class, writerClassSource);
                    return "setAnyProperty";
                } else {
                    return "set" + _class.getSimpleName() + "Property";
                }
            }

            if (type.isList()) {
                PropertyType listType = type.getNested().iterator().next();
                if (listType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(listType);
                    if (ObjectNode.class.equals(_class)) {
                        addImportTo(_class, writerClassSource);
                        return "setObjectArrayProperty";
                    } else if (JsonNode.class.equals(_class)) {
                        addImportTo(_class, writerClassSource);
                        return "setAnyArrayProperty";
                    } else {
                        return "set" + _class.getSimpleName() + "ArrayProperty";
                    }
                }
            }

            if (type.isMap()) {
                PropertyType mapType = type.getNested().iterator().next();
                if (mapType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(mapType);
                    if (ObjectNode.class.equals(_class)) {
                        addImportTo(_class, writerClassSource);
                        return "setObjectMapProperty";
                    } else if (JsonNode.class.equals(_class)) {
                        addImportTo(_class, writerClassSource);
                        return "setAnyMapProperty";
                    } else {
                        return "set" + _class.getSimpleName() + "MapProperty";
                    }
                }
            }

            Logger.warn("[CreateWritersStage] Unable to determine value type for: " + property);
            return "setProperty";
        }

        /**
         * Determines the Java data type of the given property.
         *
         * @param type
         */
        private String determineValueType(PropertyType type) {
            if (type.isPrimitiveType()) {
                Class<?> _class = primitiveTypeToClass(type);
                if (_class != null) {
                    addImportTo(_class, writerClassSource);
                    return _class.getSimpleName();
                }
            }

            if (type.isList()) {
                PropertyType listType = type.getNested().iterator().next();
                if (listType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(listType);
                    if (_class != null) {
                        addImportTo(_class, writerClassSource);
                        return "List<" + _class.getSimpleName() + ">";
                    }
                }
            }

            if (type.isMap()) {
                PropertyType mapType = type.getNested().iterator().next();
                if (mapType.isPrimitiveType()) {
                    Class<?> _class = primitiveTypeToClass(mapType);
                    if (_class != null) {
                        addImportTo(_class, writerClassSource);
                        return "Map<String, " + _class.getSimpleName() + ">";
                    }
                }
            }

            Logger.warn("[CreateWritersStage] Unable to determine value type for: " + property);
            return "Object";
        }
    }
}
