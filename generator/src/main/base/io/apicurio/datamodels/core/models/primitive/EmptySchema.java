package io.apicurio.datamodels.core.models.primitive;

import io.apicurio.datamodels.core.models.nested.EmptySchemaTrueSchemaListUnion;
import io.apicurio.datamodels.core.models.simple.TrueSchemaFalseSchemaUnion;

public interface EmptySchema extends BooleanEmptySchemaUnion, EmptySchemaTrueSchemaListUnion {

    String getFieldInEmpty();
}
