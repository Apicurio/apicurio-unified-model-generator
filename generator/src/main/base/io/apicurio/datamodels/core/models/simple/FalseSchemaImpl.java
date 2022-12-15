package io.apicurio.datamodels.core.models.simple;

import java.util.function.Consumer;

public class FalseSchemaImpl implements FalseSchema {

    private String fieldInFalse;

    @Override
    public String getFieldInFalse() {
        return fieldInFalse;
    }

    @Override
    public void ifTrueSchema(Consumer<TrueSchema> consumer) {
        // NOOP
    }

    @Override
    public void ifFalseSchema(Consumer<FalseSchema> consumer) {
        consumer.accept(this);
    }

    @Override
    public boolean isFalseSchema() {
        return true;
    }

    @Override
    public FalseSchema asFalseSchema() {
        return this;
    }

    @Override
    public Variants getVariant() {
        return Variants.FalseSchema;
    }
}
