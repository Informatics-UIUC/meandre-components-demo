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

package org.meandre.components.io;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;

/* This executable component pushes a string to the output
 *
 * @author Amit Kumar
 * @author Boris Capitanu
 *
 * AK: Created on Oct 18, 2007 3:28:07 PM
 * BC: Modified Jan 8, 2008
 * 		 - added annotations for the component
 *  	 - renamed the component from InputString to PushString
 */

@Component(
	    creator="Boris Capitanu",
	    description="Pushes the string specified in the property to the output",
	    name="Push String",
	    tags="io, input, string",
        baseURL="meandre://seasr.org/components/")
        
public class PushString extends AbstractExecutableComponent { /* implements ExecutableComponent { */

	@ComponentProperty(description = "Input string", name = "string", defaultValue = "hello world")
	public final static String DATA_PROPERTY_STRING = "string";

	@ComponentOutput(description = "Output string", name = "string")
	public final static String DATA_OUTPUT_STRING = "string";

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	public void executeCallBack(ComponentContext context)
		throws Exception {

		String strInput = context.getProperty(DATA_PROPERTY_STRING);
		context.pushDataComponentToOutput(DATA_OUTPUT_STRING, strInput);
	}
	public void disposeCallBack(ComponentContextProperties ccp)
			throws Exception {
	}


}
