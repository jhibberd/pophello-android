package com.example.pophello.app.model.server;

import org.json.JSONObject;

// An exception that is raised by the server in response to a request. The data field contains the
// JSON response from the server detailing the error (or null if non is available).
class APIException extends java.lang.Exception {

    public final JSONObject data;

    public APIException(JSONObject data) {
        this.data = data;
    }

}