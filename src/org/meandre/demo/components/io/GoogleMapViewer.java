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

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
           description="View Google map.",
           name="GoogleMapViewer",
           tags="google map, visualization",
           mode=Mode.webui)

public class GoogleMapViewer 
	implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentProperty(defaultValue="ABQIAAAAzuMq2M5--KdBKawoLNQWUxRi_j0U6kJrkFvY4-OX2XYmEAa76BQS61jzrv4ruAIpkFQs5Qp-fiN3hg",
                       description="This property sets Google Maps API key.",
                       name="type")
    final static String DATA_PROPERTY = "type";
	
	/** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;
    
    /** Store Google Maps API key */
    private String apiKey;
    
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
        sb.append("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key=" +
        		  apiKey + "\"\n");
        sb.append("type=\"text/javascript\"></script>\n");
        sb.append("<script type=\"text/javascript\">\n");

        sb.append("var map = null;\n");
        sb.append("var geocoder = null;\n");
        
        sb.append("var address= new Array();\n");
        sb.append("address[0] = \"1901 N Moreland Blvd, Champaign, Illinois\";\n");
        sb.append("address[1] = \"2001 N Moreland Blvd, Champaign, Illinois\";\n");
         
        sb.append("function initialize() {\n");
        sb.append("if (GBrowserIsCompatible()) {\n");
        sb.append("map = new GMap2(document.getElementById(\"map_canvas\"));\n");
        sb.append("map.setCenter(new GLatLng(37.4419, -122.1419), 13);\n");
        
        sb.append("geocoder = new GClientGeocoder();\n");
        
        sb.append("for(var i=0; i<address.length; i++)\n");
      	sb.append("showAddress(address[i]);\n");
        
        sb.append("}\n");
        sb.append("}\n");
        
        sb.append("function showAddress(address) {\n");
        sb.append("if (geocoder) {\n");
        sb.append("geocoder.getLatLng(\n");
        sb.append("address,\n");
        sb.append("function(point) {\n");
        sb.append("if (!point) {\n");
        sb.append("alert(address + \" not found\");\n");
        sb.append("} else {\n");
        sb.append("map.setCenter(point, 13);\n");
        sb.append("var marker = new GMarker(point);\n");
        sb.append("map.addOverlay(marker);\n");
                    
      	sb.append("GEvent.addListener(marker,\"click\", function() {\n");
        sb.append("var myHtml = address;\n");
        sb.append("map.openInfoWindowHtml(point, myHtml);\n");
        sb.append("});\n");
      	sb.append("alert(address + \" found\");\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append(");\n");
        sb.append("}\n");
        sb.append("}\n");
        
        sb.append("</script>\n");
        sb.append("</head>\n");

        sb.append("<body onload=\"initialize()\" onunload=\"GUnload()\">\n");
        sb.append("<div align=\"center\">\n");
        sb.append("<div id=\"map_canvas\" style=\"width: 500px; height: 300px\"></div>\n");
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
    	apiKey = cc.getProperty(DATA_PROPERTY);
    	
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
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    } 
}
