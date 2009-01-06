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

import java.util.Map;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Inputs a Map<String, Integer> and deletes all entries " +
           		"whose key is one of the keys to delete. The keys to delete " +
           		"are specified in this component's properties.",
           name="WordCountFilter",
           tags="map, reducer"
)

public class WordCountFilter implements ExecutableComponent {
    @ComponentInput(description="A word count summary in Map format." +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
                    name= "inputMap")
    public final static String DATA_INPUT = "inputMap";

    @ComponentOutput(description="Filtered word count in Map format." +
            "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
                     name="outputMap")        
    public final static String DATA_OUTPUT = "outputMap";
    
    @ComponentProperty(defaultValue="",
                       description="Keys to be deleted from Map. The keys should be delimited by comma.",
                       name="keysToBeDeleted")
    final static String DATA_PROPERTY = "keysToBeDeleted";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        Map inputMap = (Map)cc.getDataComponentFromInput(DATA_INPUT);
        String keysToBeDeleted = cc.getProperty(DATA_PROPERTY);
        StringTokenizer st = new StringTokenizer(keysToBeDeleted, ",");
        while(st.hasMoreTokens()) {
            String theKey = st.nextToken();
            if(inputMap.containsKey(theKey))
                inputMap.remove(theKey);
        }
        
        System.out.println(inputMap.toString());
        
        cc.pushDataComponentToOutput(DATA_OUTPUT, inputMap);
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
