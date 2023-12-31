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

package org.meandre.components.io.url;

import java.net.URL;
import java.io.InputStream;

import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;

@Component(creator="Lily Dong",
           description="Fetches content of the specified URL as an InputStream " +
           		"object.  This Component opens a handle to the resource ready " +
           		"for streaming (no data is necessarily downloaded until the " +
           		"created Stream is read from). For a local file, the URL " +
           		"should be file:///F:/...  For a remote file, the URL should " +
           		"be http://... or ftp://...  ",
           name="URLFetcher",
           tags="URL, stream",
           baseURL="meandre://seasr.org/components/")

public class URLFetcher extends AbstractExecutableComponent
{
    @ComponentProperty(defaultValue="http://www.ibm.com/developerworks/views/java/rss/libraryview.jsp?feed_by=rss",
                       description="This property sets the URL.",
                       name="location")
    final static String DATA_PROPERTY = "location";

    @ComponentOutput(description="Output content of the specified URL as stream." +
            "<br>TYPE: java.io.InputStream",
                     name="Stream")

    public final static String DATA_OUTPUT = "Stream";

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
        InputStream is = null;
        try {
            URL url = new URL(cc.getProperty(DATA_PROPERTY));
            try {
                is = url.openConnection().getInputStream();
                cc.pushDataComponentToOutput(DATA_OUTPUT, is);
                //is can not be closed here, otherwise java.net.SocketException: socket closed
                //the final client must close this stream.
            }catch(java.io.IOException e) {
                try {
                    if(is != null)
                        is.close();
                }catch(java.io.IOException ioex) {}
                throw new Exception(e);
            }
        } catch(java.net.MalformedURLException e) {
            throw new Exception(e);
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
