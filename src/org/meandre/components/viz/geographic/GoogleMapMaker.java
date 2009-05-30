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

package org.meandre.components.viz.geographic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import java.util.StringTokenizer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

@Component(creator="Lily Dong",
           description="Calculates latitude and longitude for an address contained in the input XML document.",
           name="Google Map Generator",
           tags="google map, latitude, longitude",
           baseURL="meandre://seasr.org/components/")

public class GoogleMapMaker	extends AbstractExecutableComponent
{
	@ComponentProperty(defaultValue="yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
               		   description="This property sets Yahoo API ID. The default value is applicable to all applications.",
               		   name="yahooId")
    final static String DATA_PROPERTY = "yahooId";

    @ComponentInput(description="Read XML document." +
                "<br>TYPE: org.w3c.dom.Document",
              		name= "Document")
    public final static String DATA_INPUT = "Document";

    @ComponentOutput(description="Output latitude."+
			"<br>TYPE: java.util.Vector",
	         		 name="vectorOfLatitude")
	public final static String DATA_OUTPUT_1 = "vectorOfLatitude";

    @ComponentOutput(description="Output longitude."+
			"<br>TYPE: java.util.Vector",
	         		 name="vectorOfLongitude")
	public final static String DATA_OUTPUT_2 = "vectorOfLongitude";

    @ComponentOutput(description="Output location."+
			"<br>TYPE: java.util.Vector",
	         		 name="vectorOfLocation")
	public final static String DATA_OUTPUT_3 = "vectorOfLocation";

    @ComponentOutput(description="Output context."+
			"<br>TYPE: java.util.Vector",
	         		 name="vectorOfContext")
	public final static String DATA_OUTPUT_4 = "vectorOfContext";

    /** Store latitude, longitude and address */
    private Vector<String> lat, lon, location, context;

    private final static String STRING_DELIMITER = "\n";

	/** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	//prepare for fresh start
    	if(lat!=null && lat.size()!=0)
    		lat.clear();
    	if(lon!=null && lon.size()!=0)
    		lon.clear();
    	if(location!=null && location.size()!=0)
    		location.clear();
    	if(context!=null && context.size()!=0)
    		context.clear();

    	String yahooId = cc.getProperty(DATA_PROPERTY);

    	Document doc = (Document)cc.getDataComponentFromInput(DATA_INPUT);

		try {
			doc.getDocumentElement().normalize();
			console.fine("Root element : " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("location");
			console.fine("Information of all addresses");
			for (int k = 0; k < nodeLst.getLength(); k++) {
				Node fstNode = nodeLst.item(k);

				String str = fstNode.getTextContent();

				Pattern p = Pattern.compile("[a-zA-Z .]+");
		 		Matcher m = p.matcher(str);
		 		if(!m.matches()) //illegal characters
		 			continue;

				StringBuffer sb = new StringBuffer();
			    sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
			    sb.append(yahooId);
			    //String str = fstNode.getTextContent();
			    str = str.replaceAll(" ", "%20");
			    sb.append("&location=").append(str);

			    URL url = new URL(sb.toString());
			    BufferedReader br = null;
			    try {
		        	br = new BufferedReader(new InputStreamReader(
		        			url.openConnection().getInputStream()));
			    }catch(java.io.IOException ex) {
			    	console.fine("bad query : " + str);
			    	br = null;
			    }
			    if(br == null)
			    	continue;
		        StringBuffer buffer = new StringBuffer();
		        String line;
		        while((line = br.readLine())!= null) {
		        	line = line.trim();
		            if(line.length() == 0)
		            	continue;
		            buffer.append(line).append(STRING_DELIMITER);
		        }
		        br.close();

		        String s = buffer.toString();
		        while(true) {//valid location
		        	if(s.indexOf("<Latitude>") == -1)
		        		break;

		        	int beginIndex = s.indexOf("<Latitude>") + 10,
		        	    endIndex = s.indexOf("</Latitude>");
		        	lat.add(s.substring(beginIndex, endIndex));

		        	beginIndex = s.indexOf("<Longitude>") + 11;
	        	    endIndex = s.indexOf("</Longitude>");
	        	    lon.add(s.substring(beginIndex, endIndex));

	        	    NamedNodeMap nnp = fstNode.getAttributes();

	        	    String sentence = nnp.getNamedItem("sentence").getNodeValue();

	        	    StringTokenizer st = new StringTokenizer(sentence, "|");
	        	    StringBuffer buf = new StringBuffer();
	        	    int nr = 0;
	        	    while(st.hasMoreTokens()) {
	        	    	String nt = st.nextToken();
	        	    	int pos = nt.toLowerCase().indexOf(fstNode.getTextContent());
			        	int offset = pos+fstNode.getTextContent().length();
			        	nt = new StringBuffer(nt).insert(offset, "</font>").toString();
			        	offset = pos;
			        	nt = new StringBuffer(nt).insert(offset, "<font color='red'>").toString();
	        	    	buf.append("<div onclick='toggleVisibility(this)' style='position:relative' ALIGN='LEFT'><b>Sentence ").append(++nr).append("</b>");
	        	    	buf.append("<span style='display: ' ALIGN='LEFT'><table><tr><td>").append(nt).append("</td></tr></table></span></div>");
	        	    }

	        	    /*sentence = "<p align=left>" + sentence;
	        	    sentence = sentence.replaceAll("[|]", "</p><hr><p align=left>");
	        	    sentence = sentence + "</p>";*/

	        	    location.add(fstNode.getTextContent()+"("+nr+")");
	        	    context.add(buf.toString());//sentence);

	        	    s = s.substring(endIndex+12);
		        }
			}
		} catch (Exception e1) {
			throw new ComponentExecutionException(e1);
		}

		cc.pushDataComponentToOutput(DATA_OUTPUT_1, lat);
		cc.pushDataComponentToOutput(DATA_OUTPUT_2, lon);
		cc.pushDataComponentToOutput(DATA_OUTPUT_3, location);
		cc.pushDataComponentToOutput(DATA_OUTPUT_4, context);
    }

	/**
     * Call at the end of an execution flow.
     */
    public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
    	lat = new Vector<String>();
    	lon = new Vector<String>();
    	location = new Vector<String>();
    	context = new Vector<String>();
    }

    /**
     * Called when a flow is started.
     */
    public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
    }
}
