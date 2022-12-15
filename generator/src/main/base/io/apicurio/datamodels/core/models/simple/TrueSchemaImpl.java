package io.apicurio.datamodels.core.models.simple;

import java.util.function.Consumer;

public class TrueSchemaImpl implements TrueSchema {

    private String fieldInTrue;

    @Override
    public String getFieldInTrue() {
        return fieldInTrue;
    }

    @Override
    public void ifTrueSchema(Consumer<TrueSchema> consumer) {
        consumer.accept(this);
    }

    @Override
    public void ifFalseSchema(Consumer<FalseSchema> consumer) {
        // NOOP
    }

    @Override
    public boolean isTrueSchema() {
        return true;
    }

    @Override
    public TrueSchema asTrueSchema() {
        return this;
    }

    @Override
    public Variants getVariant() {
        return Variants.TrueSchema;
    }
}
