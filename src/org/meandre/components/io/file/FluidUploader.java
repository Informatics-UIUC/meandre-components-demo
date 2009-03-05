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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;

import java.util.Calendar;

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
import org.mortbay.log.Log;

@Component(creator="Lily Dong",
           description="Uploads a local file using Fiuld at http://build.fluidproject.org/. " +
           "For using Fluid, you must have it installed in public_resources directory. " +
           "The directory is part of your local meandre server. For example, ater installing, " +
           "one branch of the hierarchy of Fluid is public_resources/fluid-0.8/fluid-componnents/" +
           "html/templates/. The html file generated by this component will be written into " +
           "templates/. Make sure that swfupload.swf lies in public_resources/fluid-0.8/" +
           "fluid-componnents/flash/.",
           name="Fluid Uploader",
           tags="file, upload",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/")

public class FluidUploader
implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentOutput(description="Output the file uploaded from local machine."+
			"<br>TYPE: java.lang.String",
	         		 name="Text")
	public final static String DATA_OUTPUT = "Text";

	/** The blocking semaphore */
	private Semaphore sem = new Semaphore(1,true);

	/** The instance ID */
	private String sInstanceID = null;

	/** Store the uploaded file */
	private String str;

	/** Store the temporary file name */
	private String name;

	/** Store the WebUI URL */
	private String webUiUrl;

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

	/**
	 * Store the interim file.
	 *
	 * @param path The path specifies where the file should be stored temporarily.
	 */
	private void save(String path) throws WebUIException{
		StringBuffer buf = new StringBuffer();

    	buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    	buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");

    	buf.append("<head>\n");
    	buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
    	buf.append("<title>Uploader</title>\n");
    	buf.append("<link href=\"../../css/fluid.components.uploader.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
    	buf.append("<link href=\"../../css/fluid.layout.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
    	buf.append("<script type=\"text/javascript\" src=\"../../js/Fluid-all.js\"></script>\n");
    	buf.append("</head>\n");

    	buf.append("<body>\n");
    	buf.append("<div id=\"uploader-contents\">\n");

    	buf.append("<form method=\"post\" enctype=\"multipart/form-data\" class=\"fl-progressive-enhanceable\">\n");
    	buf.append("<p>Browse to upload a file.</p>\n");
    	buf.append("<input name=\"fileData\" type=\"file\" />\n");
    	buf.append("<div><input type=\"submit\" value=\"Save\"/></div>\n");
    	buf.append("</form>\n");

    	buf.append("<form class=\"fl-uploader fl-progressive-enhancer\" method=\"get\" enctype=\"multipart/form-data\">\n");

    	buf.append("<div class=\"fl-uploader-queue-wrapper\">\n");
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

    	buf.append("<div class=\"fl-scroller\">\n");
        buf.append("<div class=\"scroller-inner\">\n");
    	buf.append("<table cellspacing=\"0\" class=\"fl-uploader-queue\" summary=\"Queue of files to upload.\">\n");
    	buf.append("<tbody>\n");
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
    	buf.append("<div class=\"fl-scroller-table-foot\">\n");
    	buf.append("<table cellspacing=\"0\" cellpadding=\"0\" summary=\"Status of file queue.\">\n");
    	buf.append("<tr>\n");
    	buf.append("<td class=\"total-file-progress\">\n");
    	buf.append("Total: <span class=\"fl-uploader-totalFiles\">0</span> files(<span class=\"fl-uploader-totalBytes\">0 KB</span>)\n");
    	buf.append("</td>\n");
    	buf.append("<td class=\"footer-button\" align=\"right\" >\n");
    	buf.append("<a href=\"#\" class=\"fl-uploader-browse\">Browse files</a>\n");
    	buf.append("</td>\n");
    	buf.append("</tr>\n");
    	buf.append("</table>\n");
    	buf.append("<div class=\"total-progress\">&nbsp;</div>\n");
    	buf.append("</div>\n");
    	buf.append("</div>\n");
    	buf.append("<div class=\"fl-uploader-btns\">\n");
    	buf.append("<button type=\"button\" class=\"fl-uploader-pause hidden\">Stop Upload</button><button type=\"button\" class=\"fl-uploader-upload default dim\" disabled=\"disabled\">Upload</button>\n");
    	buf.append("</div>\n");

    	buf.append("</form>\n");

    	buf.append("<div class=\"fluid-templates\">\n");
    	buf.append("<table id=\"fluid-uploader\" summary=\"Invisible placeholder for Uploader templates.\">\n");
    	buf.append("<tr id=\"queue-error-tmplt\" class=\"queue-error-row\">\n");
    	buf.append("<td colspan=\"3\" class=\"queue-error\"></td>\n");
    	buf.append("</tr>\n");
    	buf.append("</table>\n");
    	buf.append("</div>\n");

    	buf.append("</div>\n");

    	buf.append("<script>\n");
    	buf.append("var myUploader = fluid.progressiveEnhanceableUploader(\".fl-uploader\", \".fl-progressive-enhanceable\", {\n");
    		buf.append("uploadManager: {\n");
    			buf.append("type: \"fluid.swfUploadManager\",\n");
    			buf.append("options: {\n");
    	        	buf.append("flashURL: \"../../flash/swfupload.swf\",\n");
    	        	buf.append("uploadURL: \"/"+sInstanceID+"\",\n");
    	        buf.append("},\n");
    	        buf.append("decorators: {\n");
    	        	buf.append("type: \"fluid.swfUploadSetupDecorator\",\n");
    	            buf.append("options: {\n");
    	            	//buf.append("flashButtonImageURL: \"http://build.fluidproject.org/fluid/fluid-components/images/uploader/browse.png\"\n");
    	            	buf.append("flashButtonImageURL: \"../../images/uploader/browse.png\"\n");
    	            buf.append("}\n");
    	        buf.append("}\n");
    	    buf.append("},\n");
    	buf.append("});\n");
    	buf.append("</script>\n");
    	buf.append("</body>\n");
    	buf.append("</html>\n");

    	try {
    	PrintWriter out
    	   = new PrintWriter(new BufferedWriter(new FileWriter(path)));
    	out.write(buf.toString());
    	out.flush();
    	out.close();
    	} catch(Exception ex) {
    		throw new WebUIException(ex);
    	}
	}

	/** A simple message.
     *
     * @return The html containing the page
     */
    private String getViz() {
    	StringBuffer sb = new StringBuffer();

    	sb.append("<html>\n");

    	sb.append("<title>\n");
    	sb.append("Uploader");
    	sb.append("</title>\n");

    	sb.append("<body>\n");

    	sb.append("<iframe src=\"").append(webUiUrl+"public/resources/fluid-0.8/fluid-components/html/templates/" + name).append("\" width=\"100%\" height=\"30%\" FRAMEBORDER=0>\n");
    	sb.append("</iframe>\n");

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
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws	WebUIException {
		String sDone = request.getParameter("done");
	    if ( sDone!=null ) {
	    	try{
                PrintWriter writer = response.getWriter();
                writer.println("<html><head><title>Fluid Uploader</title>");
                writer.println("<meta http-equiv='REFRESH' content='0;url=/'></HEAD>");
                writer.println("<body>Fluid Uploader Releasing Display</body></html>");
            }catch (IOException e) {
                e.printStackTrace();
            }

	    	sem.release();
	    	return;
	    }

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
				String line = br.readLine();
				String boundary = line.trim();

				//continue reading until the beginning of file
				while((line=br.readLine()) != null) {
					line = line.trim();
					if(line.length() == 0)
						continue;
					if(line.startsWith("Content-Type:"))
						break;
				}

				StringBuffer buf = new StringBuffer();

				while((line=br.readLine()) != null) {
					line = line.trim();
					if(line.length() == 0)
						continue;
					if(line.startsWith(boundary)) //end of file
						break;
					buf.append(line).append("\n");
				}
				br.close();
				str = buf.toString();
				//System.out.println(str);
			}catch(java.io.IOException e) {
				throw new WebUIException(e);
			}
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
	   name = "tmp" +
	   Calendar.getInstance().get(Calendar.DAY_OF_MONTH) +
	   ".html";

	   String path = new File(".").getAbsolutePath();
	   int index = path.length()-1;
	   while(true) { //find the last occurrence of alphabet, and ignore \.
		   char ch = path.charAt(index);
		   if(Character.toString(ch).equals(File.separator))
			   break;
		   --index;
	   }
	   int endIndex = index;

	   String dir = cc.getPublicResourcesDirectory();
	   index = 0;
	   while(true) { //find the first occurrence of alphabet, and ignore .\
		   char ch = dir.charAt(index);
		   if(Character.isLetter(ch))
			   break;
		   ++index;
	   }
	   int beginIndex = index;

	   path = path.substring(0, endIndex)+File.separator+
	   dir.substring(beginIndex)+File.separator+
	   "fluid-0.8"+File.separator+
	   "fluid-components"+File.separator+
	   "html"+File.separator+
	   "templates"+File.separator+
	   name;

	   webUiUrl = cc.getWebUIUrl(true).toString();
	   sInstanceID = cc.getExecutionInstanceID();

	   System.out.println("path = " + path);
	   System.out.println("dir = " + cc.getPublicResourcesDirectory());
	   System.out.println("url = " + cc.getWebUIUrl(true).toString());

	   try {
		   save(path);
	   }catch(Exception e) {
		   throw new ComponentExecutionException(e);
	   }

	   	try {
	   		try {
	   		sem.acquire();
	   		cc.startWebUIFragment(this);
	   		sem.acquire();
	   		}
	   		catch ( InterruptedException ie ) {
	   			cc.getOutputConsole().println("I'm panicking");
		   		cc.getLogger().warning("Timing issue");
		   	}
	   		cc.stopWebUIFragment(this);

	   		cc.pushDataComponentToOutput(DATA_OUTPUT, str);
	   	}

	   	catch (Exception e) {
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

