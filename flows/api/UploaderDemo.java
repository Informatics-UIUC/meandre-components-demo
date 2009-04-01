package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class UploaderDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String uploader = wflow.addComponent(
				"org.meandre.components.io.file.LocalFileUploader");

		String viz = wflow.addComponent(
				"org.meandre.components.viz.RawContentViz");
		wflow.connectComponents(
				uploader, "Text", viz, "String");

		flowBuilder.execute(wflow, true);

		System.exit(0);
	}
}

