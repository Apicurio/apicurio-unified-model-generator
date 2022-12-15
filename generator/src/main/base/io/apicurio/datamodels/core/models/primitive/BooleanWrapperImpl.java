package io.apicurio.datamodels.core.models.primitive;

import io.apicurio.datamodels.core.models.wrapper.WrapperImpl;

public class BooleanWrapperImpl extends WrapperImpl<Boolean> implements BooleanWrapper {

    public BooleanWrapperImpl(Boolean value) {
        super(value);
    }

    @Override
    public BooleanEmptySchemaUnion.Variants getVariantForBooleanEmptySchemaUnion() {
        return BooleanEmptySchemaUnion.Variants.Boolean;
    }
}
