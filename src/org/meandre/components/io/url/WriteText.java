/**
 *
 */
package org.meandre.components.io.url;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;

/** Reads a Jena Model from disk
 *
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Write text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		//dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, io, read, text",
		description = "This component write text into a file. The component outputs the test. " +
				      "A property allows to control " +
				      "the behaviour of the component in front of an IO error, allowing to continue " +
				      "pushing and empty model or throwing and exception forcing the finalization of " +
				      "the flow execution."
)
public class WriteText extends AbstractExecutableComponent {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name=Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and empty models will be pushed. " +
					      "Otherwise, the component will throw an exception an force the flow to abort.",
		    defaultValue = "true"
		)
	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the model to write"
		)
	private final static String INPUT_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text to write"
		)
	private final static String INPUT_TEXT = Names.PORT_TEXT;

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the written XML"
		)
	private final static String OUTPUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text written"
		)
	private final static String OUTPUT_TEXT= Names.PORT_TEXT;

	//--------------------------------------------------------------------------------------------

	/** The error handling flag */
	private boolean bErrorHandling;


	//--------------------------------------------------------------------------------------------


	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initializeCallBack(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void disposeCallBack(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.bErrorHandling = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void executeCallBack(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object objLoc = cc.getDataComponentFromInput(INPUT_LOCATION);
		Object objDoc = cc.getDataComponentFromInput(INPUT_TEXT);

		if ( objLoc instanceof StreamDelimiter || objDoc instanceof StreamDelimiter ) {
			pushDelimiters(cc, objLoc, objDoc);
		}
		else {
			//Formatting filename to insert time stamp between filename and last file extension
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			int index = objLoc.toString().lastIndexOf(".");
			String sLocation;
			if (index == -1)
				 sLocation = objLoc.toString()+formatter.format(now);
			else
				 sLocation = objLoc.toString().substring(0, index)+formatter.format(now)+objLoc.toString().substring(index);
			String     sText = objDoc.toString();
			try {
				Writer wrtr = openWriter(cc.getPublicResourcesDirectory()+File.separator+sLocation);
				wrtr.write(sText);
				wrtr.close();

				URL outputURL = new URL(cc.getProxyWebUIUrl(true), "/public/resources/" + sLocation);
				console.info("File written "+
						"and accessible at "+ outputURL);
			}
			catch (Throwable t) {
				String sMessage = "Could not transform XML document into text";
				cc.getLogger().warning(sMessage);
				console.warning(sMessage);
				if ( !bErrorHandling )
					throw new ComponentExecutionException(sMessage+" "+t.toString());
			}
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			cc.pushDataComponentToOutput(OUTPUT_TEXT, objDoc);
		}

	}

	/** Pushes the obtained delimiters
	 *
	 * @param cc The component context
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws ComponentContextException Push failed
	 */
	private void pushDelimiters(ComponentContext cc, Object objLoc,
			Object objDoc) throws ComponentContextException {
		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			cc.pushDataComponentToOutput(OUTPUT_TEXT, objDoc);
		}
		else
			pushMissalignedDelimiters(cc,objLoc, objDoc);
	}

	/** Push the delimiters to the outputs as needed.
	 *
	 * @param cc The component context
	 * @param objLoc The location delimiter
	 * @param objDoc The document delimiter
	 * @throws ComponentContextException Push failed
	 */
	private void pushMissalignedDelimiters(ComponentContext cc, Object objLoc, Object objDoc)
	throws ComponentContextException {
		String sMsg = "Missaligned delimiters receive, reusing delimiters to banlance the streams";
		cc.getOutputConsole().println("[WARNING] "+sMsg);
		cc.getLogger().warning(sMsg);
		if ( objLoc instanceof StreamDelimiter ) {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objLoc);
			cc.pushDataComponentToOutput(OUTPUT_TEXT, objLoc);
		}
		else {
			cc.pushDataComponentToOutput(OUTPUT_LOCATION, objDoc);
			cc.pushDataComponentToOutput(OUTPUT_TEXT, objDoc);
		}
	}

	//-----------------------------------------------------------------------------------

	/** Opens a writer to the location where to write.
	 *
	 * @param sLocation The location to write to
	 * @return The writer for this location
	 * @throws IOException The location could not be read
	 */
	private Writer openWriter(String sLocation) throws IOException {
		try {
			// Try too pull it as a URL
			URL url = new URL(sLocation);
			if ( url.getProtocol().equalsIgnoreCase("file") )
				return new FileWriter(new File(url.toString().substring(7)));
			else
				// Not a file, assuming unsupported format
				throw new MalformedURLException();
		} catch (MalformedURLException e) {
			// Badly formated UR or remoteL. Trying as a local file
			try {
				return new FileWriter(sLocation);
			} catch (FileNotFoundException e1) {
				throw e1;
			}
		} catch (IOException e) {
			throw e;
		}
	}
}
