/**
 * Copyright (c) 2003 - 2008 Observant Pty Ltd 
 * All rights reserved.
 */
package eu.wegov.prototype.web.resources.headsup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author jima
 *
 */
public class BaseCampResource extends ServerResource {
	
	@Get("html")
	public Representation retrieveWeb() {
		String s = null;
		try {
			s = FileUtils.readFileToString("src/main/webapp", "index.html");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BaseCampResource.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BaseCampResource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return new StringRepresentation(s, MediaType.TEXT_HTML,
                                           Language.ENGLISH,
                                           CharacterSet.UTF_8);
	}

}
