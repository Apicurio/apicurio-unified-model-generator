package io.apicurio.datamodels.core.models.primitive;

public interface BooleanEmptySchemaUnion {

    Variants getVariantForBooleanEmptySchemaUnion();

    enum Variants {
        Boolean,
        EmptySchema
    }
}
