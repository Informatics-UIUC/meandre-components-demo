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

package org.meandre.components.io.table;

import org.meandre.components.datatype.table.Table;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;

@Component(creator="Lily Dong",
        description="Extracts the content of a specified table column and " +
        "outputs it as string.",
        name="TableColumnExtractor",
        tags="table extractor",
        baseURL="meandre://seasr.org/components/")

public class TableColumnExtractor extends AbstractExecutableComponent
{
	@ComponentProperty(defaultValue="",
			description="This property specifies the name of column to be extracted.",
            name="columName")
    final static String DATA_PROPERTY = "columName";

	@ComponentInput(description="Reads a table as input." +
			"<br>TYPE: org.meandre.components.datatype.table.Table",
	                name= "table")
	final static String DATA_INPUT = "table";

	@ComponentOutput(description="Outputs the content as stream of string, " +
    		"which is initialized by StreamInitiator and followed by StreamTerminator." +
    		"<br>TYPE: <br>org.meandre.core.system.components.ext.StreamInitiator" +
            "<br>   THEN" +
            "<br>java.lang.String (multiple times)" +
            "<br>   THEN" +
            "<br>org.meandre.core.system.components.ext.StreamTerminator",
                     name="string")
    public final static String DATA_OUTPUT = "string";

	/** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exception occurred during execution
     * @throws ComponentContextException Illegal access to context
     */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	String columName = cc.getProperty(DATA_PROPERTY);
    	Table table = (Table) cc.getDataComponentFromInput(DATA_INPUT);
    	int nrRows = table.getNumRows(),
    	    nrColumns = table.getNumColumns();
    	cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamInitiator());
    	int column;
    	for(column=0; column<nrColumns; column++) { //find the column with the specified name
    		String theName = table.getColumnLabel(column);
    		if(theName.equals(columName))
    			 break;
    	}
    	for(int row=0; row<nrRows; row++) {
    		cc.pushDataComponentToOutput(DATA_OUTPUT, table.getString(row, column));
    		//System.out.println(table.getString(row, column));
        }
    	cc.pushDataComponentToOutput(DATA_OUTPUT, new StreamTerminator());
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
