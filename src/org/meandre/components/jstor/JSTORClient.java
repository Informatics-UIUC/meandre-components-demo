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

package org.meandre.components.jstor;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.Component.Licenses;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
           description="Demonstrates how to construct a interface to " +
           "consume rest service of jstor at " +
           "http://dfr.jstor.org/api.",
           name="JSTOR Client",
           tags="rest service",
           mode = Mode.compute,
           firingPolicy = FiringPolicy.all,
           rights = Licenses.UofINCSA,
           baseURL="meandre://seasr.org/components/jstor/",
           dependency = {"protobuf-java-2.0.3.jar"})

public class JSTORClient extends AbstractExecutableComponent
{
	@ComponentOutput(description="Output the XML passed from JSTOR." +
			"<br>TYPE: java.lang.String",
	                 name="string")
	public final static String DATA_OUTPUT = "string";

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void executeCallBack(ComponentContext cc)
    throws Exception {

    	try {
    		StringBuffer buf = new StringBuffer();
    		String str1 = "http://dfr.jstor.org/sru/?operation=searchRetrieve&query=dc.description+%3D+%22liberal%22&version=1.1&operation=searchRetrieve&recordSchema=info%3Asrw%2Fschema%2F1%2Fdc-v1.1&maximumRecords=";
    		String str2 = "&startRecord=";
    		String str3 = "&recordPacking=xml";
    		int startRecord = 1;
    		int maximumRecords = 1;

    		String loc = str1+Integer.toString(maximumRecords) +
    			str2+Integer.toString(startRecord)+str3;
    		//String loc = "http://dfr.jstor.org/sru/";
    		URL url = new URL(loc);

    		HostConfiguration hostConfig = new HostConfiguration();
    		hostConfig.setHost(url.getHost(), url.getPort());
    		HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
    		httpClient.setHostConfiguration(hostConfig);
    		//PostMethod postMethod = new PostMethod(loc);
    		GetMethod getMethod = new GetMethod(loc);

    		/*postMethod.addParameter("operation", "searchRetrieve");
        	postMethod.addParameter("query", "jstor.text+=+jefferson");
        	postMethod.addParameter("version", "1.1");*/

    		//httpClient.executeMethod(postMethod);
    		int responseCode = httpClient.executeMethod(getMethod);

    		if(responseCode == 200) {
    			BufferedReader in = new BufferedReader(
    					new InputStreamReader(/*postMethod*/getMethod.getResponseBodyAsStream()));
    			String line = null;
    			while((line = in.readLine()) != null) {
    				buf.append(line).append("\n");
    				//console.fine(line);
    			}
    			in.close();
    			String str = buf.toString();
    			int beginIndex = str.indexOf("<numberOfRecords>"),
    			    endIndex = str.indexOf("</numberOfRecords>");
    			if(beginIndex != -1) {
    				beginIndex += new String("<numberOfRecords>").length();
    				maximumRecords = Integer.parseInt(
    					str.substring(beginIndex, endIndex));
    				loc = str1+Integer.toString(maximumRecords)+
    					  str2+Integer.toString(startRecord)+
    					  str3;
    				buf.delete(0, buf.length());
    				url = new URL(loc);
    				hostConfig = new HostConfiguration();
    	    		hostConfig.setHost(url.getHost(), url.getPort());
    	    		httpClient = new HttpClient(new SimpleHttpConnectionManager());
    	    		httpClient.setHostConfiguration(hostConfig);
    	    		getMethod = new GetMethod(loc);
    	    		responseCode = httpClient.executeMethod(getMethod);
    	    		if(responseCode == 200) {
    	    			in = new BufferedReader(
    	    					new InputStreamReader(getMethod.getResponseBodyAsStream()));
    	    			while((line = in.readLine()) != null)
    	    				buf.append(line).append("\n");
    	    			in.close();
    	    			cc.pushDataComponentToOutput(DATA_OUTPUT, buf.toString());
    	    		}
    			}
    		}
    	}catch(Exception e) {
    		throw new ComponentExecutionException(e);
    	}
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
