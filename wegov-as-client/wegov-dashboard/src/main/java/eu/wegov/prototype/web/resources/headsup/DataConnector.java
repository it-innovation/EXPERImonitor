package eu.wegov.prototype.web.resources.headsup;


import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Forum;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;
import uk.ac.itinnovation.soton.wegov.hansard.User;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class DataConnector {


	//public static Factory getFactory(String filePath) {
    public static Factory getFactory() {
    //String inputFile = filePath;

		String inputFile = "./HeadsUp_forum.xls";
		File inputWorkbook = new File(inputFile);
		System.out.println("Working with file: " + inputFile);

		Workbook w;
		Factory f = new Factory();

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			Sheet sheet = w.getSheet(0);
			// Loop over first 10 column and lines

			int numColumns = sheet.getColumns();
			int numRows = sheet.getRows();

			String[] columnNames = new String[numColumns];

			// Use first row for labels
			for (int i = 0; i < numColumns; i++) {
				columnNames[i] = sheet.getCell(i, 0).getContents();
			}

			Cell cell;
			String cellContents;
			String subjectContents;
			String messageContents;
			String currentTimePublishedAsString;
			Forum currentForum;
			Thread currentthread;
			User currentUser;
			Timestamp currentTimePublished;

//			for (int rowNum = 1; rowNum < numRows; rowNum++) {
			for (int rowNum = 1; rowNum < numRows; rowNum++) {

				// Forum
				cell = sheet.getCell(0, rowNum);
				cellContents = cell.getContents().trim();
				currentForum = f.addForum(cellContents);

				// thread
				cell = sheet.getCell(1, rowNum);
				cellContents = cell.getContents().trim();
				currentthread = f.addthread(currentForum.getId(), cellContents);

				// User
				cell = sheet.getCell(5, rowNum);
				cellContents = cell.getContents().trim();
				currentUser = f.addUser(cellContents, "user");

				// Subject
				cell = sheet.getCell(2, rowNum);
				subjectContents = cell.getContents().trim();

				// Message
				cell = sheet.getCell(3, rowNum);
				messageContents = cell.getContents().trim();

				// Time Published - 17/06/2011 15:45
				cell = sheet.getCell(4, rowNum);
				currentTimePublishedAsString = cell.getContents().trim();
//				System.out.println(currentForum + ": " + currentTimePublishedAsString);
				DateFormat formatter =  new SimpleDateFormat("MM/dd/yy HH:mm"); // It's displayed differently in Excel!
				currentTimePublished = new Timestamp(formatter.parse(currentTimePublishedAsString).getTime());

				f.addPost(currentthread.getId(), subjectContents, messageContents, currentTimePublished, currentUser.getId());

			}

			System.out.println("Found " + f.getForumsSize() + " unique forums with " + f.getthreadsSize() + " threads:");
//			for (Forum forum : f.getForums()) {
//				int forumId = forum.getId();
//				System.out.println(forum);
//				for (thread thread : f.getthreadsForForum(forumId)) {
//					System.out.println("\t-" + thread);
////					for (Post post : f.getPostsForthread(thread.getId())) {
////						System.out.println("\t\t-" + post);
////						System.out.println("\t\t" + post.getContents() + "\n");
////					}
//				}
//			}

//			System.out.println("Found " + f.getUsersSize() + " unique users:");
//			for (User user : f.getUsers()) {
//				System.out.println(user);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return f;
	}

}
