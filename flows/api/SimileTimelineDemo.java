package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class SimileTimelineDemo {
	public static void main(String[] args) {
		FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

		String pushString = wflow.addComponent(
				"org.meandre.components.io.PushString");
		wflow.setComponentInstanceProp(
				pushString, "string", "http://www.gutenberg.org/files/22925/22925.txt");
				//"http://www.gutenberg.org/dirs/etext04/dlshg10.txt");
				//"http://www.gutenberg.org/files/20120/20120-8.txt");
				//"http://norma.ncsa.uiuc.edu/public-dav/applets/Mississippi(1918).txt");
				//"http://www.gutenberg.org/files/26090/26090.txt");




		String toDoc = wflow.addComponent("" +
				"org.seasr.components.text.io.file.TextFileToDoc");
		wflow.connectComponents(pushString, "string", toDoc, "file_name");
		wflow.setComponentInstanceProp(toDoc, "webdav", "true");
		wflow.setComponentInstanceProp(toDoc, "add_space_at_new_lines", "true");

		String sentenceDetect = wflow.addComponent(
				"org.seasr.components.text.opennlp.sentence.OpenNLP_SentenceDetect");
		wflow.connectComponents(
				toDoc, "document", sentenceDetect, "document_in");

		String tokenizer = wflow.addComponent(
				"org.seasr.components.text.opennlp.tokenize.OpenNLP_Tokenizer");
		wflow.connectComponents(
				sentenceDetect, "document_out", tokenizer, "document_in");

		String posTagger = wflow.addComponent(
				"org.seasr.components.text.opennlp.pos.OpenNLP_PosTagger");
		wflow.connectComponents(
				tokenizer, "document_out", posTagger, "document_in");

		String nameFinder = wflow.addComponent(
				"org.seasr.components.text.opennlp.ie.OpenNLP_NameFinder");
		wflow.connectComponents(
				posTagger, "document_out", nameFinder, "document_in");
		wflow.setComponentInstanceProp(
				nameFinder, "verbose", "false");

		String annToXML = wflow.addComponent(
				"org.meandre.components.text.transform.Annotation2XML");
		wflow.connectComponents(
				nameFinder, "document_out", annToXML, "Document");
		wflow.setComponentInstanceProp(
				annToXML, "Entities", "date");

		/*String viewer = wflow.addComponent(
				"org.meandre.components.viz.temporal.SimileTimelineViewer");
		wflow.connectComponents(
				annToXML, "Annotation_xml", viewer, "Document");*/

		String stMaker = wflow.addComponent(
			"org.meandre.components.viz.temporal.SimileTimelineMaker");
		wflow.connectComponents(
				annToXML, "Annotation_xml", stMaker, "Document");

		String viewer = wflow.addComponent(
				"org.meandre.components.viz.temporal.SimileTimelineViz");
		wflow.connectComponents(
				stMaker, "Text", viewer, "Text");

		flowBuilder.execute(wflow, true);

		System.exit(0);
	}
}

