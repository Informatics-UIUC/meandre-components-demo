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

package org.meandre.demo.components.io;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.seasr.components.text.datatype.corpora.Annotation;
import org.seasr.components.text.datatype.corpora.AnnotationConstants;
import org.seasr.components.text.datatype.corpora.AnnotationSet;
import org.seasr.components.text.datatype.corpora.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


@Component(creator = "Loretta Auvil & Lily Dong",

		description = "<p>Overview:<br> This component extracts the " + 
		"annotations from the document and outputs them as xml.</p>",
		name = "AnnotationToXML", 
		tags = "text document annotation")

public class AnnotationToXML implements ExecutableComponent {

	@ComponentProperty(description = "Verbose output? A boolean value " + 
					   "(true or false).",
					   name = "verbose", 
					   defaultValue = "false")
	final static String DATA_PROPERTY_VERBOSE = "verbose";

	@ComponentProperty(description = "Entity types (comma delimited list).", 
					   name = "entities", 
					   defaultValue =  "person,organization,location,time,money,percentage,date")
	final static String DATA_PROPERTY_ENTITIES = "entities";

	@ComponentInput(description = "Input document.", 
			 		name = "document_in")
	public final static String DATA_INPUT_DOC_IN = "document_in";

	@ComponentOutput(description = "Extracted annotations as XML.", 
					 name = "annot_xml")
	public final static String DATA_OUTPUT_ANNOTATIONS = "annot_xml";

	private static Logger _logger = Logger.getLogger("AnnotationToXML");

	private String verbose;
	private String entities;

	public void dispose(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub

	}

	public void execute(ComponentContext ctx)
	throws ComponentExecutionException, ComponentContextException {		
		_logger.fine("execute() called");
		Document doc = (Document)  
		ctx.getDataComponentFromInput(DATA_INPUT_DOC_IN);
		
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
        	docBuilder = dbfac.newDocumentBuilder();
        }catch(ParserConfigurationException e) {
        	throw new ComponentExecutionException(e);
        }
        org.w3c.dom.Document doc_out = docBuilder.newDocument();
		
		AnnotationSet as = doc
			.getAnnotations(AnnotationConstants.ANNOTATION_SET_ENTITIES);
		Iterator<Annotation> itty = as.iterator();
		
		Element root = doc_out.createElement("root");
        doc_out.appendChild(root);
        root.setAttribute("docID", doc.getDocID());
		System.out.println("docID: " + doc.getDocID());
		
		Set<String> ts = new TreeSet<String>();
		
		while (itty.hasNext()) {
			Annotation ann = itty.next();
			if(entities.indexOf(ann.getType()) != -1) {
				Element child = doc_out.createElement(ann.getType());
				String s = ann.getContent(doc).trim();
				if(ts.contains(s))
					continue;
				Text text = doc_out.createTextNode(s);
				ts.add(s);
		        child.appendChild(text);
		        root.appendChild(child);
				System.out.println("Entity: " + ann.getContent(doc) + " <"
						+ ann.getType() + ">");
			}
		}

		 //set up a transformer
		try {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        //create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc_out);
        trans.transform(source, result);
        String xmlString = sw.toString();

        //print xml
        System.out.println("Here's the xml:\n\n" + xmlString);
		} catch(Exception e) {}
		
		// if statement to check ann.getType() to the property DATA_PROPERTY_ENTITIES
		// write xml output including doc.getDocID(), ann.getContent(doc), and ann.getType()
		ctx.pushDataComponentToOutput(DATA_OUTPUT_ANNOTATIONS, doc_out);
	}

	public void initialize(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		verbose = (ccp.getProperty(DATA_PROPERTY_VERBOSE)).toLowerCase();
		entities = ccp.getProperty(DATA_PROPERTY_ENTITIES);
	}

}


