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


package org.meandre.components.io.csv;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;

@Component(creator="Lily Dong",
           description="Iterates over an InputStream (of bytes), converting to " +
               "characters, parsing the character strings into lines and " +
               "then as tokens delineated by commas. The tokens from each " +
               "line are put in an Object[] and pushed as this component's " +
               "output. This component therefore takes in one input stream " +
               "and outputs multiple Object[]'s, one for each line in the " +
               "csv file being streamed in.",
           name="CSVLinePusher",
           tags="CSV",
           baseURL="meandre://seasr.org/components/")

public class CSVLinePusher extends AbstractExecutableComponent
{
    @ComponentInput(description="Read content as stream." +
            "<br>TYPE: java.io.InputStream",
                    name= "Stream")
    public final static String DATA_INPUT = "Stream";

    @ComponentOutput(description="Output content as stream of Object array " +
    		"followed by StreamTerminator." +
    		"<br>TYPE: <br>org.meandre.core.system.components.ext.StreamInitiator" +
            "<br>   THEN" +
            "<br>java.lang.Object[] (multiple times)" +
            "<br>   THEN" +
            "<br>org.meandre.core.system.components.ext.StreamTerminator",
                     name="Object")
    public final static String DATA_OUTPUT = "Object";

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
        InputStream is = (InputStream)cc.getDataComponentFromInput(DATA_INPUT);

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamInitiator());
        try {
            while((line = br.readLine())!= null) {
                line = line.trim();
                if(line.length() == 0)
                    continue;

                int fromIndex = 0;
                int index = line.indexOf(',', fromIndex);
                Vector<String> tokens = new Vector<String>();
                while(index != -1) {
                	tokens.add(line.substring(fromIndex, index));
                	fromIndex = index+1;
                	index = line.indexOf(',', fromIndex);
                }

                tokens.add(line.substring(fromIndex));

                cc.pushDataComponentToOutput(DATA_OUTPUT, tokens.toArray());
            }
            cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamTerminator());
            is.close();
            br.close();
        }catch(java.io.IOException e) {
            try {
                if(is != null)
                    is.close();
                if(br != null)
                    br.close();
            }catch(java.io.IOException ioes) {}
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
