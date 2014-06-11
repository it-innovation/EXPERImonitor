/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road,
// Chilworth Science Park, Southampton, SO16 7NS, UK.
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
//	Created Date :			2014-04-10
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.ac.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.ProjectConfigAccessorConfiguration;

/**
 * Grabs default project configuration properties from application.properties.
 */
@ConfigurationProperties(prefix = "ecc")
public class LocalConfiguration {

    private EccConfiguration configuration;
    private ProjectConfigAccessorConfiguration projectconfig;

    public EccConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(EccConfiguration configuration) {
        this.configuration = configuration;
    }

    public ProjectConfigAccessorConfiguration getProjectconfig() {
        return projectconfig;
    }

    public void setProjectconfig(ProjectConfigAccessorConfiguration projectconfig) {
        this.projectconfig = projectconfig;
    }

}
