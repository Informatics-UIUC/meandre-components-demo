/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.meandre.demo.components.io;

import java.io.InputStream;
import java.net.URL;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

@Component(creator="Lily Dong",
           description="Fetch content of the specified URL. " +
           "For the local file, the URL should be file:///F:/... " +
           "For the remote file, the URL should be http://... or ftp://..." +
           "If the access needs authentication, username and password should be provided " +
           "as the properties, otherwise ignoring them.",
           name="ActiveURLFetcher",
           tags="URL, stream")

public class ActiveURLFetcher implements ExecutableComponent {
    @ComponentInput(description="URL to be fetched.",
                    name= "inputUrl")
    public final static String DATA_INPUT = "inputUrl";
    
    @ComponentOutput(description="Output content of the specified URL as stream.",
                     name="outputStream")             
    public final static String DATA_OUTPUT = "outputStream";
    
    @ComponentProperty(defaultValue="",
                       description="This property sets username.",
                       name="username")
    final static String DATA_PROPERTY_1 = "username";
    @ComponentProperty(defaultValue="",
                       description="This property sets password.",
                       name="password")
    final static String DATA_PROPERTY_2 = "password";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        String username = cc.getProperty(DATA_PROPERTY_1),
               password = cc.getProperty(DATA_PROPERTY_2);        
        String inputUrl = (String)cc.getDataComponentFromInput(DATA_INPUT);
        
        InputStream is = null;
        
        if((username == null || username.length() == 0) || 
           (password == null || password.length() == 0)) {//no authentication
            try {
                URL url = new URL(inputUrl);
                is = url.openConnection().getInputStream();
                cc.pushDataComponentToOutput(DATA_OUTPUT, is);
                //is can not be closed here, otherwise java.net.SocketException: socket closed
                //the final client must close this stream.
            }catch(Exception e) {
                try {
                    if(is != null) 
                        is.close();
                }catch(java.io.IOException ioex) {}
                throw new ComponentExecutionException(e);
            }
        } else {
            try {
                URL url = new URL(inputUrl);
                HostConfiguration hostConfig = new HostConfiguration();
                hostConfig.setHost(url.getHost(), url.getPort());

                HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
                httpClient.setHostConfiguration(hostConfig);
            
                Credentials credentials = new UsernamePasswordCredentials(username, password);

                if (credentials != null) {
                    httpClient.getParams().setAuthenticationPreemptive(true);
                    httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY), credentials);
                }
                
                GetMethod getMethod = new GetMethod(inputUrl);
                httpClient.executeMethod(getMethod);

                is = getMethod.getResponseBodyAsStream();
                cc.pushDataComponentToOutput(DATA_OUTPUT, is);
            } catch(Exception e) {
                try {
                    if(is != null) 
                        is.close();
                }catch(java.io.IOException ioex) {}
                throw new ComponentExecutionException(e);
            }
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
