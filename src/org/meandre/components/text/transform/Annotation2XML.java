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

package org.meandre.components.text.transform;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Enumeration;
import java.util.Set;
import java.util.Hashtable;

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
import org.w3c.dom.Attr;


@Component(creator = "Loretta Auvil & Lily Dong",
		   description = "<p>Overview: <br> This component extracts the " +
		   "annotations from an annotated text document and outputs them " +
           "as xml document. Only those entity types specified in this component's " +
           "properties will be included in the output XML doucment.</p>",
           name = "Annotation2XML",
           tags = "text, document, annotation",
           baseURL="meandre://seasr.org/components/")

public class Annotation2XML implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output? A boolean value " +
					   "(true or false).",
					   name = "verbose",
					   defaultValue = "false")
	final static String DATA_PROPERTY_VERBOSE = "verbose";

	@ComponentProperty(description = "Entity types (comma delimited list).",
					   name = "Entities",
					   defaultValue =  "person,organization,location,time,money,percentage,date")
	final static String DATA_PROPERTY_ENTITIES = "Entities";

	@ComponentInput(description = "Input document to be read." +
	        "<br>TYPE: org.seasr.components.text.datatype.corpora.Document ",
			 		name = "Document")
	public final static String DATA_INPUT_DOC_IN = "Document";

	@ComponentOutput(description = "Extracted annotations as XML document." +
	            "<br>TYPE: org.w3c.dom.Document",
					 name = "Annotation_xml")
	public final static String DATA_OUTPUT_ANNOTATIONS = "Annotation_xml";

	private static Logger _logger = Logger.getLogger("AnnotationToXML");

	//Store properties.
	private String verbose;
	private String entities;

	public void dispose(ComponentContextProperties ccp)
	throws ComponentExecutionException, ComponentContextException {
		// TODO Auto-generated method stub
	}

	public void execute(ComponentContext ctx)
	throws ComponentExecutionException, ComponentContextException {
		_logger.fine("execute() called");
		Document doc_in = (Document)
		ctx.getDataComponentFromInput(DATA_INPUT_DOC_IN);

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
        	docBuilder = dbfac.newDocumentBuilder();
        }catch(ParserConfigurationException e) {
        	throw new ComponentExecutionException(e);
        }
        org.w3c.dom.Document doc_out = docBuilder.newDocument();

		AnnotationSet as = doc_in
			.getAnnotations(AnnotationConstants.ANNOTATION_SET_ENTITIES);
		Iterator<Annotation> itty = as.iterator();

		AnnotationSet as2 = doc_in.
			getAnnotations(AnnotationConstants.ANNOTATION_SET_SENTENCES);

		Element root = doc_out.createElement("root");
        doc_out.appendChild(root);
        root.setAttribute("docID", doc_in.getDocID());
		System.out.println("docID: " + doc_in.getDocID());

		Hashtable<String, Element> ht = new Hashtable<String, Element>();

		while (itty.hasNext()) {
			Annotation ann = itty.next();
			if(entities.indexOf(ann.getType()) != -1) {
				AnnotationSet subSet =
					as2.get(ann.getStartNodeOffset(),
							ann.getEndNodeOffset());
				Iterator<Annotation> itty2 = subSet.iterator();
				StringBuffer buf = new StringBuffer();
				while(itty2.hasNext()) {
					Annotation item = itty2.next();
					buf.append(item.getContent(doc_in).trim());
				}
				String value = buf.toString();
				//some sentences extracted are surrounded by void ".
				value = value.replaceAll("\"", " ");

				String s = ann.getContent(doc_in).trim().toLowerCase();
				if(ht.containsKey(s)) {
					Element child = ht.get(s);
					Attr attr = child.getAttributeNode("sentence");
					//append new sentences using | as list separator.
					attr.setNodeValue(attr.getNodeValue() + " | " + value);
					continue;
				}

				Element child = doc_out.createElement(ann.getType());
				Text text = doc_out.createTextNode(s);
				Attr attr = doc_out.createAttribute("sentence");
				attr.setNodeValue(value);
		        child.appendChild(text);
		        child.setAttributeNode(attr);
		        root.appendChild(child);
		        ht.put(s, child);
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


