package com.restfb;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.logging.Level.INFO;

import java.util.List;
import java.util.Map;

import com.restfb.DefaultFacebookClient.Requestor;
import com.restfb.WebRequestor.Response;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookResponseStatusException;

public class WegovFacebookClient extends DefaultFacebookClient {

	public WegovFacebookClient(String accessToken) {
		super(accessToken);
	}

	protected String makeRequestAndProcessResponse(Requestor requestor) {
		int nRetries = 5;
		String connectionJson = null;
		
		for (int i = 0; i < nRetries; i++) {
			if (i>0) {
				System.out.println("Retry " + i);
			}
			
			try {
				connectionJson = super.makeRequestAndProcessResponse(requestor);
				break;
			} catch (FacebookResponseStatusException e) {
				e.printStackTrace();
			} catch (FacebookNetworkException e) {
				e.printStackTrace();
			}
		}
		
		return connectionJson;
	}

}
