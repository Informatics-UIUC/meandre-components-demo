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

package org.meandre.components.tapor.restservice;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.URL;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Demonstrates how to construct a interface to " +
           "consume rest service of concordance of Tapor at " +
           "http://tada.mcmaster.ca/view/Main/TAPoRware#Using_TAPoRware_as_a_web_service.",
           name="Concordance",
           tags="concordance rest service",
           baseURL="meandre://seasr.org/components/")

public class Concordance implements ExecutableComponent {
	@ComponentInput(description="Input text to be analyzed." +
            "<br>TYPE: java.lang.String",
                    name= "Text")
    public final static String DATA_INPUT = "Text";

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void execute(ComponentContext cc) throws
    ComponentExecutionException,ComponentContextException {
    	String htmlInput = (String)cc.getDataComponentFromInput(DATA_INPUT);

    	try {
    		String loc = "http://tapor1-dev.mcmaster.ca/~restserv/html/concordance";
    		URL url = new URL(loc);

    		HostConfiguration hostConfig = new HostConfiguration();
        	hostConfig.setHost(url.getHost(), url.getPort());
        	HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
        	httpClient.setHostConfiguration(hostConfig);
        	PostMethod postMethod = new PostMethod(loc);

			postMethod.addParameter("htmlInput", htmlInput);
  			postMethod.addParameter("htmlTag", "body");;
			postMethod.addParameter("pattern", "*");
			postMethod.addParameter("context", "1");
			postMethod.addParameter("contextlength", "5");
			postMethod.addParameter("outFormat", "4");

        	httpClient.executeMethod(postMethod);

        	BufferedReader in = new BufferedReader(
        			new InputStreamReader(postMethod.getResponseBodyAsStream()));
        	String line = null;
        	while((line = in.readLine()) != null)
        		System.out.println(line);

        	in.close();

    	}catch(Exception e) {
    		throw new ComponentExecutionException(e);
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
