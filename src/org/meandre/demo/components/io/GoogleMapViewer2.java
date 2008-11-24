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

@Component(creator="Lily Dong",
           description="View Google map.",
           name="GoogleMapViewer2",
           tags="google map, visualization",
           mode=Mode.webui)

public class GoogleMapViewer2 
	implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentProperty(defaultValue="ABQIAAAAzuMq2M5--KdBKawoLNQWUxRi_j0U6kJrkFvY4-OX2XYmEAa76BQS61jzrv4ruAIpkFQs5Qp-fiN3hg",
                       description="This property sets Google Maps API key.",
                       name="googleKey")
    final static String DATA_PROPERTY_1 = "googleKey";
	@ComponentProperty(defaultValue="yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--",
               		   description="This property sets Yahoo API ID.",
               		   name="yahooId")
    final static String DATA_PROPERTY_2 = "yahooId";
	
    @ComponentInput(description="Read content as stream.",
              		name= "inputStream")
    public final static String DATA_INPUT = "inputStream";
    
    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;
    
    /** Store Google Maps API key */
    private String googleKey;
    
    /** Store latitude, longitude and address */
    private Vector<String> lat, lon, place;
    
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

        sb.append("var lat = new Array();\n");
        for(int i=0; i<lat.size(); i++) 
        	sb.append("lat[").append(i).append("]=").append(lat.elementAt(i)).append(";\n");

        sb.append("var lng = new Array();\n");
        for(int i=0; i<lon.size(); i++)
        	sb.append("lng[").append(i).append("]=").append(lon.elementAt(i)).append(";\n");

        sb.append("var loc = new Array();\n");
        for(int i=0; i<place.size(); i++)
        	sb.append("loc[").append(i).append("]=\"").append(place.elementAt(i)).append("\";\n");

        sb.append("var latAverage=").append(latAverage).append(";\n");
        sb.append("var lonAverage=").append(lonAverage).append(";\n");

        sb.append("function initialize() {\n");
        sb.append("if (GBrowserIsCompatible()) {\n");

        sb.append("var map = new GMap2(document.getElementById(\"map_canvas\"));\n");

        sb.append("map.setCenter(new GLatLng(latAverage, lonAverage), 10);\n");
        sb.append("map.addControl(new GSmallMapControl());\n");
        sb.append("map.addControl(new GMapTypeControl());\n");

        sb.append("var baseIcon = new GIcon(G_DEFAULT_ICON);\n");
        sb.append("baseIcon.shadow = \"http://www.google.com/mapfiles/shadow50.png\";\n");
        sb.append("baseIcon.iconSize = new GSize(20, 34);\n");
        sb.append("baseIcon.shadowSize = new GSize(37, 34);\n");
        sb.append("baseIcon.iconAnchor = new GPoint(9, 34);\n");
        sb.append("baseIcon.infoWindowAnchor = new GPoint(9, 2);\n");

        sb.append("function createMarker(point, index) {\n");
        sb.append("var letter = String.fromCharCode(\"A\".charCodeAt(0) + index);\n");
        sb.append("var letteredIcon = new GIcon(baseIcon);\n");
        sb.append("letteredIcon.image = \"http://www.google.com/mapfiles/marker\" + letter + \".png\";\n");

        sb.append("markerOptions = { icon:letteredIcon };\n");
        sb.append("var marker = new GMarker(point, markerOptions);\n");

        sb.append("GEvent.addListener(marker, \"click\", function() {\n");
        sb.append("marker.openInfoWindowHtml(loc[index]);\n");
        sb.append("});\n");
        sb.append("return marker;\n");
        sb.append("}\n");
           
        sb.append("for (var i=0; i<loc.length; i++) {\n");
        sb.append("var latlng = new GLatLng(lat[i], lng[i]);\n");
        sb.append("map.addOverlay(createMarker(latlng, i));\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("</head>\n");

        sb.append("<body onload=\"initialize()\">\n");
        sb.append("<div align=\"center\">\n");
        sb.append("<div id=\"map_canvas\" style=\"width: 500px; height: 500px\"></div>\n");
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
     * @throws WebUIException A problem arised during the call back
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
    	
    	InputStream is = (InputStream)cc.getDataComponentFromInput(DATA_INPUT);
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	
    	DocumentBuilder db;
    	
    	
    	    
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("location");
			System.out.println("Information of all addresses");

			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
			    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			        Element fstElmnt = (Element)fstNode;
			           
			        NodeList streetList = fstElmnt.getElementsByTagName("address");
			        Element streetElement = (Element)streetList.item(0);
			        NodeList street = streetElement.getChildNodes();
			        System.out.println("address : "  + ((Node)street.item(0)).getNodeValue());
			        String theAddress = ((Node)street.item(0)).getNodeValue();
			        
			        NodeList cityList = fstElmnt.getElementsByTagName("city");
			        Element cityElement = (Element)cityList.item(0);
			        NodeList city = cityElement.getChildNodes();
			        System.out.println("City : " + ((Node)city.item(0)).getNodeValue());
			        String theCity = ((Node)city.item(0)).getNodeValue();   
			        
			        NodeList stateList = fstElmnt.getElementsByTagName("state");
			        Element stateElement = (Element)stateList.item(0);
			        NodeList state = stateElement.getChildNodes();
			        System.out.println("State : " + ((Node)state.item(0)).getNodeValue());
			        String theState = ((Node)state.item(0)).getNodeValue();
			        
			        StringBuffer sb = new StringBuffer();
			        sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
			        sb.append(yahooId);
			        sb.append("&street=").append(theAddress.replace(' ', '+'));
			        sb.append("&city=").append(theCity);
			        sb.append("&state=").append(theState);
			        
			        URL url = new URL(sb.toString());
			        BufferedReader br = new BufferedReader(new InputStreamReader(
			                url.openConnection().getInputStream()));
			        StringBuffer buffer = new StringBuffer();
			        String line; 
			        while((line = br.readLine())!= null) {
			        	line = line.trim();
			            if(line.length() == 0)
			            	continue;
			            buffer.append(line).append(STRING_DELIMITER);
			        }
			        //System.out.println(buffer.toString());
			        br.close();
			        
			        String tmp = buffer.toString();
			        float latitude, longitude;
			        if(tmp.indexOf("<Latitude>") != -1) {//valid place 
			        	int beginIndex = tmp.indexOf("<Latitude>") + 10,
			        	    endIndex = tmp.indexOf("</Latitude>");
			        	latitude = Float.parseFloat(
			        			tmp.substring(beginIndex, endIndex));
			        	latMin = Math.min(latMin, latitude);
			        	latMax = Math.max(latMax, latitude);
			        	latAverage += latitude;
			        	lat.add(tmp.substring(beginIndex, endIndex));
			        	
			        	beginIndex = tmp.indexOf("<Longitude>") + 11;
		        	    endIndex = tmp.indexOf("</Longitude>");
		        	    longitude = Float.parseFloat(
			        			tmp.substring(beginIndex, endIndex));
		        	    lonMin = Math.min(lonMin, longitude);
		        	    lonMax = Math.max(lonMax, longitude);
		        	    lonAverage += longitude;
		        	    lon.add(tmp.substring(beginIndex, endIndex));
		        	    place.add(theAddress+","+theCity+","+theState);
			        }
			    }
			}
			is.close();
		} catch (Exception e1) {
			throw new ComponentExecutionException(e1);
		}
    	
		if(place.size() != 0) {
			latAverage /= place.size();
			lonAverage /= place.size();
			System.out.println(latMin + " " + latMax + " " + 
					           lonMin + " " + lonMax + " " +
					           latAverage + " " + lonAverage);
		}
		
		for(int k=0; k<place.size(); k++)
			System.out.println(place.elementAt(k) + "\t" +
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
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    	lat = new Vector<String>();
    	lon = new Vector<String>();
    	place = new Vector<String>();
    	latAverage = 0;
    	lonAverage = 0;
    	latMin =  Float.MAX_VALUE;
	    latMax = -Float.MAX_VALUE;
	    lonMin =  Float.MAX_VALUE; 
	    lonMax = -Float.MAX_VALUE;
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    } 
}
