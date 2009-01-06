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
import java.util.Vector;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Inputs a byte stream representation of a .csv file " +
           "and converts it into a Vector of Object arrays, where each Object " +
           "is a String representation of a cell from the .csv table. Each " +
           "Object array corresponds to a line/row in the csv table.",
           name="CSVReader",
           tags="CSV")

public class CSVReader implements ExecutableComponent {
    @ComponentInput(description="Read content as stream." +
            "<br>TYPE: java.io.InputStream",
                    name= "inputStream")
    public final static String DATA_INPUT = "inputStream";
    
    @ComponentOutput(description="Output content as vector containing " +
    		"Object array." +
            "<br>TYPE: java.lang.Vector<java.lang.Object[]>",
                     name="outputObject")        
    public final static String DATA_OUTPUT = "outputObject";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        InputStream is = (InputStream)cc.getDataComponentFromInput(DATA_INPUT);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line; 
        Vector<Object[]> result = new Vector<Object[]> ();
        try {
            while((line = br.readLine())!= null) {
                line = line.trim();
                if(line.length() == 0)
                    continue;
                
                int fromIndex = 0;
                int index = line.indexOf(',', fromIndex);
                Vector<String> tokens = new Vector<String>();
                while(index != -1) {
                	//System.out.println(line.substring(fromIndex, index));
                	tokens.add(line.substring(fromIndex, index));
                	fromIndex = index+1;
                	index = line.indexOf(',', fromIndex);
                }
                //System.out.println(line.substring(fromIndex));
                tokens.add(line.substring(fromIndex));
                result.add(tokens.toArray());
                
                /*StringTokenizer st = new StringTokenizer(line, ",");
                Object[] tokens = new Object[st.countTokens()];
                int pos = 0;
                while(st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    tokens[pos++] = token;
                }
                result.add(tokens);*/
            }
            /*for(int i=0; i<result.size(); i++) {
                Object[] tokens = result.elementAt(i);
                for(int j=0; j<tokens.length; j++) {
                    System.out.print(tokens[j].toString());
                    if(j != tokens.length-1)
                        System.out.print(" ");
                }
                System.out.println();
            }*/
            cc.pushDataComponentToOutput(DATA_OUTPUT, result);
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
    public void initialize(ComponentContextProperties ccp) {
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
