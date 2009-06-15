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

package org.meandre.components.jstor;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

@Component(creator="Lily Dong",
           description="Extracts text content from document.",
           name="Text Extractor",
           tags="text extractor",
           baseURL="meandre://seasr.org/components/")

public class TextExtractor extends AbstractExecutableComponent
{
	@ComponentInput(description="Read document." +
            "<br>TYPE:  org.w3c.dom.Document",
                name= "document")
    public final static String DATA_INPUT = "document";

	@ComponentOutput(description="Output text content extracted from document." +
			"<br>TYPE: java.lang.String",
	                 name="string")
	public final static String DATA_OUTPUT = "string";

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void executeCallBack(ComponentContext cc)
    throws Exception {
    	Document source = (Document)cc.getDataComponentFromInput(DATA_INPUT);
    	try {
    		listNodes(source.getDocumentElement(), "description");
        	//cc.pushDataComponentToOutput(DATA_OUTPUT, source);
    	}catch(Exception e) {
    		throw new ComponentExecutionException(e);
    	}
    }

    /**
     *
     * @param node root of document
     * @param info info to be extracted
     */
    private void listNodes(Node node, String info) {
    	String nodeName = node.getNodeName();
    	if (node instanceof Element && nodeName.equals("dc:description")) {
    		System.out.println(node.getTextContent());
    	    /*NamedNodeMap attrs = node.getAttributes();
    	    for (int i = 0; i < attrs.getLength(); i++) {
    	        Attr attribute = (Attr) attrs.item(i);
    	        System.out.println(attribute.getName() + "="
    	            + attribute.getValue());
    	    }*/
    	}

    	NodeList list = node.getChildNodes();
    	    if (list.getLength() > 0)
    	      for (int i = 0; i < list.getLength(); i++)
    	        listNodes(list.item(i), info);
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
