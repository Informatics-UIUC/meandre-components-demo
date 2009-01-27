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

import java.io.BufferedReader;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

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
        description="Upload a local file using Fluid, ." +
        "which is a open source design pattern library at " +
        "http://osdpl.fluidproject.org/.",
        name="Fluid Uploader",
        tags="file, upload",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/")

public class FluidUploader
implements ExecutableComponent, WebUIFragmentCallback {
	/** The blocking semaphore */
	private Semaphore sem = new Semaphore(1,true);

	/** The instance ID */
	private String sInstanceID = null;

	/** Store URLs */
    private String htmLocation;

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

    	buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    	buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    	    buf.append("<head>\n");
    	        buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
    	        buf.append("<title>Uploader</title>\n");

    	        buf.append("<link href=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/css/fluid.reset.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
    	        buf.append("<link href=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/css/fluid.components.uploader.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
    	        buf.append("<link href=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/css/fluid.layout.css\" rel=\"stylesheet\" type=\"text/css\" />\n");

    	        buf.append("<!-- Fluid and jQuery Dependencies -->\n");

    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/jquery/jquery-1.2.6.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/jquery/jARIA.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/jquery/jquery.keyboard-a11y.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/swfupload/swfobject.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/swfupload/swfupload.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/Fluid.js\" type=\"text/javascript\"></script>\n");

    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/ProgressiveEnhancement.js\" type=\"text/javascript\"></script>\n");

    	        buf.append("<!-- Uploader dependencies; these will be merged into a new Uploader.js at the end -->\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/uploader/FileQueue.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/uploader/SWFUploadManager.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/uploader/Scroller.js\" type=\"text/javascript\"></script>\n");
    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/uploader/Progress.js\" type=\"text/javascript\"></script>\n");

    	        buf.append("<script src=\"http://norma.ncsa.uiuc.edu/public-dav/javascript/webapp/fluid-components/js/fluid/uploader/Uploader.js\" type=\"text/javascript\"></script>\n");
    	    buf.append("</head>\n");

    	    buf.append("<body>\n");
    	        buf.append("<div id=\"uploader-contents\">\n");

    	            buf.append("<!-- This form will be progressively enhanced by the Fluid Uploader component. -->\n");
    	            buf.append("<form method=\"post\" enctype=\"multipart/form-data\" class=\"fl-progressive-enhanceable\" action=\"/" + sInstanceID + "\">\n");
    	                buf.append("<p>Browse to upload a file.</p>\n");
    	                buf.append("<input name=\"fileData\" type=\"file\" />\n");

    	                buf.append("<div><input type=\"submit\" value=\"Save\"/></div>\n");
    	            buf.append("</form>\n");

    	            buf.append("<!-- This is the markup for the Fluid Uploader component itself. -->\n");
    	            buf.append("<form class=\"fl-uploader fl-progressive-enhancer\"\n");
    	                  buf.append("method=\"get\"\n");
    	                  buf.append("enctype=\"multipart/form-data\">\n");

    	                buf.append("<!-- The file queue -->\n");
    	                buf.append("<div class=\"fl-uploader-queue-wrapper\">\n");

    	                    buf.append("<!-- Top of the queue -->\n");
    	                    buf.append("<div class=\"fl-scroller-table-head\">\n");
    	                        buf.append("<table cellspacing=\"0\" cellpadding=\"0\" summary=\"Headers for the file queue.\">\n");

    	                            buf.append("<caption>File Upload Queue:</caption>\n");

    	                                buf.append("<tr>\n");
    	                                    buf.append("<th scope=\"col\" class=\"fileName\">File Name</th>\n");
    	                                    buf.append("<th scope=\"col\" class=\"fileSize\">Size&nbsp;&nbsp;</th>\n");
    	                                    buf.append("<th scope=\"col\" class=\"fileRemove\">&nbsp;</th>\n");
    	                                buf.append("</tr>\n");

    	                        buf.append("</table>\n");
    	                    buf.append("</div>\n");

    	                    buf.append("<!-- Scrollable view -->\n");
    	                    buf.append("<div class=\"fl-scroller\">\n");
    	                        buf.append("<div class=\"scroller-inner\">\n");
    	                            buf.append("<table cellspacing=\"0\" class=\"fl-uploader-queue\" summary=\"Queue of files to upload.\">\n");
    	                                buf.append("<tbody>\n");
    	                                    buf.append("<!-- Rows will be rendered in here. -->\n");

    	                                    buf.append("<!-- Template markup for the file queue rows -->\n");

    	                                    buf.append("<tr id=\"queue-row-tmplt\" class=\"fluid-templates\">\n");
    	                                        buf.append("<th class=\"fileName\" scope=\"row\">File Name Placeholder</th>\n");
    	                                        buf.append("<td class=\"fileSize\">0 KB</td>\n");
    	                                        buf.append("<td class=\"actions\">\n");
    	                                            buf.append("<button type=\"button\" class=\"iconBtn\" title=\"Remove File\" tabindex=\"-1\">\n");
    	                                                buf.append("<span class=\"text-description\">Remove file from queue</span>\n");
    	                                            buf.append("</button>\n");

    	                                        buf.append("</td>\n");
    	                                    buf.append("</tr>\n");
    	                                buf.append("</tbody>\n");
    	                            buf.append("</table>\n");
    	                            buf.append("<div class=\"file-progress\" id=\"row-progressor-tmplt\"><span class=\"file-progress-text\">76%</span></div>\n");
    	                        buf.append("</div>\n");
    	                    buf.append("</div>\n");

    	                    buf.append("<div class=\"fl-uploader-browse-instructions\">\n");

    	                        buf.append("Choose <em>Browse files</em> to add files to the queue\n");
    	                    buf.append("</div>\n");

    	                    buf.append("<!-- Foot of the queue -->\n");
    	                    buf.append("<div class=\"fl-scroller-table-foot\">\n");
    	                        buf.append("<table cellspacing=\"0\" cellpadding=\"0\" summary=\"Status of file queue.\">\n");

    	                                buf.append("<tr>\n");
    	                                    buf.append("<td class=\"total-file-progress\">\n");

    	                                        buf.append("Total: <span class=\"fl-uploader-totalFiles\">0</span> files\n");
    	                                        buf.append("(<span class=\"fl-uploader-totalBytes\">0 KB</span>)\n");
    	                                    buf.append("</td>\n");
    	                                    buf.append("<td class=\"footer-button\" align=\"right\" >\n");
    	                                        buf.append("<a href=\"#\" class=\"fl-uploader-browse\">Browse files</a>\n");
    	                                    buf.append("</td>\n");
    	                                buf.append("</tr>\n");

    	                        buf.append("</table>\n");
    	                        buf.append("<div class=\"total-progress\">&nbsp;</div>\n");
    	                    buf.append("</div>\n");
    	                buf.append("</div>\n");

    	                buf.append("<!-- Action buttons -->\n");
    	                buf.append("<div class=\"fl-uploader-btns\">\n");
    	                    buf.append("<button type=\"button\" class=\"fl-uploader-pause hidden\">Stop Upload</button><button type=\"button\" class=\"fl-uploader-upload default dim\" disabled=\"disabled\">Upload</button>\n");

    	                buf.append("</div>\n");

    	            buf.append("</form>\n");

    	            buf.append("<!-- Other templates -->\n");
    	            buf.append("<div class=\"fluid-templates\">\n");
    	                buf.append("<table id=\"fluid-uploader\" summary=\"Invisible placeholder for Uploader templates.\">\n");
    	                    buf.append("<tr id=\"queue-error-tmplt\" class=\"queue-error-row\">\n");
    	                        buf.append("<td colspan=\"3\" class=\"queue-error\"></td>\n");
    	                    buf.append("</tr>\n");
    	                buf.append("</table>\n");

    	            buf.append("</div>\n");
    	        buf.append("</div>\n");

    	        buf.append("<script type=\"text/javascript\">\n");
    	            buf.append("var myUploader = fluid.progressiveEnhanceableUploader(\".fl-uploader\", \".fl-progressive-enhanceable\", {\n");
    	                buf.append("uploadManager: \"fluid.swfUploadManager:{uploadURL: '/" + sInstanceID + "'}\"\n");
    	            buf.append("});\n");
    	        buf.append("</script>\n");
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
