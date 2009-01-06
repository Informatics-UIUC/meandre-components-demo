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

package org.meandre.components.viz;

import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Component(creator="Lily Dong",
           description="Generates and displays a webpage with an element of " +
           		"content that is input as a byte[]. The mime type that will " +
           		"be assigned to the byte[] in the webpage is set in " +
           		"properties. \"text/plain\" is the default, \"image/<EXT>\" is " +
           		"also supported for standard image types.",
           name="MIMEContentViz",
           tags="multipurpose internet mail extensions, visualization",
           mode=Mode.webui)

public class MIMEContentViz 
    implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentProperty(defaultValue="text/plain",
                       description="This property sets MIME type.",
                       name="MIME_type")
    final static String DATA_PROPERTY = "MIME_type";
    
    @ComponentInput(description="Read content as byte array." +
            "<br>TYPE: byte[]",
                    name= "Content")
    public final static String DATA_INPUT = "Content";
    
    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;
    
    /** Store the input bytes */
    private byte[] inputContent;
    
    /** Store MIME type */
    private String mimeType;
    
    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arised during execution and something went wrong
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

        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        sb.append("<head>\n");
        sb.append("<title>Content Visualization</title>\n");
        sb.append("<style type=\"text/css\" media=\"screen\">\n");
        sb.append("body {\n");
        sb.append("font-family: Verdana, sans-serif;\n");
        sb.append("font-size: 1em;\n");
        sb.append("}\n");
        sb.append("</style>\n");
        sb.append("</head>\n");

        sb.append("<body>\n");
        sb.append("<br /><br />\n");
        sb.append("<p>");
        if(mimeType.startsWith("text")) {
            StringTokenizer st = 
                new StringTokenizer(new String(inputContent), "\n");
            while(st.hasMoreTokens()) 
                sb.append(st.nextToken()).append("<br/>");
        }
        else if(mimeType.startsWith("image")) {//type could be jpeg, gif or png
            String s = new sun.misc.BASE64Encoder().encode(inputContent); //convert byte[] to base64 string
            
            sb.append("<div align=\"center\">\n");
            
            sb.append("<img src=\"data:").append(mimeType).append(";base64,");
            sb.append(s).append("\"");
            sb.append(" border=\"0\" />");
            
            sb.append("</div>\n");
        }
          
        sb.append("</p>");
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
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        mimeType = cc.getProperty(DATA_PROPERTY);
        inputContent = (byte[])cc.getDataComponentFromInput(DATA_INPUT);
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
