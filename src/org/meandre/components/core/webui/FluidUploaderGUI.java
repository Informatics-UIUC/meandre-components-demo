package org.meandre.components.core.webui;


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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
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
        description="Presents a simple text area for user to input string.",
        name="FluidUploaderGUI",
        tags="string, visualization",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/",
        dependency={"velocity-1.6.1-dep.jar"},
        resources={"FluidUploaderGUI.vm"}
        )
public class FluidUploaderGUI extends TemplateGUI {

	//
	// reset the defaults inherited from TemplateGUI,  to this component
	//
	@ComponentProperty(description = "title",   name = "title",   defaultValue = "Uploader")
	static final String DATA_PROPERTY_TITLE = "title";
	
	@ComponentProperty(description = "The template name", 
                              name = "template", 
                      defaultValue = "org/meandre/components/core/webui/FluidUploaderGUI.vm")
    static final String DATA_PROPERTY_TEMPLATE = TemplateGUI.DATA_PROPERTY_TEMPLATE;
	
	
	public FluidUploaderGUI()
	{
		templateVariables = new String[]{DATA_PROPERTY_TITLE};
		//
		// velocity could always access these via $ccp.getProperty("title") 
		// but now they will be visible as $title
		//
	}
	
	ArrayList<String> output;
	protected void subInitialize(ComponentContextProperties ccp)
	{
		output = new ArrayList<String>();
	}
	
	//
    // not only check errors, process the input at this step
    //
	
    protected boolean processRequest(HttpServletRequest request)
       throws IOException 
    {
    	// if this request is the last request, 
    	// return
    	if (request.getParameter(formInputName) != null) {
    	   return true;
    	}
    	
    	
    	BufferedReader br = null;
    	br = request.getReader();
		
		if (br == null) {
		   return false;
		}
			
		console.println("process input");
		console.flush();
		
		String line = br.readLine();
		String boundary = line.trim();

		//continue reading until the beginning of file
		while ((line=br.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.startsWith("Content-Type:"))
				break;
		}

		StringBuffer buf = new StringBuffer();
		while ((line=br.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.startsWith(boundary)) //end of file
				break;
			buf.append(line).append("\n");
		}
		br.close();
		String outputString = buf.toString();
		output.add(outputString);
		
		
		console.println("got input " + outputString);
		console.flush();
		
		// all is good, no errors
		return true;
    }
     
    protected void subPushOutput(ComponentContext cc)
       throws ComponentExecutionException, ComponentContextException
    {
    	Iterator<String> it = output.iterator();
    	while (it.hasNext()) {
    		String out = it.next();
            cc.pushDataComponentToOutput(DATA_OUTPUT, out);
    	}
    	
    }
    
}


//  = cc.getWebUIUrl(true).toString();
/*
private String sInstanceID = null;
private String name = "abc.xyz";
private String path = "/tmp" + name;
private String webUiUrl;

protected void subExecute(ComponentContext cc) 
throws ComponentExecutionException, 
       ComponentContextException
       
{
	webUiUrl = cc.getWebUIUrl(true).toString();
	sInstanceID = cc.getExecutionInstanceID();
	
	try {
		save(path);
	}
	catch (Exception e) {}

	
 }


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

*/