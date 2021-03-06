/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uiserver.internal.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.api.exception.UiServerRuntimeException;
import org.wso2.carbon.uiserver.api.http.HttpRequest;
import org.wso2.carbon.uiserver.api.http.HttpResponse;
import org.wso2.carbon.uiserver.internal.io.http.StaticRequestDispatcher;

/**
 * Dispatches HTTP requests.
 *
 * @since 0.8.0
 */
public class RequestDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);

    private final PageRequestDispatcher pageRequestDispatcher;
    private final StaticRequestDispatcher staticRequestDispatcher;

    /**
     * Creates a new request dispatcher.
     *
     * @param app web app to be served
     */
    public RequestDispatcher(App app) {
        this(new PageRequestDispatcher(app), new StaticRequestDispatcher(app));
    }

    RequestDispatcher(PageRequestDispatcher pageRequestDispatcher, StaticRequestDispatcher staticRequestDispatcher) {
        this.pageRequestDispatcher = pageRequestDispatcher;
        this.staticRequestDispatcher = staticRequestDispatcher;
    }

    /**
     * Serves the specified HTTP request.
     *
     * @param request HTTP request to be served
     * @return HTTP response
     */
    public HttpResponse serve(HttpRequest request) {
        if (!request.isValid()) {
            return ResponseBuilder.badRequest("URI '" + request.getUri() + "' is invalid.").build();
        }

        try {
            if (request.isDefaultFaviconRequest()) {
                return staticRequestDispatcher.serveDefaultFavicon(request);
            } else if (request.isStaticResourceRequest()) {
                return staticRequestDispatcher.serve(request);
            } else {
                return pageRequestDispatcher.serve(request);
            }
        } catch (UiServerRuntimeException e) {
            LOGGER.error("An error occurred when serving for request '{}'.", request, e);
            return ResponseBuilder.serverError("A server occurred while serving for request.").build();
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred when serving for request '{}'.", request, e);
            return ResponseBuilder.serverError("An unexpected server occurred while serving for request.").build();
        }
    }
}
