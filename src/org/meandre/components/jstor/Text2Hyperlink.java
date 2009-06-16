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

package org.meandre.components.jstor;

import java.net.URLEncoder;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
           description="Converts text to hyperlink.",
           name="Text2Hyperlink",
           tags="text hyperlink converter",
           mode = Mode.compute,
           firingPolicy = FiringPolicy.all,
           rights = Licenses.UofINCSA,
           baseURL="meandre://seasr.org/components/jstor/",
           dependency = {"protobuf-java-2.0.3.jar"})

public class Text2Hyperlink extends AbstractExecutableComponent
{
	/*@ComponentProperty(defaultValue="",
			description="This property specifies the search value.",
			name="query")
    final static String DATA_PROPERTY = "query";*/


	@ComponentInput(description="Read text." +
	            "<br>TYPE: java.lang.String",
	                name= "string")
	public final static String DATA_INPUT = "string";

	@ComponentOutput(description="Output hyperlink." +
				"<br>TYPE: java.lang.String",
	                 name="string")
	public final static String DATA_OUTPUT = "string";

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	String str = (String)cc.getDataComponentFromInput(DATA_INPUT);
    	try {
        	cc.pushDataComponentToOutput(
        			DATA_OUTPUT, URLEncoder.encode(str, "UTF-8"));
    	}catch(Exception e) {
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

