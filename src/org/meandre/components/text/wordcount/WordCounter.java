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

import java.util.StringTokenizer;
import java.util.Map;
import java.util.Hashtable;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Sum up the number of occurences of words in a text string and " +
           		"output the result in a Map<String, Integer> object.",
           name="WordCounter",
           tags="word, counter",
           baseURL="meandre://seasr.org/components/")

public class WordCounter implements ExecutableComponent {
    @ComponentInput(description="Text to be analyzed." +
            "<br>TYPE: java.lang.String",
                    name= "Text")
    public final static String DATA_INPUT = "Text";

    @ComponentOutput(description="Output content in Map format." +
            "TYPE: java.util.Map<java.lang.String, java.lang.Integer>",
                     name="Map")
    public final static String DATA_OUTPUT = "Map";

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        String inpuText = (String)cc.getDataComponentFromInput(DATA_INPUT);
        StringTokenizer st = new StringTokenizer(inpuText, " ,\t\n");
        Map<String, Integer> outputMap = new Hashtable<String, Integer>();
        while(st.hasMoreTokens()) {
            String key = st.nextToken();

            if(!key.matches("[a-zA-Z]+"))
                continue;

            if(outputMap.containsKey(key)) {
                int value = ((Integer)outputMap.get(key)).intValue();
                outputMap.put(key, new Integer(++value));
            } else
                outputMap.put(key, new Integer(1));
        }

        cc.pushDataComponentToOutput(DATA_OUTPUT, outputMap);
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
