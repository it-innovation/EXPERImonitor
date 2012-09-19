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
//	Created Date :			2012-09-19
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.context;

import eu.experimedia.itinnovation.ecc.web.helpers.ExperimentMonitorHelper;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class EccContextLoaderListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(EccContextLoaderListener.class);

    public void contextInitialized(ServletContextEvent sce) {
        logger.debug("Initializing EM Host");

        try {
            ServletContext ctx = sce.getServletContext();
            WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(ctx);
            ExperimentMonitorHelper eHelper = (ExperimentMonitorHelper) springContext.getBean("experimentMonitorHelper");
            eHelper.getExperimentMonitor();
            logger.debug("EM Host initialization successful");
        } catch (Throwable e) {
            logger.error("Application failed to initialise EM host", e);
            throw new RuntimeException(e);

        }

    }

    public void contextDestroyed(ServletContextEvent sce) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}