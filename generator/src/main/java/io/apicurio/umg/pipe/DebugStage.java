package io.apicurio.umg.pipe;

public class DebugStage extends AbstractStage {

    @Override
    protected void doProcess() {
        // Debug output
        // TODO remove this
        if (Boolean.TRUE) {
            return;
        }
        System.out.println("---");
        getState().getIndex().findPackages("io.apicurio").forEach(pkg -> {
            System.out.println("Package: " + pkg.getName());
            pkg.getClasses().values().forEach(clss -> {
                System.out.println("    Class: " + clss.getName());
                clss.getFields().values().forEach(field -> {
                    System.out.println("        Field: " + field.getName() + " (" + field.getType() + ")");
                });
            });
        });
        System.out.println("---");
    }


}
