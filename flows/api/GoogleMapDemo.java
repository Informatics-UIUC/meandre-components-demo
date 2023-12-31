package api;

import org.meandre.tools.flows.FlowBuilderAPI;
import org.meandre.tools.flows.FlowBuilderAPI.WorkingFlow;

public class GoogleMapDemo {
	public static void main(String[] args) {
			FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
			WorkingFlow wflow = flowBuilder.newWorkingFlow("test");

			String pushString = wflow.addComponent(
					"org.meandre.components.io.PushString");
			wflow.setComponentInstanceProp(
					pushString, "string", //"http://www.gutenberg.org/dirs/etext04/dlshg10.txt");
					//"file:///E:/Limin/code/birdy_project/22925.txt");
					"http://repository.seasr.org/Datasets/Text/ThreeLivesExcerpt.txt");
					//"http://www.gutenberg.org/files/20120/20120-8.txt");

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
					annToXML, "Entities", "location");

			/*String mapViewer = wflow.addComponent(
				"org.meandre.components.viz.geographic.GoogleMapRegionViewer");
			wflow.connectComponents(
				annToXML, "Annotation_xml", mapViewer, "Document");*/

			String mapMaker = wflow.addComponent(
					"org.meandre.components.viz.geographic.GoogleMapMaker");
			wflow.connectComponents(
					annToXML, "Annotation_xml", mapMaker, "Document");

			String mapViewer = wflow.addComponent(
					"org.meandre.components.viz.geographic.GoogleMapViz");
			wflow.connectComponents(
					mapMaker, "vectorOfLatitude", mapViewer, "vectorOfLatitude");
			wflow.connectComponents(
					mapMaker, "vectorOfLongitude", mapViewer, "vectorOfLongitude");
			wflow.connectComponents(
					mapMaker, "vectorOfLocation", mapViewer, "vectorOfLocation");
			wflow.connectComponents(
					mapMaker, "vectorOfContext", mapViewer, "vectorOfContext");

			flowBuilder.execute(wflow, true);

			System.exit(0);
		}
	}

