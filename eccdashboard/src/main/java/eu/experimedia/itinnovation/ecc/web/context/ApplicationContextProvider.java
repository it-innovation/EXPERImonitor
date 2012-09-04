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
//	Created Date :			2012-08-17
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.context;

import java.util.Locale;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;


public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext ctx;
    private static MessageSource resource;

    public void setApplicationContext(final ApplicationContext ctx) {
        this.ctx = ctx;
    }


    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    /**
     * Get the localized version of the message.
     * 
     * @param aResource
     * @return The translated resource
     */
    public static String translateMessage(final String aResource, final Locale locale) {
        String aMessage = aResource;
        try {
            initResource();
            aMessage = resource.getMessage(aResource, null, locale);
        } catch (final Exception e) {
        }
        return aMessage;
    }

    private static void initResource() {
        try {
            if (resource == null) {
                resource = (MessageSource) ctx.getBean("messageSource");
            }
        } catch (final Exception e) {
        }
    }
}
