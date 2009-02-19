package org.meandre.components.text.transform;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Loretta Auvil",
		description="This component extracts the text from a pdf document. "+
		"The input is a String or URL specifiying the url of the pdf document. "+
		"The output is the extracted text.",
		name="ProcessTextBasedOnFormat",
		tags="URL, text, pdf",
		dependency={"FontBox-0.1.0.jar"},
		baseURL="meandre://seasr.org/components/")

public class ProcessTextBasedOnFormat implements ExecutableComponent {

	@ComponentInput(description="URL of the item." +
			"<br>TYPE: java.io.String",
			name="URL")
			public final static String DATA_INPUT = "URL";
	@ComponentOutput(description="URL of the pdf item." +
			"<br>TYPE: java.io.String",
			name="PDF_URL")
			public final static String DATA_OUTPUT_PDF = "PDF_URL";
	@ComponentOutput(description="URL of the html item." +
			"<br>TYPE: java.io.String",
			name="HTML_URL")
			public final static String DATA_OUTPUT_HTML = "HTML_URL";
	@ComponentOutput(description="URL of the text item." +
			"<br>TYPE: java.io.String",
			name="TXT_URL")
			public final static String DATA_OUTPUT_TXT = "TXT_URL";

	public void dispose(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub

	}

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {

		String url = (String)cc.getDataComponentFromInput(DATA_INPUT);
		if (url.endsWith(".pdf"))
			cc.pushDataComponentToOutput(DATA_OUTPUT_PDF, url);
		else if (url.endsWith(".html") || (url.endsWith(".xml")))
			cc.pushDataComponentToOutput(DATA_OUTPUT_HTML, url);
		else
			cc.pushDataComponentToOutput(DATA_OUTPUT_TXT, url);
	}

	public void initialize(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub
	}
}
