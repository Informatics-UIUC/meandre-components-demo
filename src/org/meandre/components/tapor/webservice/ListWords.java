/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/
package org.meandre.components.tapor.webservice;

import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.ParameterMode;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
        description="Demonstrate how to construct a interface to " +
        "consume web service of list_Words_HTML of Tapor at " +
        "http://tada.mcmaster.ca/view/Main/TAPoRware#Using_TAPoRware_as_a_web_service.",
        name="List Words",
        tags="word web service",
        dependency={"FastInfoset.jar", "jaxrpc-impl.jar", "jaxrpc-spi.jar", "jsr173_api.jar", "saaj-impl.jar"},
        baseURL="meandre://seasr.org/components/")

public class ListWords implements ExecutableComponent {
	@ComponentInput(description="Input text to be analyzed." +
            "<br>TYPE: java.lang.String",
                    name= "Text")
    public final static String DATA_INPUT = "Text";

	@ComponentOutput(description="Output the string passed from Tapor." +
			"<br>TYPE: java.lang.String",
	                 name="Text")
	public final static String DATA_OUTPUT = "Text";

	private String qnameService = "TaporwareService";
	private String qnamePort = "TaporwareService_xml";
	private String bodyNamespaceValue =  "http://taporware.mcmaster.ca/~taporware/webservice";
	private String endPoint = "http://taporware.mcmaster.ca:9982";

	private String ENCODING_STYLE_PROPERTY = "javax.xml.rpc.encodingstyle.namespace.uri";
	private String NS_XSD = "http://www.w3.org/2001/XMLSchema";
	private String URI_ENCODING =  "http://schemas.xmlsoap.org/soap/encoding/";

	/** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
	public void execute(ComponentContext cc) throws
	ComponentExecutionException, ComponentContextException {
		String htmlInput = (String)cc.getDataComponentFromInput(DATA_INPUT);

	    try {
	    	ServiceFactory factory = ServiceFactory.newInstance();
	        Service service = factory.createService(
	        		new QName(qnameService));
	        QName port = new QName(qnamePort);

	        Call call = service.createCall(port);
	        call.setTargetEndpointAddress(endPoint);

	        call.setProperty(Call.SOAPACTION_USE_PROPERTY, new Boolean(false));
	        call.setProperty(Call.SOAPACTION_URI_PROPERTY, "");
	        call.setProperty(ENCODING_STYLE_PROPERTY, URI_ENCODING);

	        QName QNAME_TYPE_STRING = new QName(NS_XSD, "string");
	        call.setReturnType(QNAME_TYPE_STRING);

	        call.setOperationName(new QName(bodyNamespaceValue,"list_Words_HTML"));

	        call.addParameter("htmlInput", QNAME_TYPE_STRING, ParameterMode.IN);
	        call.addParameter("htmlTag", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("listOption", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("optionSelection", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("sorting", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("outFormat", QNAME_TYPE_STRING, ParameterMode.IN);

			String[] params = { htmlInput, "body", "all", "glasgow", "2", "2" };
		    String result = (String)call.invoke(params);
		    java.io.PrintWriter pw = new java.io.PrintWriter("result.html");
			pw.println(result);
			pw.flush();
			pw.close();

		    cc.pushDataComponentToOutput(DATA_OUTPUT, result);
		} catch (Exception ex) {
			throw new ComponentExecutionException(ex);
		}
	}

	/**
	 * Call at the end of an execution flow.
	 */
	public void initialize(ComponentContextProperties ccp) {
	}

	/**
	 * Called when a flow is started.
	 */
	public void dispose(ComponentContextProperties ccp) {
	}
}
