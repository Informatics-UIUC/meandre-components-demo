package org.meandre.components.core.webui;


import java.util.concurrent.Semaphore;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

//import org.apache.velocity.exception.ParseErrorException;
//import org.apache.velocity.exception.ResourceNotFoundException;


/* 
 * template loading notes:
 * 
 *  templates are searched in
 *     1 local file system on the server: published_resources/templates (under the server install)
 *     2 local file system on the server: ./templates  where . is user.path
 *     3 on the classpath
 *     4 in any jars on the classpath
 *     
 *     the default template is GUITemplate.vm
 * 
 *  local files take precedence over class path files 
 *  
 *  ComponentContextProperties  are in the velocity context as ccp
 *  ComponentContext             is in the velocity context as cc
 *  HttpResponse                 is in the velocity context as response
 *  User supplied key=value pairs are in the velocity context as userMap
 *  
 *  TODO: it would be nice to be able to put the HttpRequest object
 *  in the velocity context
 */

@Component(creator="Mike Haberman",
           description="Generates and displays a webpage via a Velocity Template ",
           name="TemplateGUI",
           tags="string, visualization",
           baseURL="meandre://seasr.org/components/",
           dependency={"velocity-1.6.1-dep.jar"},
           resources={"TemplateGUI.vm"},
           mode=Mode.webui)
