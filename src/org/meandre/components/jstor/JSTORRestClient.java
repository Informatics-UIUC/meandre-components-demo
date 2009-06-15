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

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
           description="Demonstrates how to construct a interface to " +
           "consume rest service of jstor at " +
           "http://dfr.jstor.org/api.",
           name="Rest Client",
           tags="rest service",
           baseURL="meandre://seasr.org/components/")

public class JSTORRestClient extends AbstractExecutableComponent
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
    		//String loc = "http://dfr.jstor.org/sru/?operation=searchRetrieve&query=jstor.discipline+%3D+%22Education%22&version=1.1";
    		String loc = "http://dfr.jstor.org/sru/?operation=searchRetrieve&recordPacking=xml&version=1.1&startRecord=11&maximumRecords=10&resultSetTTL=&recordSchema=info:srw/schema/1/dc-v1.1&query=dc.description%20=%20springer";
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
        	httpClient.executeMethod(getMethod);

        	BufferedReader in = new BufferedReader(
        			new InputStreamReader(/*postMethod*/getMethod.getResponseBodyAsStream()));
        	String line = null;
        	StringBuffer buf = new StringBuffer();
        	while((line = in.readLine()) != null) {
        		buf.append(line).append("\n");
        		//console.fine(line);
        	}
        	in.close();

        	System.out.println(buf.toString());

        	cc.pushDataComponentToOutput(DATA_OUTPUT, buf.toString());

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
