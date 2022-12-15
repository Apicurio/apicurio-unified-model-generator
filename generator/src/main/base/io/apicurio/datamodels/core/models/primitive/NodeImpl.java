package io.apicurio.datamodels.core.models.primitive;

import static java.lang.System.out;

public class NodeImpl {

    private BooleanEmptySchemaUnion union = new BooleanWrapperImpl(true);

    void test() {
        switch (union.getVariantForBooleanEmptySchemaUnion()) {
            case Boolean:
                var _boolean = ((BooleanWrapper) union).getValue();
                out.println("Boolean variant has " + _boolean);
                break;
            case EmptySchema:
                var fieldInEmpty = ((EmptySchema) union).getFieldInEmpty();
                out.println("EmptySchema variant has " + fieldInEmpty);
                break;
        }
        // Outputs:
        // Boolean variant has true
    }
}
