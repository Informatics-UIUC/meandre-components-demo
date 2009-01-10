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


package org.meandre.components.io.rss;

import java.io.InputStream;

import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.XmlReader;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Parses a raw InputStream into a RSS SyndFeed object " +
           "containing all the rss entries. Accepts any RSS/ATom feed " +
           "type as stream.",
           name="RSSParser",
           tags="RSS",
           dependency={"rome-1.0RC1.jar", "jdom-1.0.jar"},
           baseURL="meandre://seasr.org/components/")

public class RSSParser implements ExecutableComponent {
    @ComponentInput(description="Read content as stream."+
            "<br> TYPE: java.io.InputStream",
                    name= "Stream")
    public final static String DATA_INPUT = "Stream";
    
    @ComponentOutput(description="Output content as syndication feed." +
            "<br> TYPE: com.sun.syndication.feed.synd.SyndFeed",
                     name="Object")        
    public final static String DATA_OUTPUT = "Object";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        InputStream is = (InputStream)cc.getDataComponentFromInput(DATA_INPUT);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed;
        try {
            feed = input.build(new XmlReader(is));
        }catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
        
        /*System.out.println("Title: " + feed.getTitle());
        System.out.println("Author: " + feed.getAuthor());
        System.out.println("Description: " + feed.getDescription());
        System.out.println("Pub date: " + feed.getPublishedDate());
        System.out.println("Copyrignt: " + feed.getCopyright());*/
        
        cc.pushDataComponentToOutput(DATA_OUTPUT, feed);
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
