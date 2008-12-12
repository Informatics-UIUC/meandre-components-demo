package api;

import org.meandre.tools.components.FlowBuilderAPI;
import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

public class CSVDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
		
		String pushString = wflow.addComponent(
			"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(pushString, "string", "http://repository.seasr.org/Datasets/UCI/csv/mushroom.csv");
	
		String urlFetcher = wflow.addComponent(
			"org.meandre.demo.components.io.ActiveURLFetcher");
		wflow.connectComponents(
			pushString, "output_string", urlFetcher, "inputUrl");
	
		String csvReader = wflow.addComponent(
			"org.meandre.demo.components.io.CSVReader");
		wflow.connectComponents(
			urlFetcher, "outputStream", csvReader, "inputStream");
	
		String csViz = wflow.addComponent(
			"org.meandre.demo.components.io.CSViz");
		wflow.connectComponents(
			csvReader, "outputObject", csViz, "inputContent");
		wflow.setComponentInstanceProp(csViz, "type", "true");
		wflow.setComponentInstanceProp(csViz, "header", "true");
		wflow.setComponentInstanceProp(csViz, "nrRows", "150");
		wflow.setComponentInstanceProp(csViz, "nrColumns", "4");
	
		flowBuilder.execute(wflow, true);
	}
}
