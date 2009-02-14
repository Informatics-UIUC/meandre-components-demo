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
import java.util.Hashtable;
import java.util.Vector;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamTerminator;

@Component(creator="Lily Dong",
           description="Convert a CSV file represented by " +
           "a vector with Object[] as its element to Map<Object, Float>. " +
           "The csv file is expected to have a first column of string " +
           "and a second column of float. If the same key in the first column " +
           "appears multiple times, the number values in the second column will " +
           "be aggregated to form the final float value in the output.",
           name="CSV2WordCount",
           tags="csv, map, converter",
           baseURL="meandre://seasr.org/components/")

public class CSV2WordCount implements ExecutableComponent {
    @ComponentInput(description="A tokenized csv table. Each Object[] " +
    		"contains a row of data from a csv file." +
            "<br>TYPE: java.util.Vector<java.lang.Object[]>",
                    name= "CSV_Content")
    public final static String DATA_INPUT = "CSV_Content";

    @ComponentOutput(description="Output content in Map format." +
            "java.util.Map<java.lang.String, java.lang.Float>",
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
        Vector<Object[]> inputCsv = (Vector<Object[]>)cc.getDataComponentFromInput(DATA_INPUT);

        Map outputMap = new Hashtable();
        for(int index=0; index<inputCsv.size(); index++) {
            Object[] data = inputCsv.elementAt(index);
            if(outputMap.containsKey(data[0])) {
                float value =
                    Float.valueOf(outputMap.get(data[0]).toString()) +
                    Float.valueOf(data[1].toString());
                outputMap.put(data[0], new Float(value));
            } else {
                Float value = null;
                try {
                    value = Float.valueOf(data[1].toString());
                }catch(NumberFormatException e) {}
                if(value != null) //only number is stored in map
                    outputMap.put(data[0], value);
            }
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
