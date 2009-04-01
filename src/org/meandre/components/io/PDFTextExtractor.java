package org.meandre.components.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageNode;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.tools.locator.ByteArrayLocator;

/** This class provides methods related to text extraction from PDF files
 *
 * @author Boris Capitanu
 * @author Xavier Llor&agrave;
 * @author Loretta Auvil
 *
 */

@Component(creator="Loretta Auvil",
        description="This component extracts the text from a pdf document. "+
        "The input is a String or URL specifiying the url of the pdf document. "+
        "The output is the extracted text.",
        name="PDFTextExtractor",
        tags="URL, text, pdf",
        baseURL="meandre://seasr.org/components/")

        public class PDFTextExtractor extends AbstractExecutableComponent {

    @ComponentInput(description="URL of the pdf file." +
            "<br>TYPE: java.io.String",
            name="URL")
            public final static String DATA_INPUT = "URL";
    @ComponentOutput(description="Text of the pdf file." +
            "<br>TYPE: java.io.String",
            name="Text")
            public final static String DATA_OUTPUT = "Text";

    //private PrintStream console;

    public void initializeCallBack(ComponentContextProperties ccp)
        throws Exception {

        //console = ccp.getOutputConsole();
        getConsoleOut().println("Initializing PDFTextExtrator for " + ccp.getFlowID());
    }

    public void executeCallBack(ComponentContext cc)
        throws Exception {

        URL url;

        try {
            if (cc.getDataComponentFromInput(DATA_INPUT).getClass().getName() == "java.lang.String")
                url = new URL ((String) cc.getDataComponentFromInput("URL"));
            else if (cc.getDataComponentFromInput(DATA_INPUT).getClass().getName() == "java.net.URL")
                url = (URL) cc.getDataComponentFromInput("URL");
            else {
            	getConsoleOut().println("PDFTextExtractor must receive a java.lang.String or a java.net.URL type");
                url = null;
            }
        } catch(java.net.MalformedURLException e) {
            throw new ComponentExecutionException(e);
        }

        if (url != null && url.toString().toLowerCase().endsWith(".pdf")) {
            try {
                byte[] pdfData;
                String pdfText;

                InputStream dataStream = url.openStream();
                try {
                    pdfData = getBytesFromStream(dataStream);
                } finally {
                    dataStream.close();
                }

                PDDocument pdfDoc = PDDocument.createFromLocator(
                        new ByteArrayLocator(pdfData, url.toString(), null));
                try {
                    StringBuilder sb = new StringBuilder();
                    extractText(pdfDoc.getPageTree(), sb);
                    pdfText = sb.toString();
                } finally {
                    pdfDoc.close();
                }

                cc.pushDataComponentToOutput(DATA_OUTPUT, pdfText);
            }
            catch (IOException ioex) {
                //ioex.printStackTrace();
            	componentConsoleHandler.whenLogLevelOutput("verbose" , ioex);
                throw new ComponentExecutionException(ioex);
            }
            catch (COSLoadException coslex) {
                //coslex.printStackTrace();
            	componentConsoleHandler.whenLogLevelOutput("verbose" , coslex);
                throw new ComponentExecutionException(coslex);
            }
        } else
        	getConsoleOut().println("PDFTextExtractor can only process PDF files (*.pdf)");
    }

    public void disposeCallBack(ComponentContextProperties ccp)
        throws Exception {
    }

    /**
     * Reads the content of an InputStream into a byte array
     *
     * @param dataStream The data stream
     * @return A byte array containing the data from the data stream
     * @throws IOException Thrown if a problem occurred while reading from the stream
     */
    private byte[] getBytesFromStream(InputStream dataStream) throws IOException {
        BufferedInputStream bufStream = new BufferedInputStream(dataStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int nRead;

        do {
            nRead = bufStream.read(buffer, 0, buffer.length);
            if (nRead > 0)
                baos.write(buffer, 0, nRead);
        } while (nRead > 0);

        return baos.toByteArray();
    }

    /**
     * Extracts text from the page tree of a PDF document
     *
     * @param pageTree The page tree root node
     * @param sb The StringBuilder to use to store the extracted text
     */
    @SuppressWarnings("unchecked")
    private void extractText(PDPageTree pageTree, StringBuilder sb) {
        for (Iterator it = pageTree.getKids().iterator(); it.hasNext();) {
            PDPageNode node = (PDPageNode) it.next();
            if (node.isPage()) {
                try {
                    CSTextExtractor extractor = new CSTextExtractor();
                    PDPage page = (PDPage) node;
                    CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(
                            null, extractor);
                    interpreter.process(page.getContentStream(), page
                            .getResources());
                    sb.append(extractor.getContent());
                } catch (CSException e) {
                    //e.printStackTrace();
                	componentConsoleHandler.whenLogLevelOutput("verbose" , e);
                }
            } else {
                extractText((PDPageTree) node, sb);
            }
        }
    }
}
