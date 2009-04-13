package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class TextClusteringDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String pushString = wflow.addComponent(
			"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(
				pushString, "string", "http://www.gutenberg.org/files/22925/22925.txt");

		String toDoc = wflow.addComponent("" +
			"org.seasr.components.text.io.file.TextFileToDoc");
		wflow.connectComponents(pushString, "string", toDoc, "file_name");
		wflow.setComponentInstanceProp(toDoc, "webdav", "true");
		wflow.setComponentInstanceProp(toDoc, "add_space_at_new_lines", "true");
		wflow.setComponentInstanceProp(toDoc, "retain_new_lines", "false");
		wflow.setComponentInstanceProp(toDoc, "store_dir_name", "false");

		flowBuilder.execute(wflow, false);
	}
}
