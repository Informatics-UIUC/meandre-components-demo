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
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
        description="Demonstrate how to construct a interface to " +
        "consume web service of list words of Tapor at " +
        "http://tada.mcmaster.ca/view/Main/TAPoRware#Using_TAPoRware_as_a_web_service.",
        name="List Words",
        tags="word web service",
        baseURL="meandre://seasr.org/components/")

public class ListWords implements ExecutableComponent{
	private static String qnameService = "TaporwareService";
	private static String qnamePort = "TaporwareService_xml";
	private static String BODY_NAMESPACE_VALUE =  "http://taporware.mcmaster.ca/~taporware/webservice";
	private static String ENCODING_STYLE_PROPERTY = "javax.xml.rpc.encodingstyle.namespace.uri";
	private static String NS_XSD = "http://www.w3.org/2001/XMLSchema";
	private static String URI_ENCODING =  "http://schemas.xmlsoap.org/soap/encoding/";

	/** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
	public void execute(ComponentContext cc) throws
	ComponentExecutionException, ComponentContextException {
		String endPoint = "http://taporware.mcmaster.ca:9982";

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

	        call.setOperationName(new QName(BODY_NAMESPACE_VALUE,"list_Words_HTML"));

	        call.addParameter("htmlInput", QNAME_TYPE_STRING, ParameterMode.IN);
	        call.addParameter("htmlTag", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("listOption", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("optionSelection", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("sorting", QNAME_TYPE_STRING, ParameterMode.IN);
	    	call.addParameter("outFormat", QNAME_TYPE_STRING, ParameterMode.IN);

	    	String htmlInput = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n";
	    	htmlInput += "<!--NewPage-->\n";
	        htmlInput += "<HTML>\n";
	        htmlInput += "<HEAD>\n";
	        htmlInput += "<meta name=\"collection\" content=\"exclude\">\n";
	        htmlInput += "<!-- Generated by javadoc on Wed Aug 11 07:30:38 PDT 2004-->\n";
	        htmlInput += "<TITLE>\n";
			htmlInput += "Java 2 Platform SE 5.0\n";
			htmlInput += "</TITLE>\n";
			htmlInput += "<SCRIPT type=\"text/javascript\">\n";
			htmlInput += "targetPage = \"\" + window.location.search;\n";
			htmlInput += "if (targetPage != \"\" && targetPage != \"undefined\")\n";
			htmlInput += "targetPage = targetPage.substring(1);\n";
			htmlInput += "if (targetPage.indexOf(\":\") != -1)\n";
			htmlInput += "targetPage = \"undefined\";\n";
			htmlInput += "function loadFrames() {\n";
			htmlInput += "if (targetPage != \"\" && targetPage != \"undefined\")\n";
			htmlInput += "top.classFrame.location = top.targetPage;\n";
			htmlInput += "}\n";
			htmlInput += "</SCRIPT>\n";
			htmlInput += "<NOSCRIPT>\n";
			htmlInput += "</NOSCRIPT>\n";
			htmlInput += "</HEAD>\n";
			htmlInput += "<FRAMESET cols=\"20%,80%\" title=\"\" onLoad=\"top.loadFrames()\">\n";
			htmlInput += "<FRAMESET rows=\"30%,70%\" title=\"\" onLoad=\"top.loadFrames()\">\n";
			htmlInput += "<FRAME src=\"overview-frame.html\" name=\"packageListFrame\" title=\"All Packages\">\n";
			htmlInput += "<FRAME src=\"allclasses-frame.html\" name=\"packageFrame\" title=\"All classes and interfaces (except non-static nested types)\">\n";
			htmlInput += "</FRAMESET>\n";
			htmlInput += "<FRAME src=\"overview-summary.html\" name=\"classFrame\" title=\"Package, class and interface descriptions\" scrolling=\"yes\">\n";
			htmlInput += "<NOFRAMES>\n";
			htmlInput += "<H2>\n";
			htmlInput += "Frame Alert</H2>\n";
			htmlInput += "<P>\n";
			htmlInput += "This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.\n";
			htmlInput += "<BR>\n";
			htmlInput += "Link to<A HREF=\"overview-summary.html\">Non-frame version.</A>\n";
			htmlInput += "</NOFRAMES>\n";
			htmlInput += "</FRAMESET>\n";
			htmlInput += "</HTML>\n";

			String[] params = { htmlInput, "body", "all", "glasgow", "2", "4" };
		    String result = (String)call.invoke(params);
		    java.io.PrintWriter pw = new java.io.PrintWriter("result.txt");
			pw.println(result);
			pw.flush();
			pw.close();

		    System.out.println(result);
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
