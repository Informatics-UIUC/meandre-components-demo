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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

@Component(creator="Lily Dong",
           description="View Google map.",
           name="GoogleMapViewer",
           tags="google map, visualization",
           mode=Mode.webui)

public class GoogleMapViewer
	implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentProperty(defaultValue=//"",
		//"ABQIAAAAzuMq2M5--KdBKawoLNQWUxQKQLDP3-h3gXPk25Qansm9VUoOPBRzc41cGNCHCFTWrlzNJCHE5Y_9AA", //demo.seasr.org
		"ABQIAAAAzuMq2M5--KdBKawoLNQWUxRi_j0U6kJrkFvY4-OX2XYmEAa76BQS61jzrv4ruAIpkFQs5Qp-fiN3hg", //127.0.0.1
                       description="This property sets Google Maps API key. The default value is only applicable to 127.0.0.1.",
                       name="googleKey")
    final static String DATA_PROPERTY_1 = "googleKey";
	@ComponentProperty(defaultValue="yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
               		   description="This property sets Yahoo API ID. The default value is applicable to all applications.",
               		   name="yahooId")
    final static String DATA_PROPERTY_2 = "yahooId";

    @ComponentInput(description="Read XML doucment.",
              		name= "inputDocument")
    public final static String DATA_INPUT = "inputDocument";

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;

    /** Store Google Maps API key */
    private String googleKey;

    /** Store latitude, longitude and address */
    private Vector<String> lat, lon, location, context;

    /** Store the average values of latitude and longitude */
    private float latAverage, lonAverage;

    /** Store the minimum and maximum values of latitude and longitude */
    float latMin, latMax, lonMin, lonMax ;

    private final static String STRING_DELIMITER = "\n";

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

        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n");
        sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n");
        sb.append("<head>\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n");
        sb.append("<title>Google Map Viewer</title>\n");
        sb.append("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key="+
        		googleKey + "\"\n");
        sb.append("type=\"text/javascript\"></script>\n");
        sb.append("<script type=\"text/javascript\">\n");

        //------------
        sb.append("function toggleVisibility(me){\n");
        	sb.append("var child = me.childNodes.item(1);\n");
        	sb.append("if (child.style.display=='none'){\n");
        		sb.append("child.style.display='';\n");
        	sb.append("}\n");
        	sb.append("else {\n");
        		sb.append("child.style.display='none';\n");
        	sb.append("}\n");
        sb.append("}\n");
        //------------

        sb.append("var lat = new Array();\n");
        for(int i=0; i<lat.size(); i++)
        	sb.append("lat[").append(i).append("]=").append(lat.elementAt(i)).append(";\n");

        sb.append("var lon = new Array();\n");
        for(int i=0; i<lon.size(); i++)
        	sb.append("lon[").append(i).append("]=").append(lon.elementAt(i)).append(";\n");

        sb.append("var loc = new Array();\n");
        for(int i=0; i<location.size(); i++)
        	sb.append("loc[").append(i).append("]=\"").append(location.elementAt(i)).append("\";\n");

        sb.append("var cxt = new Array();\n");
        for(int i=0; i<context.size(); i++)
        	sb.append("cxt[").append(i).append("]=\"").append(context.elementAt(i)).append("\";\n");

        sb.append("var latAverage=").append(latAverage).append(";\n");
        sb.append("var lonAverage=").append(lonAverage).append(";\n");
        sb.append("var latMin=").append(latMin).append(";\n");
        sb.append("var latMax=").append(latMax).append(";\n");
        sb.append("var lonMin=").append(lonMin).append(";\n");
        sb.append("var lonMax=").append(lonMax).append(";\n");

        sb.append("var zooMin=4;\n");
        sb.append("var zooMax=13;\n");

        sb.append("function initialize() {\n");
        sb.append("if (GBrowserIsCompatible()) {\n");

        sb.append("var map = new GMap2(document.getElementById(\"map_canvas\"));\n");

        //-------------
        sb.append("var bounds = new GLatLngBounds();\n");
        //-------------

        sb.append("var p1 = new GLatLng(latMin, lonMax);\n");
 	    sb.append("var p2 = new GLatLng(latMax, lonMin);\n");
 	    sb.append("var bounds = new GLatLngBounds(p1, p2);\n");
 	    sb.append("var zoom = map.getBoundsZoomLevel(bounds);\n");
 	    sb.append("if(zoom > zooMax) zoom = zooMax;\n");
        sb.append("if(zoom < zooMin) zoom = zooMin;\n");

        //sb.append("map.setCenter(new GLatLng(latAverage, lonAverage), zoom);\n");

        //-------------
        sb.append("map.setCenter(new GLatLng(0,0),0);\n");
        //-------------

        sb.append("map.addControl(new GSmallMapControl());\n");
        sb.append("map.addControl(new GMapTypeControl());\n");

        sb.append("function createMarker(point, index) {\n");
        sb.append("var marker = new GMarker(point);\n");

        sb.append("GEvent.addListener(marker, \"click\", function() {\n");
        sb.append("var maxContent = cxt[index];\n");
        sb.append("marker.openInfoWindowHtml('<b>'+loc[index]+'</b>', {maxContent:maxContent, maxTitle:loc[index]});\n");
        sb.append("});\n");
        sb.append("return marker;\n");
        sb.append("}\n");

        sb.append("for (var i=0; i<loc.length; i++) {\n");
        sb.append("var latlng = new GLatLng(lat[i], lon[i]);\n");

        //-------------
        sb.append("bounds.extend(latlng);\n");
        //-------------

        sb.append("map.addOverlay(createMarker(latlng, i));\n");
        sb.append("}\n");

        //-------------
        sb.append("map.setZoom(map.getBoundsZoomLevel(bounds));\n");
        sb.append("map.setCenter(bounds.getCenter());\n");
        //-------------

        sb.append("}\n");
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("</head>\n");

        sb.append("<body onload=\"initialize()\">\n");
        sb.append("<div align=\"center\">\n");
        sb.append("<div id=\"map_canvas\" style=\"width: 800px; height: 500px\"></div>\n");
        sb.append("</div>\n");

        sb.append("<br/>\n");

        sb.append("<div align=\"center\">\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                     sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");

        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
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
    	googleKey = cc.getProperty(DATA_PROPERTY_1);

    	String yahooId = cc.getProperty(DATA_PROPERTY_2);

    	Document doc = (Document)cc.getDataComponentFromInput(DATA_INPUT);

		try {
			doc.getDocumentElement().normalize();
			System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("location");
			System.out.println("Information of all addresses");
			for (int k = 0; k < nodeLst.getLength(); k++) {
				Node fstNode = nodeLst.item(k);
				StringBuffer sb = new StringBuffer();
			    sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
			    sb.append(yahooId);
			    String str = fstNode.getTextContent();
			    /*if(str.contains("\"")) //invalid location
			    	continue;*/
			    str = str.replaceAll(" ", "%20");
			    sb.append("&location=").append(str);

			    URL url = new URL(sb.toString());
			    BufferedReader br = null;
			    try {
		        	br = new BufferedReader(new InputStreamReader(
		        			url.openConnection().getInputStream()));
			    }catch(java.io.IOException ex) {
			    	System.out.println("bad query : " + str);
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
		        float latitude, longitude;
		        while(true) {//valid location
		        	if(s.indexOf("<Latitude>") == -1)
		        		break;

		        	int beginIndex = s.indexOf("<Latitude>") + 10,
		        	    endIndex = s.indexOf("</Latitude>");
		        	latitude = Float.parseFloat(
		        			s.substring(beginIndex, endIndex));
		        	latMin = getMin(latMin, latitude);
		        	latMax = getMax(latMax, latitude);
		        	latAverage += latitude;
		        	lat.add(s.substring(beginIndex, endIndex));

		        	beginIndex = s.indexOf("<Longitude>") + 11;
	        	    endIndex = s.indexOf("</Longitude>");
	        	    longitude = Float.parseFloat(
		        			s.substring(beginIndex, endIndex));
	        	    lonMin = getMin(lonMin, longitude);
	        	    lonMax = getMax(lonMax, longitude);
	        	    lonAverage += longitude;
	        	    lon.add(s.substring(beginIndex, endIndex));

	        	    NamedNodeMap nnp = fstNode.getAttributes();

	        	    String sentence = nnp.getNamedItem("sentence").getNodeValue();
	        	    //------------
	        	    StringTokenizer st = new StringTokenizer(sentence, "|");
	        	    StringBuffer buf = new StringBuffer();
	        	    int nr = 0;
	        	    while(st.hasMoreTokens()) {
	        	    	String nt = st.nextToken();
	        	    	buf.append("<div onclick='toggleVisibility(this)' style='position:relative' ALIGN='LEFT'>Sentence ").append(++nr);
	        	    	buf.append("<span style='display: none' ALIGN='LEFT'><table bgcolor='yellow'><tr><td>").append(nt).append("</td></tr></table></span></div>");
	        	    }
	        	    //------------
	        	    /*sentence = "<p align=left>" + sentence;
	        	    sentence = sentence.replaceAll("[|]", "</p><hr><p align=left>");
	        	    sentence = sentence + "</p>";*/

	        	    location.add(fstNode.getTextContent());
	        	    context.add(buf.toString());//sentence);

	        	    s = s.substring(endIndex+12);
		        }
			}
		} catch (Exception e1) {
			throw new ComponentExecutionException(e1);
		}

		if(location.size() != 0) {
			latAverage /= location.size();
			lonAverage /= location.size();
			System.out.println(latMin + " " + latMax + " " +
					           lonMin + " " + lonMax + " " +
					           latAverage + " " + lonAverage);
		}

		for(int k=0; k<location.size(); k++)
			System.out.println(k + "\t" +
							   location.elementAt(k) + "\t" +
							   lat.elementAt(k)   + "\t" +
							   lon.elementAt(k));

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
     *
     * @param n1 the first number to be compared
     * @param n2 the second number to be compared
     * @return the smaller number between n1 and n2
     */
    private float getMin(float n1, float n2) {
    	if(Float.compare(n1, 0) == 0) //for the first time
    		return n2;

    	if(Float.compare(n1, n2) == 0)
    		return n1;
    	else if(n1 < 0 && n2 > 0)
    		return n1;
    	else if(n1 > 0 && n2 < 0)
    		return n2;
    	else {//same sign
    		if((new Float(n1).toString().compareTo(new Float(n2).toString())) == -1)
    			return n1;
    		else
    			return n2;
    	}
    }

    /**
     *
     * @param n1 the first number to be compared
     * @param n2 the second number to be compared
     * @return the bigger number between n1 and n2
     */
    private float getMax(float n1, float n2) {
    	if(Float.compare(n1, 0) == 0) //for the first time
    		return n2;

    	if(Float.compare(n1, n2) == 0)
    		return n1;
    	else if(n1 < 0 && n2 > 0)
    		return n2;
    	else if(n1 > 0 && n2 < 0)
    		return n1;
    	else {//same sign
    		if((new Float(n1).toString().compareTo(new Float(n2).toString())) == -1)
    			return n2;
    		else
    			return n1;
    	}
    }

	/**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    	lat = new Vector<String>();
    	lon = new Vector<String>();
    	location = new Vector<String>();
    	context = new Vector<String>();
    	latAverage = 0;
    	lonAverage = 0;
    	latMin = 0;//Float.MAX_VALUE;
	    latMax = 0;//-Float.MAX_VALUE;
	    lonMin = 0;//Float.MAX_VALUE;
	    lonMax = 0;//-Float.MAX_VALUE;
    }

    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
