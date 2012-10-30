/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-09-18
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.context;

import eu.experimedia.itinnovation.scc.web.helpers.ExperimentHelper;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class WegovContextLoaderListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(WegovContextLoaderListener.class);

    public void contextInitialized(ServletContextEvent sce) {
//        logger.debug("Initializing EM Client");
//
//        try {
//            ServletContext ctx = sce.getServletContext();
//            WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(ctx);
//            ExperimentHelper eHelper = (ExperimentHelper) springContext.getBean("experimentHelper");
//            eHelper.getClient();
//            logger.debug("EM Client initialization successful");
//        } catch (Throwable e) {
//            logger.error("Application failed to initialise EM client", e);
//            throw new RuntimeException(e);
//
//        }

    }

    public void contextDestroyed(ServletContextEvent sce) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}
