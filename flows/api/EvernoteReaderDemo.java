package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class EvernoteReaderDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
		
		String evernoteReader = wflow.addComponent(
				"org.meandre.demo.components.io.EvernoteReader");
		wflow.setComponentInstanceProp(
				evernoteReader, "username", "li2");
		wflow.setComponentInstanceProp(
				evernoteReader, "password", "91234567");
		
		flowBuilder.execute(wflow, false);
	}
}
