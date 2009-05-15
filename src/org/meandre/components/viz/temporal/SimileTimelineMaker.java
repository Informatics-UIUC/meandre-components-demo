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

package org.meandre.components.viz.temporal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.StringTokenizer;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.SimpleDateFormat;

import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component(creator="Lily Dong",
           description="Generates the necessary HTML and XML files " +
           "for viewing timeline and store them on the local machine. " +
           "The two files will be stored under public/resources/timeline/file/. " +
           "The accompanying javascrpt file, that is examples.js, " +
           "will be stored under public/resources/timeline/js/. " +
           "For fast browse, dates are grouped into different time slices. " +
           "The number of time slices is designed as a property. " +
           "If granularity is not appropriate, adjusts this porperty.",
           name="SimileTimelineGenerator",
           tags="simile, timeline",
           baseURL="meandre://seasr.org/components/")

public class SimileTimelineMaker extends AbstractExecutableComponent
{
	@ComponentProperty(defaultValue="10",
			description="This property specifies the number of time slices.",
            name="numberOfSegments")
    final static String DATA_PROPERTY = "numberOfSegments";

	@ComponentInput(description="Read XML document." +
	            "<br>TYPE: org.w3c.dom.Document",
      			    name= "Document")
    public final static String DATA_INPUT = "Document";

	@ComponentOutput(description="Output URL which are used to store HTML and XML files. "+
			"<br>TYPE: java.lang.String",
	         		 name="Text")
	public final static String DATA_OUTPUT = "Text";

    /** Store document title */
    private String docTitle;

    /** Store the minimum value of year */
    private int minYear;

    /** Store the maximum value of year */
    private int maxYear;

    /** Store the number of time slices */
    private int nrSegments;

