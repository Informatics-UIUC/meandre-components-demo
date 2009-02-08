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

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
        description="Present a simple text area for user to input string.",
        name="Input String",
        tags="string, visualization",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/")

public class InputString
implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentOutput(description="Output the string passed from the text area." +
			"<br>TYPE: java.lang.String",
	                 name="Text")
	public final static String DATA_OUTPUT = "Text";

	/*
     * Store the message imported by user.
     */
    private String outputText = null;


    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;

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
       sb.append("<title>Input String</title>\n");
       sb.append("<script language=\"JavaScript\">\n");
       sb.append("function validate(form, value) {\n");
       sb.append("var trimmed = value.replace(/^\\s+|\\s+$/g, '') ;\n");
       sb.append("if (trimmed.length != 0)\n");
       sb.append("form.submit();\n");
       sb.append("else\n");
       sb.append("alert(\"Please input valid string and submit again.\");\n");
       sb.append("}\n");
	   sb.append("</script>\n");
       sb.append("</head>\n");

       sb.append("<body>\n");
       sb.append("<br/>\n");
       sb.append("<div>\n");
       sb.append("<form name=\"input\" method=\"get\" action=\"/" + sInstanceID + "\">\n");
       sb.append("Your input:<BR>\n");
       sb.append("<TEXTAREA NAME=\"context\" COLS=40 ROWS=6></TEXTAREA>\n");
       //sb.append("<input type=\"text\" name=\"context\" size=\"40\"/>\n");
       sb.append("<br/>\n");
       sb.append("<input type=\"submit\" value=\"Submit\" onclick=\"validate(this.form, this.form.context.value); return false;\">\n");
       sb.append("</form>\n");
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
        outputText = request.getParameter("context");
        if(outputText != null)
            sem.release();
        else
            emptyRequest(response);
    }

   /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc)
    throws ComponentExecutionException,ComponentContextException {
       	try {
       		sInstanceID = cc.getExecutionInstanceID();
       		sem.acquire();
       		cc.startWebUIFragment(this);
       		sem.acquire();
       		cc.stopWebUIFragment(this);

       		cc.pushDataComponentToOutput(DATA_OUTPUT, outputText);

       		System.out.println(outputText);
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
