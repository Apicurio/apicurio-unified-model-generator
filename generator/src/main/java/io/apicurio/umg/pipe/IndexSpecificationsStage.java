package io.apicurio.umg.pipe;

public class IndexSpecificationsStage extends AbstractStage {

	@Override
	protected void doProcess() {
		getState().getSpecifications().forEach(spec -> getState().getSpecIndex().index(spec));
	}

}
