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

package eu.wegov.web.controller;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;


public class InitController implements ServletContextAware , InitializingBean {
    
    private ServletContext sc;
    private final Logger logger = Logger.getLogger(InitController.class);
    
    public InitController() {
        System.out.println("Inside InitController");
    }

    public void setServletContext(ServletContext sc) {
        this.sc = sc;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("Running afterPropertiesSet");
    }

}
