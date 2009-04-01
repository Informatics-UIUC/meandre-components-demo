/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.meandre.components.io.url;

import org.htmlparser.beans.StringBean;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Loretta Auvil",
		description="Fetches content of the specified URL as an text.",
		name="URLContent",
		tags="URL, content, text",
		baseURL="meandre://seasr.org/components/")

		public class URLContent extends AbstractExecutableComponent {
	@ComponentInput(description="URL to be fetched." +
			"<br> TYPE: java.lang.String",
			name= "URL")
			public final static String DATA_INPUT = "URL";

	@ComponentOutput(description="Output content of the specified URL as text.",
			name="Text")
			public final static String DATA_OUTPUT = "Text";


	/** When ready for execution.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException An exception occurred during execution
	 * @throws ComponentContextException Illegal access to context
	 */
	public void executeCallBack(ComponentContext cc)
	throws Exception {
		String inputUrl = (String)cc.getDataComponentFromInput(DATA_INPUT);

		StringBean sb = new StringBean ();
		sb.setURL (inputUrl);
		String sRes = sb.getStrings();
		cc.pushDataComponentToOutput(DATA_OUTPUT, (sRes==null)?"We have a problem.":sRes);
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
