package me.devhelp.unityplugin;
import com.loopj.android.http.AsyncHttpClient;

import android.location.Location;

import com.loopj.android.http.*;

public class MarbleRestClient {
    private static final String USER_PARAM = "user";
    private static final String TOKEN_PARAM = "token";
    private String serverUrl;
    private String userToken;
    private String userId;
    private AsyncHttpClient client = new AsyncHttpClient();

    MarbleRestClient(String serverUrl, String userId, String userToken) {
        setServer(serverUrl);
        setCredentials(userId, userToken);
    }

    public void setServer(String server) {
        this.serverUrl = server;
    }
    public void setCredentials(String userId, String userToken) {
        this.userId = userId;
        this.userToken = userToken;
    }

    public void get(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        addCredentials(params);
        client.get(getAbsoluteUrl(path), params, responseHandler);
    }

    public void post(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        addCredentials(params);
        client.post(getAbsoluteUrl(path), params, responseHandler);
    }

    public void getSpotsAt(Location location, AsyncHttpResponseHandler responseHandler) {

    }

    private void addCredentials(RequestParams params) {
        params.add(USER_PARAM, this.userId);
        params.add(TOKEN_PARAM, this.userToken);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return serverUrl + relativeUrl;
    }

}
