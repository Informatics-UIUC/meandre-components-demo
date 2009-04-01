package org.meandre.components.tapor.util;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
        description="Converts XML doucment to Map<String, Integer>. " +
        "The structure of the XML document is defined as " +
        "<tapor:item>" +
        "<tapor:label></tapor:label>" +
        "<tapor:count></tapor:count>" +
        "</tapor:item>.",
        name="XML2Map",
        tags="XML map converter",
        baseURL="meandre://seasr.org/components/")

public class XML2Map extends AbstractExecutableComponent
{
	@ComponentInput(description="Input XML document to be converted." +
            "<br>TYPE: java.lang.String",
                    name= "Text")
    public final static String DATA_INPUT = "Text";

	@ComponentOutput(description="Output content in Map format." +
			"TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
	                 name="Map")
	public final static String DATA_OUTPUT = "Map";

	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
	public void executeCallBack(ComponentContext cc)
	throws Exception {
		String xmlString = (String)cc.getDataComponentFromInput(DATA_INPUT);

		//DocumentBuilder reports error if not removing the &lt;, &gt;, &quot; <br> and <b>.
		xmlString = xmlString.replaceAll("&lt;", "<");
		xmlString = xmlString.replaceAll("&gt;", ">");
		xmlString = xmlString.replaceAll("&quot;", "\"");
		xmlString = xmlString.replaceAll("<BR/>|<br/>|<b>|</b>", "");
		xmlString = xmlString.replaceAll("&ntilde;", "Ñ");

		xmlString = xmlString.replaceAll("&", "");

		Map<String, Integer> outputMap = new Hashtable<String, Integer>();

		try {
			DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			Document doc = db.parse(inStream);

			NodeList nodeList = doc.getElementsByTagName("tapor:item");
			for(int index=0; index < nodeList.getLength(); index++) {
				Node node = nodeList.item(index);
				String label = node.getFirstChild().getTextContent();
				Integer count = Integer.valueOf(node.getLastChild().getTextContent());
				outputMap.put(label, count);
			}
		}catch(Exception e) {
			throw new ComponentExecutionException(e);
		}

		cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);
	}

	/**
	 * Call at the end of an execution flow.
	 */
	public void initializeCallBack(ComponentContextProperties ccp)
	throws Exception {
	}

	/**
	 * Called when a flow is started.
	 */
	public void disposeCallBack(ComponentContextProperties ccp)
	throws Exception {
	}
}
