package io.apicurio.datamodels.core.models.nested;

import io.apicurio.datamodels.core.models.simple.TrueSchema;
import io.apicurio.datamodels.core.models.wrapper.WrapperImpl;

import java.util.List;

public class TrueSchemaListWrapperImpl extends WrapperImpl<List<TrueSchema>> implements TrueSchemaListWrapper {

    public TrueSchemaListWrapperImpl(List<TrueSchema> value) {
        super(value);
    }

    @Override
    public Variants getVariantForEmptySchemaTrueSchemaListUnion() {
        return Variants.TrueSchemaList;
    }
}
