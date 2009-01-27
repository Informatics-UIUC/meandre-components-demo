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
package org.meandre.components.io.file;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
           description="Upload a local file.",
           name="Local File Uploader",
           tags="file, upload",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/")

public class LocalFileUploader
implements ExecutableComponent, WebUIFragmentCallback {
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
    	StringBuffer buf = new StringBuffer();

    	buf.append("<html>\n");
    	buf.append("<head>\n");

    	buf.append("<script language=\"JavaScript\">\n");
    		buf.append("function LimitAttach(form, file) {\n");
    		//buf.append("if (!file && file.length!=0)\n");
    		buf.append("form.submit();\n");
    		//buf.append("else\n");
    		//buf.append("alert(\"Please input valid file name and submit again.\");\n");
    		buf.append("}\n");
    	buf.append("</script>\n");
    	buf.append("</head>\n");

    	buf.append("<body>\n");
    	//buf.append("<center>\n");
    		buf.append("<p>\n");
    		buf.append("Please specify a file:\n");
    		buf.append("<form method=post name=upform enctype=\"multipart/form-data\" action=\"/" + sInstanceID + "\">\n");
    		buf.append("<input type=file name=uploadfile size=\"40\">\n");
    		buf.append("<p>\n");
    		buf.append("<input type=button name=\"Submit\" value=\"Submit\" onclick=\"LimitAttach(this.form, this.form.uploadfile.value)\">\n");
    		buf.append("</form>\n");
    	//buf.append("</center>\n");
    	buf.append("</body>\n");
    	buf.append("</html>\n");

    	return buf.toString();
    }

	/** This method gets called when a call with parameters is done to a given component
	 * webUI fragment
	 *
	 * @param target The target path
	 * @param request The request object
	 * @param response The response object
	 * @throws WebUIException A problem arised during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws	WebUIException {
		BufferedReader br = null;
		try {
			br = request.getReader();
		}catch(java.io.IOException e) {
			throw new WebUIException(e);
		}
		if(br == null)
			emptyRequest(response);
		else {
			try {
				String line = null;
				while((line = br.readLine()) != null)
					System.out.println(line);
			}catch(java.io.IOException e) {
				throw new WebUIException(e);
			}
			sem.release();
		}
	}

   /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
   public void execute(ComponentContext cc) throws
   ComponentExecutionException,ComponentContextException {
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
