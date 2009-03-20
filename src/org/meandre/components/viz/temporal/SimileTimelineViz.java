package org.meandre.components.viz.temporal;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Lily Dong",
           description="Visualizes temporal XML document.",
           name="SimileTimelineViz",
           tags="simile, timeline",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/")

public class SimileTimelineViz
	implements ExecutableComponent, WebUIFragmentCallback {
	@ComponentInput(description="Read URL of an existing HTML file generated " +
			"by SimileTimelineGenerator. XML file is supposed to be " +
			"in the same directory with the HTML file." +
			"<br>TYPE: java.lang.String",
    		 name="Text")
  	public final static String DATA_INPUT = "Text";

	/** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The instance ID */
    private String sInstanceID = null;

    /** Store URL of a HTML file */
	private String urlOfHtml;


    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem raised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    }

    /** A simple message.
    *
    * @return The html containing the page
    */
    private String getViz() {
    	StringBuffer sb = new StringBuffer();

    	sb.append("<html>\n");

    	sb.append("<title>\n");
    	sb.append("Temporal Data Viewer");
    	sb.append("</title>\n");

    	sb.append("<body>\n");

    	sb.append("<iframe src=\"").append(urlOfHtml).append("\" width=\"100%\" height=\"30%\" FRAMEBORDER=0>\n");
    	sb.append("</iframe>\n");

    	sb.append("<div align=\"center\">\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
        		sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");

    	sb.append("</body>\n");
    	sb.append("</html>\n");

    	return sb.toString();
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arose during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        if ( sDone!=null ) {
            sem.release();
        }
        else
            emptyRequest(response);
    }

    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    /* (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
        ComponentContextException {
    	urlOfHtml = (String)cc.getDataComponentFromInput(DATA_INPUT);

    	try {
    		sInstanceID = cc.getExecutionInstanceID();
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
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
