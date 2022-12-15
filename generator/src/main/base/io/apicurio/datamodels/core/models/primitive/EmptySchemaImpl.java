package io.apicurio.datamodels.core.models.primitive;

import io.apicurio.datamodels.core.models.nested.EmptySchemaTrueSchemaListUnion;

public class EmptySchemaImpl implements EmptySchema {

    private String fieldInEmpty;

    @Override
    public String getFieldInEmpty() {
        return fieldInEmpty;
    }

    @Override
    public EmptySchemaTrueSchemaListUnion.Variants getVariantForEmptySchemaTrueSchemaListUnion() {
        return EmptySchemaTrueSchemaListUnion.Variants.EmptySchema;
    }

    @Override
    public BooleanEmptySchemaUnion.Variants getVariantForBooleanEmptySchemaUnion() {
        return BooleanEmptySchemaUnion.Variants.EmptySchema;
    }
}
