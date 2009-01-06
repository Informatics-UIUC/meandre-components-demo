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

package org.meandre.components.text.transform;

import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.Node;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.nodes.TagNode;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Converts an HTML doc to plain text. All nodes from " +
           		"the dom tree that are plain text nodes are appended " +
           		"together and returned as a string.",
           name="HTML2Text",
           tags="html, text, converter"
)

public class HTML2Text implements ExecutableComponent {
    @ComponentInput(description="Read content in HTML format." +
                    "<br>TYPE: java.lang.String",
                    name= "Html")
    public final static String DATA_INPUT = "Html";
    
    @ComponentOutput(description="Output content in plain text format."+
            "<br>TYPE: java.lang.String",
                     name="Text")        
    public final static String DATA_OUTPUT = "Text";
    
    StringBuffer sb;
    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) 
        throws ComponentExecutionException, ComponentContextException {
        String inputHtml = (String)cc.getDataComponentFromInput(DATA_INPUT);
        Parser parser = new Parser();
        try {
            parser.setInputHTML(inputHtml); 
            NodeList list = parser.parse (null); 
            traverse(list);
        }catch(org.htmlparser.util.ParserException e) {
            throw new ComponentExecutionException(e);
        }
        
        //System.out.println(sb.toString());
        
        cc.pushDataComponentToOutput(DATA_OUTPUT, sb.toString());
    }
    
    /**
     * 
     * @param list to be traversed
     */
    private void traverse(NodeList list) {
        if(list != null)
            for(int i=0; i<list.size(); i++) {
                Node node = list.elementAt(i);
                if(node instanceof TextNode)
                    sb.append(((TextNode)node).getText().trim()).append("\n");
                else if(node instanceof TagNode) {
                    NodeList sublist = ((TagNode)node).getChildren();
                    traverse(sublist);
                }
            }
    }

    /**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
        sb = new StringBuffer();
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }   
}
