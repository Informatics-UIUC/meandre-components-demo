package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;


public class FiringPolicyDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String pushString = wflow.addComponent(
		"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(pushString, "string", "Hello Seasr!");

		String urlFetcher = wflow.addComponent(
		"org.meandre.components.io.url.URLFetcher");
		wflow.setComponentInstanceProp(
				urlFetcher, "location", "http://www.gutenberg.org/files/158/158.txt");

		String streamReader = wflow.addComponent(
		"org.meandre.components.io.StreamContentReader");
		wflow.connectComponents(urlFetcher, "Stream", streamReader, "Stream");

		String html2Text = wflow.addComponent(
		"org.meandre.components.text.transform.HTML2Text");
		wflow.connectComponents(streamReader, "Object", html2Text, "Html");

		String testingFiringPolicy = wflow.addComponent(
		"org.meandre.components.io.TestingFiringPolicy");
		wflow.connectComponents(pushString, "string", testingFiringPolicy, "string_1");
		wflow.connectComponents(html2Text, "Text", testingFiringPolicy, "string_2");

		flowBuilder.execute(wflow, false);
	}
}