public class TemplateGUI
    implements ExecutableComponent, WebUIFragmentCallback {
	
	@ComponentProperty(description = "The template name", 
			                  name = "template", 
			          defaultValue = "org/meandre/components/core/webui/TemplateGUI.vm")
	public final static String DATA_PROPERTY_TEMPLATE = "template";
	
	@ComponentProperty(description = "User supplied property list", 
			                  name = "properties", 
			          defaultValue = "key=value,author=mike")
	public final static String DATA_PROPERTY_HASHTABLE = "properties";
	
	@ComponentProperty(description = "Generate meta refresh html after execute", 
                              name = "doRefresh", 
                      defaultValue = "true")
    public final static String DATA_PROPERTY_REFRESH = "doRefresh";
	
	
	//
	// this is a generic input, doesn't have to be used, up to the template
	// 
	/*
    @ComponentInput(description="Name of input port", name= "input")
    public final static String DATA_INPUT = "input";
    */
    
    @ComponentOutput(description="Name of output port", name= "output")
    public final static String DATA_OUTPUT = "output";
    
    
    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    
    /* what field will we check for in the HTTPRequest */
    // the template should actually set this variable via $gui.setPushValue()
    protected String formInputName = "done";
    
    // convenience properties to easily push additional properties 
    // not needed, template can always do $ccp.getProperty("title")
    protected String[] templateVariables = {};
    
    
    // generate meta refresh html after execute
    protected boolean doRefresh = true;
    
    
    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arose during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException 
    {
    	// TODO: it would be nice to see the request object here
    	generateContent(null, response);
    }
    
    
    protected void generateContent(HttpServletRequest request, HttpServletResponse response)
       throws WebUIException
    {
        try {
        	// request could be null on the "first" request
        	context.put("request",  request);
        	context.put("response", response);
        	
        	// render the template
        	VelocityTemplateService velocity = VelocityTemplateService.getInstance();
            String html = velocity.generateOutput(context, templateName);
    		
    		// write the template to the response stream
            response.getWriter().println(html);
            
        } catch (Exception e) {
            throw new WebUIException(e);
        }
    	
    }
    
    // exposed to the template
    public void setPushValue(String parameterName)
    {
    	formInputName = parameterName;
    }
    
    protected void generateMetaRefresh(HttpServletResponse response)
    throws IOException
    {
 	   PrintWriter writer = response.getWriter();
       writer.println("<html><head><title>Refresh Page</title>");
       writer.println("<meta http-equiv='REFRESH' content='0;url=/'></head>");
       writer.println("<body>Refreshing Page</body></html>");
    }

    //
    // not only check errors, process the input at this step
    // return false on errors, bad input, etc
    // return true if all is good
    protected boolean processRequest(HttpServletRequest request)
       throws IOException 
    {
    	// server side form validation goes here
    	// any processing of the request parameters
    	return true;
    }
    
    protected boolean expectMoreRequests(HttpServletRequest request)
    {
    	// we are done, if the 'formInputName' is in the request
    	// e.g. ?done=true or whatever
    	// if the request does NOT contain this parameter
    	// we will assume, we are expecting more input
    	return (request.getParameter(formInputName) == null);
    }
    
    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem arose during the call back
     */
    
    Map<String, String[]>parameterMap;
    @SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response) 
       throws WebUIException 
    {
    	parameterMap = (Map<String, String[]>) request.getParameterMap();
    	
    	//
    	// this is the workhorse method
    	// process the input, determine any errors, 
    	// set up any output that will be pushed
    	//
    	try {
    	   if (! processRequest(request)) {
    	    	// TODO: populate the velocity context with error objects
    	    	// regenerate the template
    		    // TODO: put the request parameters or a wrapper in the context
    		   context.put("hasErrors", new Boolean(true));
    		   generateContent(request, response);
    		   return;
    	    }
    	} catch(IOException ioe) {
    		throw new WebUIException(ioe);
    	}
    	
    	//
    	// see if this component can handle multiple requests before
    	// releasing the semaphore,
    	if (expectMoreRequests(request)) {
    		return;
    	}
    	
           
    	// No Errors, 
    	// just push the browser to the "next" component
        try{
        	// when the page has been handled, generate a browser refresh
        	if (doRefresh) {
               generateMetaRefresh(response);
        	}
         }catch (IOException e) {
            throw new WebUIException("unable to generate redirect response");
         }
         finally {
        	sem.release();
         }
    }
    
    
    protected void subExecute(ComponentContext cc) 
          throws ComponentExecutionException, 
                 ComponentContextException      
    {
    	// if TemmplateGUI is subclassed, you can use this
    	// extension point for execute() without having to 
    	// worry about semaphores, web fragments,
    	
    }
    
    protected void subPushOutput(ComponentContext cc)
       throws ComponentExecutionException, 
              ComponentContextException
    {
    	// default is to push the formInputName value
    	// now push the output to the next component
        // allow subclasses to push a form value or ParameterParser object
        //
    	String defaultOutput = parameterMap.get(formInputName)[0];
        //console.println("pushing " + defaultOutput);
        //console.flush();
        cc.pushDataComponentToOutput(DATA_OUTPUT, defaultOutput);
    }
    

    
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exception occurred during execution
    * @throws ComponentContextException Illegal access to context
    */
    public void execute(ComponentContext cc) 
       throws ComponentExecutionException, ComponentContextException {
    	
        try {
        	
        	// allow subclasses to do execute
        	subExecute(cc);
        	
            String sInstanceId = cc.getExecutionInstanceID();
            context.put("sInstanceId", sInstanceId);
            context.put("cc", cc);
            context.put("gui", this);
            
            sem.acquire();
            cc.startWebUIFragment(this);
            
            // block waiting for any web requests
            sem.acquire();
            cc.stopWebUIFragment(this);
            
            // allow subclasses to determine what to push to the output
            subPushOutput(cc);
            
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }
    
    protected void subInitialize(ComponentContextProperties ccp) {}
    
    protected VelocityContext context;
    protected String templateName;
    
    /**
     * Called when a flow is started.
     */
    protected PrintStream console;
    public void initialize(ComponentContextProperties ccp) {
    	
    	console = ccp.getOutputConsole();
    	
    	try {
    		
    		templateName = ccp.getProperty(DATA_PROPERTY_TEMPLATE);
    		String tf = ccp.getProperty(DATA_PROPERTY_REFRESH);
    		this.doRefresh = Boolean.parseBoolean(tf);
    		
    		
    		VelocityTemplateService velocity = VelocityTemplateService.getInstance();
            context = velocity.getNewContext();  
    		
            /*
             *  Make a context object and populate with the data.  This
             *  is where the Velocity engine gets the data to resolve the
             *  references (ex. $date) in the template
             */

            context = new VelocityContext();
            context.put("dir",  System.getProperty("user.dir"));
            context.put("date", new Date());
            context.put("ccp",  ccp);
            
            String toParse = ccp.getProperty(DATA_PROPERTY_HASHTABLE);
            HashMap<String,String> map = new HashMap<String,String>();
            StringTokenizer tokens = new StringTokenizer(toParse, ",");
            while (tokens.hasMoreTokens()){
            	String kv = tokens.nextToken();
            	int idx = kv.indexOf('=');
            	if (idx > 0) {
            		String key   = kv.substring(0,idx);
            		String value = kv.substring(idx+1);
            		map.put(key.trim(), value.trim());
            	}
            }
            context.put("userMap", map);
            
            // push property values to the context
            for (String name: templateVariables) {
            	String value = ccp.getProperty(name);
            	context.put(name,value);
            }
            
            subInitialize(ccp);
            
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(e);
    	}
    }
    
   
    /**
     * Call at the end of an execution flow.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
    
 
}

/* OLD WAY
private String getVizOLD() {
	
	
    StringBuffer sb = new StringBuffer();

    sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
    sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    sb.append("<head>\n");
    sb.append("<title>String Visualization</title>\n");
    sb.append("<style type=\"text/css\" media=\"screen\">\n");
    sb.append("body {\n");
    sb.append("font-family: Verdana, sans-serif;\n");
    sb.append("font-size: 1em;\n");
    sb.append("}\n");
    sb.append("</style>\n");
    sb.append("</head>\n");

    sb.append("<body>\n");
    sb.append("<br /><br />\n");
    sb.append("<p>");
    StringTokenizer st = new StringTokenizer(inputString, "\n");
    while(st.hasMoreTokens()) 
        sb.append(st.nextToken()).append("<br/>");
    sb.append("</p>");
    sb.append("<div align=\"center\">\n");
    sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                 sInstanceID + "?done=true\">DONE</a></font></table>\n");
    sb.append("</div>\n");
    sb.append("</body>\n");
    sb.append("</html>\n");

    return sb.toString();
}
*/