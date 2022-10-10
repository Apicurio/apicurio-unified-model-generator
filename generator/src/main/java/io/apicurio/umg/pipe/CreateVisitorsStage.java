package io.apicurio.umg.pipe;

import java.util.stream.Collectors;

import io.apicurio.umg.logging.Logger;
import io.apicurio.umg.models.EntityModel;
import io.apicurio.umg.models.NamespaceModel;
import io.apicurio.umg.models.VisitorModel;

/**
 * Creates a Visitor for each leaf namespace.  Adds all of the entities
 * from that leaf namespace.
 *
 * @author eric.wittmann@gmail.com
 */
public class CreateVisitorsStage extends AbstractStage {

    @Override
    protected void doProcess() {
        getState().getModelIndex().findNamespaces("").stream().filter(ns -> ns.getChildren().isEmpty()).forEach(ns -> {
            Logger.debug("Creating a visitor for namespace: %s", ns.fullName());
            VisitorModel visitorModel = VisitorModel.builder().namespace(ns).build();
            visitorModel.getEntities().addAll(ns.getEntities().values().stream().map(entity -> cloneEntity(entity)).collect(Collectors.toSet()));
            ns.setVisitor(visitorModel);
            getState().getModelIndex().index(visitorModel);

            // Now create ancestor visitors
            NamespaceModel parentNS = ns.getParent();
            while (parentNS != null) {
                // If the parent namespace has at least 2 children, it needs a visitor
                if (parentNS.getChildren().size() > 1) {
                    VisitorModel parentVisitor = parentNS.getVisitor();
                    if (parentVisitor == null) {
                        parentVisitor = VisitorModel.builder().namespace(parentNS).build();
                        parentNS.setVisitor(parentVisitor);
                        getState().getModelIndex().index(parentVisitor);
                    }
                    visitorModel.setParent(parentVisitor);
                    parentVisitor.getChildren().add(visitorModel);

                    visitorModel = parentVisitor;
                }

                parentNS = parentNS.getParent();
            }
        });
    }

    private EntityModel cloneEntity(EntityModel entity) {
        return EntityModel.builder().name(entity.getName()).build();
    }

}