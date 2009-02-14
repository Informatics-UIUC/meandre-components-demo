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

package org.meandre.components.text.transform;

import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Convert Map with set of key-value mappings to Vector " +
           "taking Object array as its element. If table viewer is used for visualization, " +
           "Attribute_Label_Header should be set to true, whereas " +
           "Attribute_Type_Header shoud be set to false." +
           "Heading in a table is predefined as a pair of \"Word\" and \"Count\".",
           name="Map2Table",
           tags="map, table, converter",
           baseURL="meandre://seasr.org/components/")

public class Map2Table implements ExecutableComponent {
	@ComponentInput(description="Map to converted." +
            "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
                    name= "Map")
    public final static String DATA_INPUT = "Map";

	@ComponentOutput(description="Output content as vector containing Object array." +
	        "<br>TYPE: java.lang.Vector<java.lang.Object[]>",
	                 name="Table")
	public final static String DATA_OUTPUT = "Table";

   /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc)
    throws ComponentExecutionException, ComponentContextException {
    	Map<String, Integer> map = (Map)cc.getDataComponentFromInput(DATA_INPUT);

    	Vector<Object[]> result = new Vector<Object[]>();
    	Object[] objects = new Object[2];
    	objects[0] = "Word";
    	objects[1] = "Count";
    	result.add(objects);
    	Set<String> set = map.keySet();
    	Iterator<String> iterator = set.iterator();
    	while(iterator.hasNext()) {
    		objects = new Object[2];
    		objects[0] = iterator.next();
    		objects[1] = map.get(objects[0]);
    		result.add(objects);
    	}
    	cc.pushDataComponentToOutput(DATA_OUTPUT, result);
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
