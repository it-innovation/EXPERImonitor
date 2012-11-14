package eu.wegov.prototype.web.resources.headsup;

import java.io.File;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class KoblenzDownloadResultsAsExcelFileResource extends ServerResource {

	@Get("html")
	public Representation retrieve() throws Exception {
		
		String filePath = getQuery().getFirstValue("filePath");
		System.out.println("Downloading file: " + filePath);
		
//		Router myRouter = (Router) getApplication().getInboundRoot();
//		Directory webDir = new Directory(getContext(), LocalReference.createFileReference("/Users/max/Documents/Work/wegov/Hansard data/restlet-basecamp/Jan 25, 2012 - 16_06_02, 4 topics.xls"));
//		
//		myRouter.attach("/abc", webDir);
		
		File fileToDownload = new File(filePath);
		FileRepresentation rep = new FileRepresentation(fileToDownload, MediaType.APPLICATION_EXCEL);
		Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
		disposition.setFilename(fileToDownload.getName());
		
		rep.setDisposition(disposition);
		return rep;
	}
}
