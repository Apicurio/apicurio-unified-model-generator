package io.apicurio.datamodels.core.models.nested;

import io.apicurio.datamodels.core.models.primitive.EmptySchema;
import io.apicurio.datamodels.core.models.simple.TrueSchemaImpl;

import java.util.List;

import static java.lang.System.out;

public class NodeImpl {

    private EmptySchemaTrueSchemaListUnion union = new TrueSchemaListWrapperImpl(List.of(new TrueSchemaImpl()));

    void test() {
        switch (union.getVariantForEmptySchemaTrueSchemaListUnion()) {
            case TrueSchemaList:
                var list = ((TrueSchemaListWrapper) union).getValue();
                out.println("TrueSchemaList variant has " + list);
                break;
            case EmptySchema:
                var fieldInEmpty = ((EmptySchema) union).getFieldInEmpty();
                out.println("EmptySchema variant has " + fieldInEmpty);
                break;
        }
    }
}
