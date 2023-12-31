
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

package org.meandre.components.core.webui;

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
        name="InputStringGUI",
        tags="string, visualization",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/",
        dependency={"velocity-1.6.1-dep.jar"},
        resources={"InputString.vm"}
        )
public class InputStringGUI extends TemplateGUI {

	//
	// specific to this component
	//
	@ComponentProperty(description = "title",   name = "title",   defaultValue = "Input a string")
	static final String DATA_PROPERTY_TITLE = "title";
	
	@ComponentProperty(description = "message", name = "message", defaultValue = "Please input a string")
	static final String DATA_PROPERTY_MESSAGE = "message";
	
	@ComponentProperty(description = "default value", name = "defaultValue", defaultValue = "")
	static final String DATA_PROPERTY_DEFAULT = "defaultValue";
	
	@ComponentProperty(description = "The template name", 
                              name = "template", 
                      defaultValue = "org/meandre/components/core/webui/InputString.vm")
    static final String DATA_PROPERTY_TEMPLATE = TemplateGUI.DATA_PROPERTY_TEMPLATE;
	
	
	public InputStringGUI()
	{
		templateVariables = new String[]{DATA_PROPERTY_TITLE, DATA_PROPERTY_MESSAGE, DATA_PROPERTY_DEFAULT};
		//
		// velocity could always access these via $ccp.getProperty("title") 
		// but now they will be visible as $title, $message
		//
	}
	

	
}
