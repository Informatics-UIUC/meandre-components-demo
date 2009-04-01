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

package org.meandre.components.text.wordcount;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong and Loretta Auvil",
		description="Aggregates a series of input Map<String, Float> " +
		"objects into a single Map. If the same String key appears " +
		"in multiple input Maps, the count values are summed together. " +
		"The aggregated map is pushed out when 'true' is received for the last item input.",
		name="WordCountAggregatorByFlag",
		tags="map, aggregator",
		baseURL="meandre://seasr.org/components/")

public class WordCountAggregatorByFlag extends AbstractExecutableComponent
{
	@ComponentInput(description="Read content in Map format." +
			"<br>TYPE: " +
			"<br>java.util.Map<java.lang.String, java.lang.Float> (multiple times)",
			name= "Map")
			public final static String DATA_INPUT = "Map";

	@ComponentInput(
			description = "Boolean value for whether the current item passed is the last item.",
			name = "last_item"
	)
	public final static String INPUT_LAST_ITEM = "last_item";

	@ComponentOutput(description="Output content in Map format." +
			"<br>TYPE: java.util.Map<java.lang.String, java.lang.Float>",
			name="Map")
			public final static String DATA_OUTPUT = "Map";

	//Store output value.
	private Map outputMap;

	/** When ready for execution.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException An exception occurred during execution
	 * @throws ComponentContextException Illegal access to context
	 */
	public void executeCallBack(ComponentContext cc)
	throws Exception {

		Object inputObject = cc.getDataComponentFromInput(DATA_INPUT);

		// check to see if outputMap is null and create; otherwise it will be appended while
		// gathering data for all the submitted urls
		if (outputMap == null) {
			outputMap = new Hashtable();
		}

		if(inputObject instanceof Map) {
			Map inputMap = (Map)inputObject;
			Set ks = inputMap.keySet();
			Iterator it = ks.iterator();
			while(it.hasNext()) {
				Object obj = it.next();
				if(outputMap.containsKey(obj)) {
					int value =
						Integer.valueOf(outputMap.get(obj).toString()) +
						Integer.valueOf(inputMap.get(obj).toString());
					outputMap.put(obj, new Integer(value));
				} else {
					outputMap.put(obj, inputMap.get(obj));
				}
			}
		}
		// null outputMap so that it is ready to execute the again for another run

		if ((String)cc.getDataComponentFromInput(INPUT_LAST_ITEM)=="true") {
			cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);
			outputMap = null;
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
