package eu.wegov.context;

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