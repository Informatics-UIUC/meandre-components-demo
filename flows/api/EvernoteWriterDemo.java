package api;

import org.meandre.tools.components.FlowBuilderAPI;
import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

public class EvernoteWriterDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
	
		String pushString = wflow.addComponent(
				"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(
				pushString, "string", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                					  "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml.dtd\">\n" +
                					  "<en-note>\n" + 
                					  "<b><font size=\"5\">Here is a simple writer demo:</font></b>\n" + 
                					  "<br/>\n" + 
                					  "<u>Things to work on:</u>\n" +
                					  "<en-todo checked=\"true\"/> Write up Meandre documentation\n" + 
                					  "<br/>\n" +
                					  "<en-todo/> Set up Seasr wiki\n" + 
                					  "<br/>\n" + 
                					  "</en-note>\n");
		
		String evernoteWriter = wflow.addComponent(
			"org.meandre.demo.components.io.EvernoteWriter");
		wflow.connectComponents(
				pushString, "output_string", evernoteWriter, "inputContent");
		wflow.setComponentInstanceProp(
			evernoteWriter, "username", "li2");
		wflow.setComponentInstanceProp(
			evernoteWriter, "password", "91234567");
		wflow.setComponentInstanceProp(
			evernoteWriter, "title", "for seasr demo");
	
		flowBuilder.execute(wflow, false);
	}
}
