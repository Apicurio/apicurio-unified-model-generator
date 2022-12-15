package io.apicurio.datamodels.core.models.nested;

public interface EmptySchemaTrueSchemaListUnion {

    Variants getVariantForEmptySchemaTrueSchemaListUnion();

    enum Variants {
        TrueSchemaList,
        EmptySchema
    }
}
