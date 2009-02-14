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
        description="Reads content as stream and output it as string or byte array.",
        name="StreamContentReader",
        tags="stream, string",
        baseURL="meandre://seasr.org/components/")

public class StreamContentReader implements ExecutableComponent {
    @ComponentProperty(defaultValue="string",
                       description="This property sets the type of content. " +
                       "The type could be \'string\' or \'binary\'.",
                       name="contentType")
    final static String DATA_PROPERTY = "contentType";

    @ComponentInput(description="A raw byte stream to be read. " +
                    "<br>TYPE: java.io.InputStream",
                    name= "Stream")
    public final static String DATA_INPUT = "Stream";

    @ComponentOutput(description="Output content as string or byte array." +
                    "<br>TYPE: java.lang.String OR byte[]",
                     name="Object")
    public final static String DATA_OUTPUT = "Object";

    private final static String STRING_DELIMITER = "\n";
    private final static int ARRAY_LENGTH = 4096;
    private final static int BYTE_LENGTH = 1024;

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
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
                while((line = br.readLine())!= null) {
                    line = line.trim();
                    if(line.length() == 0)
                        continue;
                    sb.append(line).append(STRING_DELIMITER);
                }
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
        } else {
            DataInputStream dis = new DataInputStream(is);
            byte[] b = new byte[ARRAY_LENGTH];
            int  nrBytes = 0,
                 totalBytes = 0;
            try {
                while((nrBytes = dis.read(b, totalBytes, BYTE_LENGTH)) != -1) {
                    totalBytes += nrBytes;
                    if(!bytesAvailable(totalBytes, b.length)) {
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
     *
     * @param totalBytes_ The total bytes read from stream so far.
     * @param length_ The length of array holding data.
     * @return false if there are not enough bytes, otherwise true.
     */
    private boolean bytesAvailable(int totalBytes_, int length_) {
        return totalBytes_+BYTE_LENGTH >= length_?false: true;
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
