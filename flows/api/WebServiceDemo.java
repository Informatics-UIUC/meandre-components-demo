package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class WebServiceDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String urlFetcher = wflow.addComponent(
				"org.meandre.components.io.url.URLFetcher");
		wflow.setComponentInstanceProp(
				urlFetcher, "location", "http://www.cnn.com");

		String streamContentReader = wflow.addComponent(
				"org.meandre.components.io.StreamContentReader");
		wflow.connectComponents(
				urlFetcher, "Stream", streamContentReader, "Stream");

		String listWords = wflow.addComponent(
				//"org.meandre.components.tapor.webservice.ListWords");
				"org.meandre.components.tapor.webservice.ListTagsHTML");
		wflow.connectComponents(
				streamContentReader, "Object", listWords, "Text");

		/*String concordance = wflow.addComponent(
				"org.meandre.components.tapor.restservice.ListWords");
		wflow.connectComponents(
				streamContentReader, "Object", concordance, "Text");*/

		String xml2Map = wflow.addComponent(
				"org.meandre.components.tapor.util.XML2Map");
		wflow.connectComponents(listWords, "Text", xml2Map, "Text");

		String wordFilter = wflow.addComponent(
				"org.meandre.components.text.wordcount.WordCountFilterAdvanced");
		wflow.connectComponents(
				xml2Map, "Map", wordFilter, "Map");
		wflow.setComponentInstanceProp(
				wordFilter, "is_Limited", "true");
		wflow.setComponentInstanceProp(
				wordFilter, "upper_Limit", "100");
		wflow.setComponentInstanceProp(
				wordFilter, "URL_for_Stop_Words", "http://repository.seasr.org/Datasets/Text/common_words.txt");

		String m2t = wflow.addComponent(
				"org.meandre.components.text.transform.Map2Table");
		wflow.connectComponents(
				wordFilter, "Map", m2t, "Map");

		String tableViz = wflow.addComponent(
				"org.meandre.components.viz.table.TableViewer");
		wflow.connectComponents(
				m2t, "Table", tableViz, "Content");
		wflow.setComponentInstanceProp(tableViz, "Attribute_Type_Header", "false");
		wflow.setComponentInstanceProp(tableViz, "Attribute_Label_Header", "true");
		wflow.setComponentInstanceProp(tableViz, "Num_Rows_to_Display", "50");
		wflow.setComponentInstanceProp(tableViz, "Num_Columns_to_Display", "2");

		/*String tagCloudGenerator = wflow.addComponent(
				"org.meandre.components.viz.text.TagCloudImageMaker");
		wflow.connectComponents(
				wordFilter, "Map", tagCloudGenerator, "Map");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "maxSize", "60");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "minSize", "20");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "countVisible", "false");

		String mimeContent = wflow.addComponent(
				"org.meandre.components.viz.MIMEContentMaker");
		wflow.connectComponents(
				tagCloudGenerator, "Object", mimeContent, "Content");
		wflow.setComponentInstanceProp(
				mimeContent, "MIME_type", "image");

		String htmlViz = wflow.addComponent(
				"org.meandre.components.viz.HTMLViewer");
		wflow.connectComponents(
				mimeContent, "HTML_Content", htmlViz, "Content");*/

		flowBuilder.execute(wflow, true);
	}
}

