/*
 * Copyright 2020 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.umg;

import io.apicurio.umg.beans.Specification;
import io.apicurio.umg.logging.Logger;
import io.apicurio.umg.pipe.CreateVisitorsStage;
import io.apicurio.umg.pipe.DebugStage;
import io.apicurio.umg.pipe.GeneratorState;
import io.apicurio.umg.pipe.NormalizeVisitorsStage;
import io.apicurio.umg.pipe.Pipeline;
import io.apicurio.umg.pipe.concept.CreateEntityModelsStage;
import io.apicurio.umg.pipe.concept.CreateNamespaceModelsStage;
import io.apicurio.umg.pipe.concept.CreateParentTraitsStage;
import io.apicurio.umg.pipe.concept.CreatePropertyModelsStage;
import io.apicurio.umg.pipe.concept.CreateTraitModelsStage;
import io.apicurio.umg.pipe.concept.IndexSpecificationsStage;
import io.apicurio.umg.pipe.concept.NormalizeEntitiesStage;
import io.apicurio.umg.pipe.concept.NormalizePropertiesStage;
import io.apicurio.umg.pipe.concept.NormalizeTraitsStage;
import io.apicurio.umg.pipe.concept.RemoveTransparentTraitsStage;

import java.io.File;
import java.util.Collection;

/**
 * @author eric.wittmann@gmail.com
 */
public class UnifiedModelGenerator {

    private Collection<Specification> specifications;

    public static UnifiedModelGenerator create(Collection<Specification> specifications) {
        UnifiedModelGenerator generator = new UnifiedModelGenerator(specifications);
        return generator;
    }

    private UnifiedModelGenerator(Collection<Specification> specifications) {
        this.specifications = specifications;
    }

    /**
     * Generates the output from the given list of specifications.
     */
    public void generateInto(File outputDirectory) throws Exception {
        Logger.info("Output directory: %s", outputDirectory.getAbsolutePath());

        GeneratorState state = new GeneratorState();
        state.setSpecifications(specifications);
        Pipeline pipe = new Pipeline();

        // Index phase
        pipe.addStage(new IndexSpecificationsStage());

        // Model creation phase
        pipe.addStage(new CreateNamespaceModelsStage());
        pipe.addStage(new CreateTraitModelsStage());
        pipe.addStage(new CreateEntityModelsStage());
        pipe.addStage(new CreatePropertyModelsStage());
        pipe.addStage(new CreateParentTraitsStage());
        pipe.addStage(new CreateVisitorsStage());

        // Model optimization phase
        pipe.addStage(new RemoveTransparentTraitsStage());
        pipe.addStage(new NormalizeTraitsStage());
        pipe.addStage(new NormalizeEntitiesStage());
        pipe.addStage(new NormalizePropertiesStage());
        pipe.addStage(new NormalizeVisitorsStage());

        // Debug the models
        pipe.addStage(new DebugStage());

        //        pipe.addStage(new PrepareJsonSchemaStage());
        //
        //        pipe.addStage(new CreateClassModelsStage());
        //        pipe.addStage(new ProcessExtendsStage());
        //
        //        pipe.addStage(new IncludeFieldsModelsStage());
        //        pipe.addStage(new CreateFieldModelsStage());
        //
        //        pipe.addStage(new UnionTypeMapperStage());
        //
        //        pipe.addStage(new RemoveTransparentClassModelsStage());
        //
        //        pipe.addStage(new NormalizeModelsStage());
        //        pipe.addStage(new NormalizeFieldsStage());
        //
        //        pipe.addStage(new JavaClassStage());
        //        pipe.addStage(new JavaAddImplementsStage());
        //        pipe.addStage(new JavaSuperTypesStage());
        //        pipe.addStage(new JavaFieldStage());
        //        pipe.addStage(new JavaGetterStage());
        //        pipe.addStage(new JavaSetterStage());
        //        pipe.addStage(new JavaWriteStage(outputDirectory));

        pipe.run(state);
    }
}
