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

/* NOTES
 * 
 * This component takes NO inputs,
 * User selects one or more files to upload
 * Each file is uploaded to this component
 * on Execute, this component then pushes each of those uploaded files
 * to the next component
 * 
 * It is assumed that the server running this component
 * has installed the fluid flash stuff in published_resources
 * see FluidUploaderGUI.vm for more details on the html
 * 
 * The code here is based on FluidUploader.java in components.io.file
 * 
 */

@Component(creator="Lily Dong",
        description="Flash based file uploading",
        name="FluidUploaderGUI",
        tags="file input",
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
		
		// now save this input in an array
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