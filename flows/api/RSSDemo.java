package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class RSSDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String urlFetcher = wflow.addComponent(
				"org.meandre.components.io.url.URLFetcher");

		String rssParser = wflow.addComponent(
				"org.meandre.components.io.rss.RSSParser");
		wflow.connectComponents(
				urlFetcher, "Stream", rssParser, "Stream");

		String rssViz = wflow.addComponent(
				"org.meandre.components.viz.text.RSSViz");
		wflow.connectComponents(
				rssParser, "object", rssViz, "RSS_Feed");

		flowBuilder.execute(wflow, true);
	}
}