    /**
     *
     * @param urlOfXml URL of XML file
     * @return HTML file for viewing timeline
     */
    private String generateHTML(String fileNameOfXml) {
    	StringBuffer sb = new StringBuffer();

        sb.append("<html>\n");
        sb.append("<head>\n");

        sb.append("<script src=\"http://simile.mit.edu/timeline/api/timeline-api.js\" type=\"text/javascript\"></script>\n");
        sb.append("<script src=\"../js/examples.js\" type=\"text/javascript\"></script>\n");

       	sb.append("<script type=\"text/javascript\">\n");
       	sb.append("function toggleVisibility(me){\n");
      	sb.append("var child = me.childNodes.item(1);\n");
      	sb.append("if (child.style.display=='none'){\n");
      	sb.append("child.style.display='';\n");
      	sb.append("}\n");
      	sb.append("else {\n");
      	sb.append("child.style.display='none';\n");
      	sb.append("}\n");
      	sb.append("}\n");

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
        	sb.append("showEventText:	false,\n");
        	sb.append("trackHeight: 	0.5,\n");
        	sb.append("trackGap: 		0.2,\n");
        	sb.append("eventSource:    eventSource,\n");
            sb.append("date:           \"Jan 01 ").append(minYear).append(" 00:00:00 GMT\",\n");
            sb.append("width:          \"30%\",\n");
            sb.append("intervalUnit:   Timeline.DateTime.YEAR,\n");
            sb.append("intervalPixels: 200\n");
        sb.append("})\n");
        sb.append("];\n");

        sb.append("bandInfos[1].syncWith = 0;\n");
        sb.append("bandInfos[1].highlight = true;\n");
        sb.append("bandInfos[1].eventPainter.setLayout(bandInfos[0].eventPainter.getLayout(1));\n");

        sb.append("tl = Timeline.create(document.getElementById(\"my-timeline\"), bandInfos);\n");
        sb.append("Timeline.loadXML(\"").append(fileNameOfXml).append("\", function(xml, url) { eventSource.loadXML(xml, url); });\n");
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

        int nrYears = (maxYear-minYear)/nrSegments; //nrYears indicates the number of years in each segment
        if(nrYears != 0) {
        	sb.append("<br/>");
        	sb.append("<div style=\"width: 100%\">\n");
            sb.append("<table style=\"text-align: center; width: 100%\">\n");
            int year = minYear;
            int i = 0;
            while(!(year>maxYear)) {//every row consists of 10 segments
            	if(i%10 == 0)
            		sb.append("<tr>\n");
            	sb.append("<td><a href=\"javascript:centerTimeline("+year+");\">"+year+"</a></td>\n");
            	if((i+1)%10 == 0)
            		sb.append("</tr>\n");
            	year += nrYears;
            	++i;
            }
            if(i%10 != 0)
            	sb.append("</tr>\n");
            sb.append("</table>\n");
            sb.append("</div>\n");
            sb.append("<br/>");
        }

        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    /**
     *
     * @param doc contains data for generating XML file impromptu
     * @return XML file
     */
    private String generateXML(Document doc) {
    	minYear = Integer.MAX_VALUE;
    	maxYear = Integer.MIN_VALUE;

    	StringBuffer buf = new StringBuffer(); //Store XML
    	buf.append("<data>\n");

		doc.getDocumentElement().normalize();
		docTitle = doc.getDocumentElement().getAttribute("docID");
		getConsoleOut().println("Root element : " + docTitle);
		NodeList nodeLst = doc.getElementsByTagName("date");
		getConsoleOut().println("Information of date");
		for (int k = 0; k < nodeLst.getLength(); k++) {
			Node fstNode = nodeLst.item(k);
			String aDate = fstNode.getTextContent();

			//standardize date
			//getConsoleOut().println("time : " + aDate);

			String month = null,
				   day   = null,
				   year  = null;

			String startMonth = null,
					 endMonth = null;

			Pattern datePattern = Pattern.compile("(january|jan|feburary|feb|march|mar|" + //look for month
					"april|apr|may|june|jun|july|jul|august|aug|september|sept|october|oct|"+
					"november|nov|december|dec)");
			Matcher dateMatcher = datePattern.matcher(aDate);
			if(dateMatcher.find()) { //look for month
				month = dateMatcher.group(1);
			} else { //look for season
				datePattern = Pattern.compile("(spring|summer|fall|winter)");
				dateMatcher = datePattern.matcher(aDate);
				if(dateMatcher.find()) {
					String season = dateMatcher.group(1);
					if(season.equalsIgnoreCase("spring")) {
						startMonth = "Apr 01";
						endMonth = "June 30";
					} else if(season.equalsIgnoreCase("summer")) {
						startMonth = "July 01";
						endMonth = "Sept 30";
					} else if(season.equalsIgnoreCase("fall")) {
						startMonth = "Oct 01";
						endMonth = "Dec 31";
					} else { //winter
						startMonth = "Jan 01";
						endMonth = "Mar 31";
					}
				}
			}

			datePattern = Pattern.compile("(\\b\\d{1}\\b)"); //look for day	like 5
			dateMatcher = datePattern.matcher(aDate);
			if(dateMatcher.find()) {
				day = dateMatcher.group(1);
			} else {
				datePattern = Pattern.compile("(\\b\\d{2}\\b)"); //look for day	like 21
				dateMatcher = datePattern.matcher(aDate);
				if(dateMatcher.find()) {
					day = dateMatcher.group(1);
				}
			}

			datePattern = Pattern.compile("(\\d{4})"); //look for year
			dateMatcher = datePattern.matcher(aDate);
			if(dateMatcher.find()) { //look for year
				NamedNodeMap nnp = fstNode.getAttributes();
		        String sentence = nnp.getNamedItem("sentence").getNodeValue();

		        //escape invalid xml characters
		        sentence = sentence.replaceAll("[&]",  "&amp;");
		        sentence = sentence.replaceAll("[<]",  "&lt;");
		        sentence = sentence.replaceAll("[>]",  "&gt;");
		        sentence = sentence.replaceAll("[\"]", "&quot; ");
		        sentence = sentence.replaceAll("[\']", "&#39;");

		        StringTokenizer st = new StringTokenizer(sentence, "|");
		        StringBuffer sb = new StringBuffer();
		        int nr = 0;
		        while(st.hasMoreTokens()) {
		        	String nt = st.nextToken();
		        	   sb.append("&lt;div onclick=&#39;toggleVisibility(this)&#39; style=&#39;position:relative&#39; ALIGN=&#39;LEFT&#39;&gt;Sentence ").append(++nr);
		        	   sb.append("&lt;span style=&#39;display: none&#39; ALIGN=&#39;LEFT&#39;&gt; &lt;table bgcolor=&#39; yellow&#39; &gt;&lt;tr&gt;&lt;td&gt;").append(nt).append("&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;&lt;/span&gt;&lt;/div&gt;");
		        	}
		        sentence = sb.toString();

				year = dateMatcher.group(1);
				minYear = Math.min(minYear, Integer.parseInt(year));
				maxYear = Math.max(maxYear, Integer.parseInt(year));
				//year or month year or month day year
				if(day == null)
					if(month == null) { //season year
						if(startMonth != null) {//spring or summer or fall or winter year
							buf.append("<event start=\"").append(startMonth + " " + year).append("\" end=\"").append(endMonth + " " + year).append("\" title=\"").append(/*year*/aDate).append("\">\n").append(sentence).append("\n");
							buf.append("</event>\n");
						} else { //year
							//if(Integer.parseInt(year) != 1832) {
							buf.append("<event start=\"").append(year).append("\" title=\"").append(/*year*/aDate).append("\">\n").append(sentence).append("\n");
							buf.append("</event>\n");//}
						}
					} else { //month year
						String startDay = month + " 01";
						int m = 1;
						if(month.startsWith("feb"))
							m = 2;
						else if(month.startsWith("mar"))
							m = 3;
						else if(month.startsWith("apr"))
							m = 4;
						else if(month.startsWith("may"))
							m = 5;
						else if(month.startsWith("jun"))
							m = 6;
						else if(month.startsWith("jul"))
							m = 7;
						else if(month.startsWith("aug"))
							m = 8;
						else if(month.startsWith("sept"))
							m = 9;
						else if(month.startsWith("oct"))
							m = 10;
						else if(month.startsWith("nov"))
							m = 11;
						else if(month.startsWith("dec"))
							m = 12;
						int y = Integer.parseInt(year);
						int numberOfDays = 31;
						if (m == 4 || m == 6 || m == 9 || m == 11)
							  numberOfDays = 30;
						else if (m == 2) {
							boolean isLeapYear = (y % 4 == 0 && y % 100 != 0 || (y % 400 == 0));
							if (isLeapYear)
								numberOfDays = 29;
							else
								numberOfDays = 28;
						}
						String endDay = month + " " + Integer.toString(numberOfDays);
						buf.append("<event start=\"").append(startDay + " " + year).append("\" end=\"").append(endDay + " " + year).append("\" title=\"").append(/*year*/aDate).append("\">\n").append(sentence).append("\n");
				    	buf.append("</event>\n");
					}
				else {
					if(month == null) {//year
						buf.append("<event start=\"").append(year).append("\" title=\"").append(/*year*/aDate).append("\">\n").append(sentence).append("\n");
						buf.append("</event>\n");
					} else { //month day month
						buf.append("<event start=\"").append(month + " " + day + " " + year).append("\" title=\"").append(/*month + " " + day + " " + year*/aDate).append("\">\n").append(sentence).append("\n");
						buf.append("</event>\n");
					}
				}
			}
		}
		buf.append("</data>");

    	return buf.toString();
    }

	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    /* (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	nrSegments = Integer.parseInt(cc.getProperty(DATA_PROPERTY));

    	Document doc = (Document)cc.getDataComponentFromInput(DATA_INPUT);

   	   	String dir = cc.getPublicResourcesDirectory() + File.separator;
   	   	dir += "timeline" + File.separator;
   	   	dir += "file" + File.separator;

   		String webUiUrl = cc.getWebUIUrl(true).toString();
   		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String fileNameOfHtml = "myhtml" + formatter.format(now) + ".html",
		       fileNameOfXml = "myxml" + formatter.format(now) + ".xml";
    	String urlOfHtml = webUiUrl + "public/resources/timeline/file/" + fileNameOfHtml,
    	       urlOfXml  = webUiUrl + "public/resources/timeline/file/" + fileNameOfXml;

   	   	try {
   	   		PrintWriter out = new PrintWriter(
	   				new BufferedWriter(new FileWriter(dir+fileNameOfXml)));
	   		out.write(generateXML(doc));
	   		out.flush();

   	   		out	= new PrintWriter(
   	   					new BufferedWriter(new FileWriter(dir+fileNameOfHtml)));
   	   		out.write(generateHTML(fileNameOfXml));
   	   		out.flush();

   	   		out.close();
     	} catch(Exception ex) {}

    	cc.pushDataComponentToOutput(DATA_OUTPUT, urlOfHtml);
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
