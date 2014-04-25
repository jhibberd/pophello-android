package com.example.pophello.app.model.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Abstract superclass for all API endpoints, which all return JSON. */
public abstract class Endpoint {

    protected enum HTTPMethod {
        GET,
        POST,
        DELETE
    }

    private static final String TAG = "Endpoint";
    private static final String META_DATA_SERVER_HOST = "com.example.pophello.ServerHost";
    private Context mContext;
    private HTTPMethod mMethod;

    public Endpoint(Context context, HTTPMethod method) {
        this.mContext = context;
        this.mMethod = method;
    }

    public void call() {

        // read API host from manifest
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "failed to get package manager");
            return;
        }
        String packageName = mContext.getPackageName();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "failed to get application into from package manager");
            return;
        }
        Bundle bundle = applicationInfo.metaData;
        if (bundle == null) {
            Log.e(TAG, "failed to get metadata from application info");
            return;
        }
        String host = bundle.getString(META_DATA_SERVER_HOST);

        // build URI (implemented by subclass)
        String uriString = String.format("http://%s:4000", host);
        Uri.Builder uriBuilder = Uri.parse(uriString).buildUpon();
        buildURI(uriBuilder);
        Uri uri = uriBuilder.build();
        if (uri == null) {
            Log.e(TAG, "failed to build API URI");
            return;
        }

        Log.i(TAG, "API request: " + uri.toString());
        new AsyncRequest().execute(uri);
    }

    protected abstract void buildURI(Uri.Builder uriBuilder);
    protected abstract void onResponseSuccess(JSONObject data);
    protected abstract void onResponseFailed(JSONObject data);

    protected JSONObject getRequestBody() {
        // should be implemented by the subclass if the endpoint expects a request body
        return null;
    }

    // AsyncTask will only return a single object
    private class APIResponse {

        public boolean success;
        public JSONObject data;

        public APIResponse(boolean success, JSONObject data) {
            this.success = success;
            this.data = data;
        }
    }

    // abstraction of logic for issuing HTTP request
    // http://developer.android.com/training/basics/network-ops/connecting.html
    protected class AsyncRequest extends AsyncTask<Uri, Void, APIResponse> {

        @Override
        protected APIResponse doInBackground(Uri... uris) {
            try {
                JSONObject payload = request(uris[0]);
                return new APIResponse(true, payload);

            } catch (APIException e) {
                Log.e(TAG, "error response from server");
                return new APIResponse(false, e.data);
            }
        }

        @Override
        protected void onPostExecute(APIResponse response) {
            String responseString = response.data == null ? "null" : response.data.toString();
            Log.i(TAG, "API response: " + responseString);
            if (response.success) {
                onResponseSuccess(response.data);
            } else {
                onResponseFailed(response.data);
            }
        }

        private final int READ_TIMEOUT = 10000; // milliseconds
        private final int WRITE_TIMEOUT = 15000; // milliseconds

        private JSONObject request(Uri uri) throws APIException {
            InputStream is;
            HttpURLConnection conn = null;
            try {

                URL url = new URL(uri.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(WRITE_TIMEOUT);
                conn.setDoInput(true);

                switch (mMethod) {
                    case GET:
                        conn.setRequestMethod("GET");
                        conn.connect();
                        break;

                    case DELETE:
                        conn.setRequestMethod("DELETE");
                        conn.connect();
                        break;

                    case POST:

                        JSONObject body = getRequestBody();
                        conn.setRequestMethod("POST");
                        if (body != null) {
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setDoOutput(true);
                        }
                        conn.connect();

                        if (body != null) {
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(body.toString());
                            os.flush();
                            os.close();
                        }
                        break;

                }

                is = conn.getInputStream();
                String response = readInputStream(is);
                return jsonDecode(response);

            } catch (IOException input_e) {
                // attempt to read the error response from the server (if there is one)
                JSONObject errorResponse = null;
                if (conn != null) {
                    is = conn.getErrorStream();
                    if (is != null) {
                        try {
                            String data = readInputStream(is);
                            errorResponse = jsonDecode(data);
                        } catch (IOException error_e) {
                            Log.e(TAG, "failed to read from error stream");
                        }
                    }
                }
                throw new APIException(errorResponse);
            }
        }

        private final int BYTE_ARRAY_BUFFER_SIZE = 50; /* initial buffer capacity */
        private final int BUFFER_SIZE = 512;
        private final String TEXT_ENCODING = "UTF-8";

        // Read the contents of an input stream to a String object.
        // http://stackoverflow.com/questions/2793168/reading-httpurlconnection-inputstream-manual-buffer-or-bufferedinputstream
        private String readInputStream(InputStream is) throws IOException {
            try {
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer bab = new ByteArrayBuffer(BYTE_ARRAY_BUFFER_SIZE);
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while (true) {
                    bytesRead = bis.read(buffer);
                    if (bytesRead == -1)
                        break;
                    bab.append(buffer, 0, bytesRead);
                }
                return new String(bab.toByteArray(), TEXT_ENCODING);
            } finally {
                // according to the docs any resources associated with the resource are also
                // released, which would suggest there's no need to explicitly close the
                // HttpURLConnection object:
                // http://developer.android.com/reference/java/io/BufferedInputStream.html#close()
                is.close();
            }
        }

        // All communication to and from the server is encoded as JSON.
        private JSONObject jsonDecode(String data) {
            try {
                return new JSONObject(data);
            } catch (JSONException e) {
                Log.e(TAG, "server response isn't valid JSON: " + data);
                return null;
            }
        }
    }
}