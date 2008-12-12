package api;

import org.meandre.tools.components.FlowBuilderAPI;
import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

public class RSSDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
		
		String urlFetcher = wflow.addComponent(
				"org.meandre.demo.components.io.URLFetcher");
		
		String rssReader = wflow.addComponent(
				"org.meandre.demo.components.io.RSSReader");
		wflow.connectComponents(
				urlFetcher, "outputStream", rssReader, "inputStream");
		
		String rssViz = wflow.addComponent(
				"org.meandre.demo.components.io.RSSViz");
		wflow.connectComponents(
				rssReader, "outputObject", rssViz, "inputFeed");
		
		flowBuilder.execute(wflow, true);
	}
}
