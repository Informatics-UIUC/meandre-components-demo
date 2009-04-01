package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class CSVDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String pushString = wflow.addComponent(
			"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(pushString, "string", "http://repository.seasr.org/Datasets/UCI/csv/mushroom.csv");
				//"http://norma.ncsa.uiuc.edu/public-dav/Birdy/st_6_19-20_06_262.csv");

		String urlFetcher = wflow.addComponent(
			"org.meandre.components.io.url.URLFetcherAuthenticated");
		wflow.connectComponents(
			pushString, "string", urlFetcher, "URL");

		String csvReader = wflow.addComponent(
			"org.meandre.components.io.csv.CSVReader");
		wflow.connectComponents(
			urlFetcher, "Stream", csvReader, "Stream");

		String csViz = wflow.addComponent(
			"org.meandre.components.viz.table.TableViewer");
		wflow.connectComponents(
			csvReader, "CSV_Content", csViz, "Content");
		wflow.setComponentInstanceProp(csViz, "Attribute_Type_Header", "true");
		wflow.setComponentInstanceProp(csViz, "Attribute_Label_Header", "true");
		wflow.setComponentInstanceProp(csViz, "Num_Rows_to_Display", "200");
		wflow.setComponentInstanceProp(csViz, "Num_Columns_to_Display", "8");

		flowBuilder.execute(wflow, true);
	}
}
