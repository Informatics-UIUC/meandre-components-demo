package org.meandre.components.io;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/** This class provides methods related to text extraction from PDF files
 *
 * @author Xavier Llor&agrave;
 * @author Loretta Auvil
 *
 */

@Component(creator="Loretta Auvil",
		description="This component extracts the text from a pdf document. "+
		"The input is a String or URL specifiying the url of the pdf document. "+
		"The output is the extracted text.",
		name="PDFTextExtractor",
		tags="URL, text, pdf",
		dependency={"FontBox-0.1.0.jar"},
		baseURL="meandre://seasr.org/components/")

		public class PDFTextExtractor implements ExecutableComponent {

	@ComponentInput(description="URL of the pdf file." +
			"<br>TYPE: java.io.String",
			name="URL")
			public final static String DATA_INPUT = "URL";
	@ComponentOutput(description="Text of the pdf file." +
			"<br>TYPE: java.io.String",
			name="Text")
			public final static String DATA_OUTPUT = "Text";

	private PrintStream console;

	public void dispose(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub
	}

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		try {
			URL url;
			if (cc.getDataComponentFromInput(DATA_INPUT).getClass().getName() == "java.lang.String")
				url = new URL ((String) cc.getDataComponentFromInput("URL"));
			else if (cc.getDataComponentFromInput(DATA_INPUT).getClass().getName() == "java.net.URL")
					url = (URL) cc.getDataComponentFromInput("URL");
			else {
				console.println("PDFTextExtractor must receive a java.lang.String or a java.net.URL type");
				url = null;
			}
			if (url.toString().endsWith(".pdf")) {

				PDDocument pdd = PDDocument.load(url);
				PDFTextStripper pts = new PDFTextStripper();
				String sRes = pts.getText(pdd);
				pdd.close();
				cc.pushDataComponentToOutput(DATA_OUTPUT, sRes);
			}
		} catch(java.net.MalformedURLException e) {
			throw new ComponentExecutionException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		console = ccp.getOutputConsole();
		console.println("Initializing PDFTextExtrator for " + ccp.getFlowID());
	}
}
