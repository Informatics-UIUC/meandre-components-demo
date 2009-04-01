package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class TagCloudDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String urlFetcher = wflow.addComponent(
				"org.meandre.components.io.url.URLFetcher");
		wflow.setComponentInstanceProp(
				urlFetcher, "location", "http://www.cnn.com");

		String streamReader = wflow.addComponent(
				"org.meandre.components.io.StreamContentReader");
		wflow.connectComponents(urlFetcher, "Stream", streamReader, "Stream");

		String html2Text = wflow.addComponent(
				"org.meandre.components.text.transform.HTML2Text");
		wflow.connectComponents(streamReader, "Object", html2Text, "Html");

		//String uploader = wflow.addComponent("org.meandre.components.io.file.LocalFileUploader");

		String toLowerCase = wflow.addComponent(
				"org.meandre.components.text.transform.ToLowerCase");
		wflow.connectComponents(
				html2Text, "Text", toLowerCase, "Text");
				//uploader, "Text", toLowerCase, "Text");

		String wordCounter = wflow.addComponent(
				"org.meandre.components.text.wordcount.WordCounter");
		wflow.connectComponents(
				toLowerCase, "Text", wordCounter, "Text");

		String wordFilter = wflow.addComponent(
			"org.meandre.components.text.wordcount.WordCountFilterAdvanced");
		wflow.connectComponents(
				wordCounter, "Map", wordFilter, "Map");
		wflow.setComponentInstanceProp(
				wordFilter, "is_Limited", "true");
		wflow.setComponentInstanceProp(
				wordFilter, "upper_Limit", "100");
		wflow.setComponentInstanceProp(
				wordFilter, "URL_for_Stop_Words", "http://repository.seasr.org/Datasets/Text/common_words.txt");

		/*String m2t = wflow.addComponent(
				"org.meandre.components.text.transform.Map2Table");
		wflow.connectComponents(
				wordFilter, "Map", m2t, "Map");

		String csViz = wflow.addComponent(
				"org.meandre.components.viz.table.TableViewer");
		wflow.connectComponents(
				m2t, "Table", csViz, "Content");
		wflow.setComponentInstanceProp(csViz, "Attribute_Type_Header", "false");
		wflow.setComponentInstanceProp(csViz, "Attribute_Label_Header", "true");
		wflow.setComponentInstanceProp(csViz, "Num_Rows_to_Display", "50");
		wflow.setComponentInstanceProp(csViz, "Num_Columns_to_Display", "2");*/

		String tagCloudGenerator = wflow.addComponent(
				"org.meandre.components.viz.text.TagCloudImageMaker");
		wflow.connectComponents(
				wordFilter, "Map", tagCloudGenerator, "Map");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "maxSize", "100");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "minSize", "30");
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
				mimeContent, "HTML_Content", htmlViz, "Content");

		flowBuilder.execute(wflow, true);

		System.exit(0);
	}
}
