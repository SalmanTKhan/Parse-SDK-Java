/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import com.parse.http.ParseHttpRequest;
import org.json.JSONObject;

import java.net.URI;

class ParseRESTObjectCommand extends ParseRESTCommand {

    public ParseRESTObjectCommand(
            String httpPath,
            ParseHttpRequest.Method httpMethod,
            JSONObject parameters,
            String sessionToken) {
        super(httpPath, httpMethod, parameters, sessionToken);
    }

    public static ParseRESTObjectCommand getObjectCommand(String objectId, String className,
                                                          String sessionToken) {
        String httpPath = String.format("classes/%s/%s", URI.create(className), URI.create(objectId));
        return new ParseRESTObjectCommand(httpPath, ParseHttpRequest.Method.GET, null, sessionToken);
    }

    public static ParseRESTObjectCommand saveObjectCommand(
            ParseObject.State state, JSONObject operations, String sessionToken) {
        if (state.objectId() == null) {
            return ParseRESTObjectCommand.createObjectCommand(
                    state.className(),
                    operations,
                    sessionToken);
        } else {
            return ParseRESTObjectCommand.updateObjectCommand(
                    state.objectId(),
                    state.className(),
                    operations,
                    sessionToken);
        }
    }

    private static ParseRESTObjectCommand createObjectCommand(String className, JSONObject changes,
                                                              String sessionToken) {
        String httpPath = String.format("classes/%s", URI.create(className));
        return new ParseRESTObjectCommand(httpPath, ParseHttpRequest.Method.POST, changes, sessionToken);
    }

    private static ParseRESTObjectCommand updateObjectCommand(String objectId, String className,
                                                              JSONObject changes, String sessionToken) {
        String httpPath = String.format("classes/%s/%s", URI.create(className), URI.create(objectId));
        return new ParseRESTObjectCommand(httpPath, ParseHttpRequest.Method.PUT, changes, sessionToken);
    }

    public static ParseRESTObjectCommand deleteObjectCommand(
            ParseObject.State state, String sessionToken) {
        String httpPath = String.format("classes/%s", URI.create(state.className()));
        String objectId = state.objectId();
        if (objectId != null) {
            httpPath += String.format("/%s", URI.create(objectId));
        }
        return new ParseRESTObjectCommand(httpPath, ParseHttpRequest.Method.DELETE, null, sessionToken);
    }
}
