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

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.Mode;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component(creator="Loretta Auvil",
        description="Generates a webpage from the HTML text that it receives as input.",
        name="HTML_Viewer",
        tags="html, viewer",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/")

public class HTMLViewer extends AbstractExecutableComponent
implements WebUIFragmentCallback {

	@ComponentInput(description="Read content as byte array." +
            "<br>TYPE: byte[]",
                    name= "Content")
    public final static String DATA_INPUT = "Content";

    //private PrintStream console;
	private ComponentContext ccHandle;

	/** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
        	String sb = (String)ccHandle.getDataComponentFromInput(DATA_INPUT);

        	//Adding html code for the "Done" button to end the flow execution
        	StringBuffer doneButton = new StringBuffer();
        	doneButton.append("<div align=\"center\">\n");
        	doneButton.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
            		ccHandle.getExecutionInstanceID() + "/?done=true\">DONE</a></font></table>\n");
        	doneButton.append("</div>\n");
        	doneButton.append("</body>\n");

        	//sb.replace("</body>\n", doneButton);

            response.getWriter().println(sb.replace("</body>\n", doneButton));
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
    public void executeCallBack(ComponentContext cc)
    throws Exception {

			ccHandle = cc;

			try {
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