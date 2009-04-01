package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class EvernoteReaderDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String evernoteReader = wflow.addComponent(
				"org.meandre.components.io.evernote.EvernoteReader");
				//"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(
				evernoteReader, "username", "li2");
		wflow.setComponentInstanceProp(
				evernoteReader, "password", "91234567");

		flowBuilder.execute(wflow, false);
	}
}
