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

package org.meandre.components.viz.text;

import java.util.concurrent.Semaphore;
import java.util.Date;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.meandre.core.system.components.ext.StreamInitiator;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

@Component(creator="Lily Dong",
           description="Collects a stream of RSS SyndFeed or SyndEntry " +
           		"objects, and then displays them in a webpage when a " +
           		"StreamTerminator object is input. The first input of the " +
           		"stream can be a StreamInitiator.",
           name="RSSEntryAggregatorViz",
           tags="RSS, visualization",
           mode=Mode.webui)

public class RSSAggregatorViz 
    implements ExecutableComponent, WebUIFragmentCallback{
    @ComponentInput(description="Read RSS content as SyndFeed or SyndEntry." +
            "<br>TYPE:" +
            "<br>org.meandre.core.system.components.ext.StreamInitiator" +
            "<br>    THEN:" +
            "<br>com.sun.syndication.feed.synd.SyndEntry" +
            "<br>    OR:" +
            "<br>com.sun.syndication.feed.synd.SyndFeed" +
            "<br>    THEN:" +
            "<br>org.meandre.core.system.components.ext.StreamTerminator",
                    name= "Object")
    public final static String DATA_INPUT = "Object";
    
    @ComponentOutput(description="Output a vector of RSS feeds or entries.",
                     name="Object")        
    public final static String DATA_OUTPUT = "Object";
    
    /** 
     * The blocking semaphore 
     */
    private Semaphore sem = new Semaphore(1,true);

    /** 
     * The instance ID 
     */
    private String sInstanceID = null;
    
    /**
     * Aggregator for output
     */
    private Vector<SyndEntry> aggregator;
    
    /** A simple message.
    *
    * @return The html containing the page
    */
    private String getViz() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<?xml-stylesheet type=\"text/xsl\"  href=\"#\"?>\n");
        sb.append("<xsl:stylesheet\n");
        sb.append("xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n");
        sb.append("xmlns:srw=\"http://www.seasr.org\"\n");
        sb.append("xmlns:mx=\"http://www.seasr.org\"\n");
        sb.append("version=\"1.0\"\n");
        sb.append(">\n");
        sb.append("<xsl:template match=\"xsl:stylesheet\">\n");
        sb.append("<xsl:apply-templates select=\"srw:feed\"/>\n");
        sb.append("</xsl:template>\n");
        sb.append("<xsl:template match=\"srw:feed\">\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<style type=\"text/css\">\n");
        //sb.append("body { background-color:#F0F0F0; font: 9pt Verdana, Arial, \"Arial Unicode MS\", Helvetica, sans-serif;}\n");
        sb.append("table.display { font-family:arial; background-color:#CDCDCD; font-size:10pt; position:relative;left:15pt; border:none; padding:0;}\n");
        sb.append("th.odd {background-color: #e6EEEE;}\n");
        sb.append("tr.even { background-color:#FFF;}\n");
        sb.append("tr.odd { background-color:#F0F0F6;}\n");
        sb.append("</style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        
        sb.append("<div align=\"center\">\n");
        sb.append("<h1>Contents of Entries</h1>\n");
        sb.append("</div>\n");
        
        sb.append("<xsl:apply-templates select=\"srw:entries\"/>\n"); 
                    
        sb.append("<div align=\"center\">\n");
        sb.append("<table align=\"center\"><font size=\"2\"><a id=\"url\" href=\"/" +
                sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");
           
        sb.append("</body>\n");
        sb.append("</html>\n");
        sb.append("</xsl:template>\n");

        sb.append("<xsl:template match=\"srw:entries\">\n");      
        sb.append("<p>\n");
        sb.append("<table class=\"display\">\n");
        sb.append("<tr><th class=\"odd\">Title</th><th class=\"odd\">Content</th></tr>\n");
        sb.append("<xsl:for-each select=\"//mx:entry\">\n");        
        sb.append("<xsl:choose>\n");
        sb.append("<xsl:when test=\"position() mod 2\">\n");
        sb.append("<tr class=\"even\">\n");
        sb.append("<td><xsl:value-of select=\"./mx:title\"/></td>\n");
        sb.append("<td><xsl:value-of select=\"./mx:content\"/></td>\n");
        sb.append("</tr>\n");
        sb.append("</xsl:when>\n");
        sb.append("<xsl:otherwise>\n");
        sb.append("<tr class=\"odd\">\n");
        sb.append("<td><xsl:value-of select=\"./mx:title\"/></td>\n");
        sb.append("<td><xsl:value-of select=\"./mx:content\"/></td>\n");
        sb.append("</tr>\n");
        sb.append("</xsl:otherwise>\n");
        sb.append("</xsl:choose>\n");             
        sb.append("</xsl:for-each>\n");       
        sb.append("</table>\n");
        sb.append("</p>\n");
        sb.append("</xsl:template>\n");
           
        sb.append("<feed xmlns=\"http://www.seasr.org\" xmlns:mx=\"http://www.seasr.org\">\n");

        StringBuffer sbEntries = new StringBuffer("<entries>\n");
        
        for(int index=0; index<aggregator.size(); index++)
            addEntry((SyndEntry)aggregator.elementAt(index), sbEntries);
        
        sb.append(sbEntries).append("</entries>\n");
        
        sb.append("</feed>\n");
        sb.append("</xsl:stylesheet>\n");
        
        return sb.toString();
    }
    
    /**
     * 
     * @param s input string
     * @return s if s is not null or empty string, otherwise warning information.
     */
    private String getValidValue(String s) {
        return (s == null || s.length() == 0)? "no data available": s;
    }
    
    /**
     * 
     * @param e input entry
     * @param b output buffer
     */
    private void addEntry(SyndEntry e, StringBuffer b) {
        b.append("<mx:entry>\n");
        String title = e.getTitle();
        title = (title.indexOf("&") != -1)? title.replaceAll("&", "and") : title;
        b.append("<mx:title>").
          append(title).
          append("</mx:title>\n");
        b.append("<mx:content>").
          append(e.getDescription().getValue()).
          append("</mx:content>\n");
        b.append("</mx:entry>\n");
    }
    
    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.setContentType("text/xml");
            response.getWriter().println(getViz());
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }
    
    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arises during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        if ( sDone!=null ) {
            sem.release();
        }
        else
            emptyRequest(response);
    }
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        Object inputObject = cc.getDataComponentFromInput(DATA_INPUT);
        
        if(inputObject instanceof StreamInitiator) //start of stream
            return;
        else if(inputObject instanceof StreamTerminator) {//end of stream      
            try {
                sInstanceID = cc.getExecutionInstanceID();
                sem.acquire();
                cc.startWebUIFragment(this);
                sem.acquire();
                cc.stopWebUIFragment(this);
            } catch (Exception e) {
                throw new ComponentExecutionException(e);
            }  
            cc.pushDataComponentToOutput(DATA_OUTPUT, aggregator);
        } else if(inputObject instanceof SyndFeed) {   
            List<SyndEntry> list= ((SyndFeed)inputObject).getEntries();
            for(final Iterator it = list.iterator(); it.hasNext();)
                aggregator.add((SyndEntry)it.next());
        } else if(inputObject instanceof SyndEntry)
            aggregator.add((SyndEntry)inputObject);
    }
    
    /**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
        aggregator = new Vector<SyndEntry>();
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
