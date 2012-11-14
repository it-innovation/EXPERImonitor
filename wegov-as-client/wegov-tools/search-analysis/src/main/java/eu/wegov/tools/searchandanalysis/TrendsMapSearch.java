package eu.wegov.tools.searchandanalysis;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.http.auth.AuthScope;

import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class TrendsMapSearch extends SingleSiteSearch {

	public TrendsMapSearch(WegovTool wegovTool) throws Exception {
		super(wegovTool, "trendsmap");
	}
	
	public void setupSearch() throws Exception {
		searchUrl = "http://trendsmap.com/search";
		searchParams.put("q", "southampton");
	}

	@Override
	protected void extractPosts(JSON json) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void extractUsers(JSON json) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void extractGroups(JSON json) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected WegovSnsUserAccount jsonToUser(JSONObject jsonObject)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WegovPostItem jsonToPost(JSONObject object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("trendsmap", "Trendsmap", "http://trendsmap.com/", "http://trendsmap.com/favicon.ico", outputOfRunID);
	}

	@Override
	protected String getUserIdFromPost(JSONObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setLocationViaAPI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getPostIdFromPost(JSONObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSearchType() {
		return "posts-trendsmap";
	}

}
