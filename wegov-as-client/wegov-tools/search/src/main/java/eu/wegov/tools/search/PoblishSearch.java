package eu.wegov.tools.search;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.http.auth.AuthScope;

import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class PoblishSearch extends SingleSiteSearch {

	public PoblishSearch(WegovTool wegovTool) throws Exception {
		super(wegovTool, "poblish");
	}
	
	public void setupSearch() throws Exception {
		searchUrl = "http://www.poblish.org/poblish2/REST/feed/";
		searchParams.put("props", "feed:liberalconspiracy.org:ARTICLE,FLAG,FAVE,RATING,VERSION;actor:polly.toynbee:ARTICLE;actor:aregan:ARTICLE,FLAG,FAVE,RATING,VERSION;group:labourPartyUK:ARTICLE;");
		searchParams.put("maxEntries", "10");
		
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
	protected WegovPostItem jsonToPost(JSONObject object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected WegovSnsUserAccount jsonToUser(JSONObject jsonObject)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("poblish", "Poblish", "http://www.poblish.org/", "http://www.poblish.org/favicon.ico", outputOfRunID);
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
		return "posts-poblish";
	}

}
