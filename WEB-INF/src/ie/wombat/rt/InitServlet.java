package ie.wombat.rt;

import ie.wombat.framework.AppException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class InitServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(InitServlet.class);
	
	/**
	 * Servlet init() method. This is called once in the servlets lifecycle
	 * (a the start of it). We do all our heavy duty initializing work
	 * here (eg reading config params for servlet config file, getting
	 * getting a handle on our datastore object, setting up the appResources
	 * object etc)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		BasicConfigurator.configure();
		
		/*
		 * Initialize our own templating wrapper
		 */
		log.info("Initializing temlpating system");
		try {
			ie.wombat.template.TemplateRegistry.getInstance().init(
				getServletContext().getRealPath("/templates"));
		} catch (AppException e) {
			e.printStackTrace();
			throw new ServletException(e.toString());
		}
	}
	
}
