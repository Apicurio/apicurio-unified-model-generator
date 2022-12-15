package io.apicurio.datamodels.core.models.simple;

import java.util.function.Consumer;

public interface TrueSchemaFalseSchemaUnion {

    void ifTrueSchema(Consumer<TrueSchema> consumer);

    void ifFalseSchema(Consumer<FalseSchema> consumer);

    // Option with is* and as*

    default boolean isTrueSchema() {
        return false;
    }

    default boolean isFalseSchema() {
        return false;
    }

    default TrueSchema asTrueSchema() {
        throw new ClassCastException();
    }

    default FalseSchema asFalseSchema() {
        throw new ClassCastException();
    }

    // Option with enums

    Variants getVariant/*ForTrueSchemaFalseSchemaUnion*/();

    enum Variants {
        TrueSchema,
        FalseSchema
    }
}
