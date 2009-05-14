package org.meandre.components.core.webui;



import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;



public class VelocityTemplateService {
	
	
	static private VelocityTemplateService instance = null;
	
	protected VelocityTemplateService() 
	   throws Exception
	{
		
		Properties p = new Properties();
		p.setProperty("resource.loader", "file,class" );
		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
	    p.setProperty("file.resource.loader.path", "published_resources/templates, WEB-INF/templates, ./templates");
	                               
	    Velocity.init( p );
	    
	    //Velocity.init("WEB-INF/velocity.properties");
	}
	
	
	public static VelocityTemplateService getInstance()
	{
		if (instance == null) {
			
			try {
				instance = new VelocityTemplateService();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public VelocityContext getNewContext()
	{
		return new VelocityContext();
	}
	
	public String generateOutput(VelocityContext context, String templateName)
	   throws Exception
	{
		Template template = null;
		try {
			template = Velocity.getTemplate(templateName);
		}
		catch (ResourceNotFoundException rnf) {
			throw new RuntimeException("Unable to find the template " + templateName);
		}
		catch (ParseErrorException pee) {
			throw new RuntimeException("Unable to parse the template " + templateName);
		}
        
        StringWriter sw = new StringWriter();
    	template.merge(context,sw);
    	return sw.toString();
	}
	

}
