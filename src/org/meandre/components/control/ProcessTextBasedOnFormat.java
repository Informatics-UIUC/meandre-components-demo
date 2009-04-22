package org.meandre.components.control;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;

@Component(creator="Loretta Auvil",
		description="This component examines the URL of the document in order to output "+
		"html or xml urls on one output port and pdf urls on another output port. All other urls"+
		"urls are output on the remaining output port. "+
		"Three different output ports are used for three different types of documents "+
		"(html or xml url, pdf url, url).",
		name="Process Text Based On Format",
		tags="URL, pdf",
		baseURL="meandre://seasr.org/components/")

public class ProcessTextBasedOnFormat extends AbstractExecutableComponent
{

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
	@ComponentOutput(description="URL of the item." +
			"<br>TYPE: java.io.String",
			name="TXT_URL")
			public final static String DATA_OUTPUT_URL = "URL";

	public void disposeCallBack(ComponentContextProperties ccp)
	throws Exception {
		// TODO Auto-generated method stub

	}

	public void executeCallBack(ComponentContext cc)
	throws Exception {

		String url = (String)cc.getDataComponentFromInput(DATA_INPUT);
		if (url.endsWith(".pdf") || url.endsWith(".PDF"))
			cc.pushDataComponentToOutput(DATA_OUTPUT_PDF, url);
		else if (url.endsWith(".html") || url.endsWith(".htm")
				|| url.endsWith(".HTML") || url.endsWith(".HTM")
				|| url.endsWith(".xml") || url.endsWith(".XML"))
			cc.pushDataComponentToOutput(DATA_OUTPUT_HTML, url);
		else
			cc.pushDataComponentToOutput(DATA_OUTPUT_URL, url);
	}

	public void initializeCallBack(ComponentContextProperties ccp)
	throws Exception {
		// TODO Auto-generated method stub
	}
}
