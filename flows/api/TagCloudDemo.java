package api;

import org.meandre.tools.components.FlowBuilderAPI;
import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

public class TagCloudDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
		
		String urlFetcher = wflow.addComponent(
				"org.meandre.demo.components.io.URLFetcher");
		wflow.setComponentInstanceProp(
				urlFetcher, "location", "http://www.cspan.org/");

		String streamReader = wflow.addComponent(
				"org.meandre.demo.components.io.StreamContentReader");
		wflow.connectComponents(urlFetcher, "outputStream", streamReader, "inputStream");
		
		String html2Text = wflow.addComponent(
				"org.meandre.demo.components.io.HTML2Text");
		wflow.connectComponents(streamReader, "outputObject", html2Text, "inputHtml");
		
		String toLowerCase = wflow.addComponent(
				"org.meandre.demo.components.io.ToLowerCase");
		wflow.connectComponents(
				html2Text, "outpuText", toLowerCase, "inpuText");
		
		String wordCounter = wflow.addComponent(
				"org.meandre.demo.components.io.WordCounter");
		wflow.connectComponents(
				toLowerCase, "outpuText", wordCounter, "inpuText");
		
		String wordFilter = wflow.addComponent(
			"org.meandre.demo.components.io.WordFilter");
		wflow.connectComponents(
				wordCounter, "outputMap", wordFilter, "inputMap");
		wflow.setComponentInstanceProp(
				wordFilter, "fileName", "E:/Limin/code/javascript/tag_cloud/common_words.txt");
		wflow.setComponentInstanceProp(
				wordFilter, "isLimited", "true");
		wflow.setComponentInstanceProp(
				wordFilter, "upperLimit", "300");
		
		String tagCloudGenerator = wflow.addComponent(
				"org.meandre.demo.components.io.TagCloudGenerator");
		wflow.connectComponents(
				wordFilter, "outputMap", tagCloudGenerator, "inputMap");
		wflow.setComponentInstanceProp(
				tagCloudGenerator, "maxSize", "100");
		
		String mimeContentViz = wflow.addComponent(
				"org.meandre.demo.components.io.MIMEContentViz");
		wflow.connectComponents(
				tagCloudGenerator, "outputObject", mimeContentViz, "inputContent");
		wflow.setComponentInstanceProp(
				mimeContentViz, "type", "image");
		
		flowBuilder.execute(wflow, true);
		
		System.exit(0);
	}
}
