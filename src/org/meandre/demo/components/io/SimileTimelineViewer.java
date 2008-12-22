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

import java.io.File;
import java.net.URL;
import java.util.concurrent.Semaphore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component(creator="Lily Dong",
           description="Visualize time-based events.",
           name="TimelineViewer",
           tags="simile, timeline",
           mode=Mode.webui)

public class SimileTimelineViewer
	implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentProperty(defaultValue="http://norma.ncsa.uiuc.edu/public-dav/applets/myfile.html",
            		   description="This property sets URL for storing html generated by this component. " +
            		   "It must be in the same directory with xml file.",
            		   name="html_loc")
    final static String DATA_PROPERTY_1 = "html_loc";
	@ComponentProperty(defaultValue="http://norma.ncsa.uiuc.edu/public-dav/applets/myxml.xml",
    		       	   description="This property sets URL for storing xml generated by this component. " +
    		       	   "It must be in the same directory with html file.",
    		       	   name="xml_loc")
    final static String DATA_PROPERTY_2 = "xml_loc";

	@ComponentInput(description="Read XML doucment.",
      			    name= "inputDocument")
    public final static String DATA_INPUT = "inputDocument";

	/** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;

    /** Store URLs */
    private String htmLocation, xmLocation;

    /** Store XML for date */
    StringBuffer buf;

    /** Store the minimum year */
    int minYear;

    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem raised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }

    /** A simple message.
    *
    * @return The html containing the page
    */
    private String getViz() {
    	StringBuffer sb = new StringBuffer();

    	sb.append("<html>\n");
    	sb.append("<body>\n");

    	sb.append("<iframe src=\"").append(htmLocation).append("\" width=\"100%\" height=\"30%\" FRAMEBORDER=0>\n");
    	sb.append("</iframe>\n");

    	sb.append("<div align=\"center\">\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
        		sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");

    	sb.append("</body>\n");
    	sb.append("</html>\n");

    	return sb.toString();
    }

    /**
     * Upload html and data(xml) to URLs.
     */
    private void upload() {
        StringBuffer sb = new StringBuffer();

        sb.append("<html>\n");
        sb.append("<head>\n");

        sb.append("<script src=\"http://simile.mit.edu/timeline/api/timeline-api.js\" type=\"text/javascript\"></script>\n");

      	sb.append("<script type=\"text/javascript\">\n");
      	sb.append("var tl;\n");
        sb.append("function onLoad() {\n");
        sb.append("var eventSource = new Timeline.DefaultEventSource();\n");

        sb.append("var bandInfos = [\n");
        sb.append("Timeline.createBandInfo({\n");
        sb.append("eventSource:    eventSource,\n");
              sb.append("date:           \"Jan 01 ").append(minYear).append(" 00:00:00 GMT\",\n");
              sb.append("width:          \"70%\",\n");
              sb.append("intervalUnit:   Timeline.DateTime.MONTH,\n");
              sb.append("intervalPixels: 100\n");
        sb.append("}),\n");
        sb.append("Timeline.createBandInfo({\n");
        sb.append("eventSource:    eventSource,\n");
              sb.append("date:           \"Jan 01 ").append(minYear).append(" 00:00:00 GMT\",\n");
              sb.append("width:          \"30%\",\n");
              sb.append("intervalUnit:   Timeline.DateTime.YEAR,\n");
              sb.append("intervalPixels: 200\n");
        sb.append("})\n");
        sb.append("];\n");

        sb.append("bandInfos[1].syncWith = 0;\n");
        sb.append("bandInfos[1].highlight = true;\n");

        sb.append("tl = Timeline.create(document.getElementById(\"my-timeline\"), bandInfos);\n");
        int beginIndex = xmLocation.lastIndexOf("/")+1;
        sb.append("Timeline.loadXML(\"").append(xmLocation.substring(beginIndex)).append("\", function(xml, url) { eventSource.loadXML(xml, url); });\n");
      	sb.append("}\n");

      	sb.append("var resizeTimerID = null;\n");
      	sb.append("function onResize() {\n");
          sb.append("if (resizeTimerID == null) {\n");
              sb.append("resizeTimerID = window.setTimeout(function() {\n");
                  sb.append("resizeTimerID = null;\n");
                  sb.append("tl.layout();\n");
              sb.append("}, 500);\n");
          sb.append("}\n");
      	sb.append("}\n");

      	sb.append("</script>\n");

        sb.append("</head>\n");
        sb.append("<body onload=\"onLoad();\" onresize=\"onResize();\">\n");
        sb.append("<div id=\"my-timeline\" style=\"height: 150px; border: 1px solid #aaa\"></div>\n");

        sb.append("</body>\n");
        sb.append("</html>\n");

        /*StringBuffer buf = new StringBuffer();
        buf.append("<data>\n");
        buf.append("<event start=\"May 28 2006 09:00:00 GMT\" end=\"Jun 15 2006 09:00:00 GMT\" isDuration=\"true\" title=\"Writing Timeline documentation\" image=\"http://simile.mit.edu/images/csail-logo.gif\">\n");
        buf.append("A few days to write some documentation for &lt;a href=\"http://simile.mit.edu/timeline/\"&gt;Timeline&lt;/a&gt;.\n");
        buf.append("</event>\n");

        buf.append("<event start=\"Jun 16 2006 00:00:00 GMT\" end=\"Jun 26 2006 00:00:00 GMT\" title=\"Friend's wedding\">\n");
        buf.append("I'm not sure precisely when my friend's wedding is.\n");
        buf.append("</event>\n");

        buf.append("<event start=\"Aug 02 2006 00:00:00 GMT\" title=\"Trip to Illinois\" link=\"http://travel.yahoo.com/\">\n");
        buf.append("Woohoo!\n");
        buf.append("</event>\n");
        buf.append("</data>\n");*/

        try {
        	String inputUrl = htmLocation;
        	URL url = new URL(inputUrl);
        	HostConfiguration hostConfig = new HostConfiguration();
        	hostConfig.setHost(url.getHost(), url.getPort());
        	HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
        	httpClient.setHostConfiguration(hostConfig);
        	PutMethod putMethod = new PutMethod(inputUrl);
        	putMethod.setRequestEntity(
        			new ByteArrayRequestEntity(sb.toString().getBytes(), "text/plain"));
        	httpClient.executeMethod(putMethod);

        	inputUrl = xmLocation;
        	url = new URL(inputUrl);
        	hostConfig.setHost(url.getHost(), url.getPort());
        	httpClient.setHostConfiguration(hostConfig);
        	putMethod = new PutMethod(inputUrl);
        	putMethod.setRequestEntity(
        			new ByteArrayRequestEntity(buf.toString().getBytes(), "text/plain"));
        	httpClient.executeMethod(putMethod);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arose during the call back
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
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
    	htmLocation = cc.getProperty(DATA_PROPERTY_1);
    	xmLocation = cc.getProperty(DATA_PROPERTY_2);

    	Document doc = (Document)cc.getDataComponentFromInput(DATA_INPUT);
    	buf = new StringBuffer();
    	buf.append("<data>\n");
    	try {
			doc.getDocumentElement().normalize();
			System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("date");
			System.out.println("Information of date");
			for (int k = 0; k < nodeLst.getLength(); k++) {
				Node fstNode = nodeLst.item(k);
				String aDate = fstNode.getTextContent();

				//standardize date
				System.out.println("time : " + aDate);

				String month = null,
				       day = null,
				       year = null;

				Pattern datePattern = Pattern.compile("(January|Jan|Feburary|Feb|March|Mar|" + //look for month
						"April|Apr|May|June|July|August|Aug|September|Sept|October|Oct|"+
						"November|Nov|December|Dec)");
				Matcher dateMatcher = datePattern.matcher(aDate);
				if(dateMatcher.find()) {
					month = dateMatcher.group(1);
					System.out.println("\tMonth is:  " + dateMatcher.group(1));
				}

				datePattern = Pattern.compile("(\\b\\d{1}\\b)"); //look for day	like 5
				dateMatcher = datePattern.matcher(aDate);
				if(dateMatcher.find()) {
					day = dateMatcher.group(1);
					System.out.println("\tDay is:  " + dateMatcher.group(1));
				} else {
					datePattern = Pattern.compile("(\\b\\d{2}\\b)"); //look for day	like 21
					dateMatcher = datePattern.matcher(aDate);
					if(dateMatcher.find()) {
						day = dateMatcher.group(1);
						System.out.print("\tDay is:  " + dateMatcher.group(1) + "\n");
					}
				}

				datePattern = Pattern.compile("(\\d{4})"); //look for year
				dateMatcher = datePattern.matcher(aDate);
				if(dateMatcher.find()) {
					NamedNodeMap nnp = fstNode.getAttributes();
		        	String value = nnp.getNamedItem("sentence").getNodeValue();
		        	System.out.println("value = " + value);

					year = dateMatcher.group(1);
					minYear = Math.min(minYear, Integer.parseInt(year));
					System.out.println("\tYear is:  " + dateMatcher.group(1));
					//year or month year or month day year
					if(day == null) //month year
						if(month == null) {//year
							buf.append("<event start=\"").append(year).append("\" title=\"").append(year+" | "+value).append("\">\n");
				    		buf.append("</event>\n");
						} else { //month year
							buf.append("<event start=\"").append(month + " " + year).append("\" title=\"").append(month + " " + year+" | "+value).append("\">\n");
				    		buf.append("</event>\n");
						}
					else {
						if(month == null) {//year
							buf.append("<event start=\"").append(year).append("\" title=\"").append(year+" | "+value).append("\">\n");
							buf.append("</event>\n");
						} else { //month day month
							buf.append("<event start=\"").append(month + " " + day + " " + year).append("\" title=\"").append(month + " " + day + " " + year+" | "+value).append("\">\n");
							buf.append("</event>\n");
						}
					}
				}
			}
			buf.append("</data>");
			System.out.println(buf.toString());
    	} catch (Exception e1) {
			throw new ComponentExecutionException(e1);
		}

        upload();

    	try {
    		sInstanceID = cc.getExecutionInstanceID();
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }

	/**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    	minYear = Integer.MAX_VALUE;
    }

    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
