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

import java.util.List;
import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.meandre.core.system.components.ext.StreamInitiator;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Component(creator="Lily Dong",
           description="Parses a raw InputStream into RSS entries. Pushes a " +
           		"StreamInitiator object, then iterates over all RSS entries in " +
           		"the stream and pushes them individually, then pushes a " +
           		"StreamTerminator object. Accepts any RSS/ATom feed format.",
           name="RSSParseStream",
           tags="RSS")


public class RSSParseStream implements ExecutableComponent {
    @ComponentInput(description="Read content as stream." +
            "<br>TYPE: java.io.InputStream",
                    name= "inputStream")
    public final static String DATA_INPUT = "inputStream";
    
    @ComponentOutput(description="Output content as stream of syndication entry " +
                     "followed by StreamTerminator." +
                     "<br>TYPES:"+
                     "<br>org.meandre.core.system.components.ext.StreamInitiator;"+
                     "<br>    THEN"+
                     "<br>com.sun.syndication.feed.synd.SyndEntry (multiple times)"+
                     "<br>    THEN"+
                     "<br>org.meandre.core.system.components.ext.StreamTerminator",
                     name="outputObject")        
    public final static String DATA_OUTPUT = "outputObject";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
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
        List entries = feed.getEntries();
        //System.out.println("entries.size: " + entries.size());
        int i = 0;
        cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamInitiator());
        for(final Iterator iter = entries.iterator(); iter.hasNext();) {
            SyndEntry entry = (SyndEntry)iter.next();
            //System.out.println("\tAuthor(" + (i++) + "): " + entry.getDescription().getType());
            cc.pushDataComponentToOutput(DATA_OUTPUT, entry);
        }
        cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamTerminator());
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
