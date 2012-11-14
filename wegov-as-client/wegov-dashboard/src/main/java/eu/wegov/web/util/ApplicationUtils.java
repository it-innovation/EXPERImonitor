package eu.wegov.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import eu.wegov.context.ApplicationContextProvider;

public class ApplicationUtils {

    private static final Logger LOGGER = Logger.getLogger(ApplicationUtils.class);

    public final static String DATE_PATTERN = "dd/MM/yyyy";
    public final static String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";

    // Date parsing static methods
    public static Date stringToDatetime(final String date) throws ParseException {
        return parseToDate(date, DATE_TIME_PATTERN);
    }

    // Date parsing static methods
    public static Date stringToDate(final String date) throws ParseException {

        return parseToDate(date, DATE_PATTERN);
    }

    public static Date stringToDate(final String date, final String pattern) throws ParseException {

        return parseToDate(date, pattern);
    }

    public static String dateToString(final Date date) {

        return parseToString(date, DATE_PATTERN);
    }

    public static String dateToString(final Date date, final String pattern) {

        return parseToString(date, pattern);
    }

    public static String parseToString(final Date date, final String pattern) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        return date != null ? dateFormat.format(date) : null;
    }

    public static Date parseToDate(final String date, final String pattern) throws ParseException {
        try {

            final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

            return dateFormat.parse(date);
        } catch (final ParseException pe) {
            LOGGER.error(pe.getMessage());
            throw pe;
        }
    }

    /**
     * this method extract and return a byte array from a File object.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getByteStreamFromFile(final File file) throws IOException {
        byte[] stream = null;
        if (file != null) {
            final FileInputStream fileInputStream = new FileInputStream(file);
            final int length = (int) file.length();
            stream = new byte[length];

            fileInputStream.read(stream, 0, length);
        }
        return stream;
    }

    /**
     * Return a Messagesource reference using the as bean name messageSource.
     * 
     * @return MessageSource Reference
     */
    public static MessageSource getMessageResource() {
        return (MessageSource) ApplicationContextProvider.getApplicationContext().getBean("messageSource");
    }
}
