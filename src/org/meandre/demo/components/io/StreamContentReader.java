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

package org.meandre.demo.components.io;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

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
        description="Read content as stream and output it as string or byte array.",
        name="StreamContentReader",
        tags="stream, string")

public class StreamContentReader implements ExecutableComponent {
    @ComponentProperty(defaultValue="string",
                       description="This property sets the type of content. " +
                       "The type could be string or binary.",
                       name="contentType")
    final static String DATA_PROPERTY = "contentType";
    
    @ComponentInput(description="Read content as stream.",
                    name= "inputStream")
    public final static String DATA_INPUT = "inputStream";
    
    @ComponentOutput(description="Output content as string or byte array.",
                     name="outputObject")        
    public final static String DATA_OUTPUT = "outputObject";
    
    private final static String STRING_DELIMITER = "\n";
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
        String type = cc.getProperty(DATA_PROPERTY);
        InputStream is = (InputStream)cc.getDataComponentFromInput(DATA_INPUT);
        
        BufferedReader br = null;
        if(type.equals("string")) {
            br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String line; 
            try {
                while((line = br.readLine())!= null)
                    sb.append(line).append(STRING_DELIMITER);
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
            cc.pushDataComponentToOutput(DATA_OUTPUT, sb.toString());
            
            //System.out.println(sb.toString());
        } else {
            DataInputStream dis = new DataInputStream(is);
            byte[] b = new byte[4096];
            int  nrBytes = 0,
                 totalBytes = 0;
            try {
                while((nrBytes = dis.read(b, totalBytes, 1024)) != -1) {
                    totalBytes += nrBytes;
                    if(totalBytes+1024 >= b.length) {
                        byte[] tmp = new byte[b.length + 4096];
                        System.arraycopy(b, 0, tmp, 0, b.length);
                        b = tmp;
                    }
                }
                byte[] tmp = new byte[totalBytes];
                System.arraycopy(b, 0, tmp, 0, totalBytes);
                cc.pushDataComponentToOutput(DATA_OUTPUT, tmp);
                is.close();
                dis.close();
                
                //System.out.println(new String(tmp));
            }catch(java.io.IOException e) {
                try {
                    if(is != null)
                        is.close();
                    if(dis != null)
                        dis.close();
                }catch(java.io.IOException ioes) {}
                throw new ComponentExecutionException(e);
            }
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
