package eu.wegov.json;

import eu.wegov.coordinator.sql.PostgresConnector;

public class Controller {
	private PostgresConnector connector;

	public Controller(PostgresConnector connector) {
		this.connector = connector;
	}

	public JSONActivity[] getActivitiesForUser(String username) {
		
//		String preparedCommand = "SELECT * FROM \"" + tableName + "\" WHERE \""
//				+ key + "\"=?";
//		PreparedStatement p = getConnection().prepareStatement(
//				preparedCommand + ";");
//
//		p.setInt(counter, value);
//
//		logger.debug(p.toString());
//
//		ResultSet rs = p.executeQuery();
//
//		if (rs != null) {
//			while (rs.next()) {
//
//				int newValue = rs.getInt(key.toLowerCase());
//			}
//		}
//
		return new JSONActivity[] {};
	}
}
