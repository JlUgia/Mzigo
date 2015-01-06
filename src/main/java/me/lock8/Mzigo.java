/*
 * Mzigo.java
 *
 * Copyright (C) 2013 Lock8
 *
 * @author      Jose L Ugia - @Jl_Ugia
 * @version     1.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.lock8;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by joseluisugia on 20/12/14.
 */
public class Mzigo {

    private final OkHttpClient client;

    private final int duplicatedRequestPolicy;

    private static final int DUPLICATED_REQUEST_POLICY_CANCEL_ONGOING = 0;
    private static final int DUPLICATED_REQUEST_POLICY_WAIT_FOR_RESPONSE = 1;

    private LinkedList<ResponseListener> registeredListeners = new LinkedList<ResponseListener>();

    private final HashMap<String, Call> ongoingCalls = new HashMap<String, Call>();

    public Mzigo() {
        this(new OkHttpClient(), DUPLICATED_REQUEST_POLICY_WAIT_FOR_RESPONSE);
    }

    Mzigo(OkHttpClient client, int duplicatedRequestPolicy) {
        this.client = client;
        this.duplicatedRequestPolicy = duplicatedRequestPolicy;
    }

    public void register(final Callback responseCallback) {
        registeredListeners.add(new ResponseListener(responseCallback));
    }

    public void unregister(final Callback responseCallback) {
        registeredListeners.remove(responseListenerForCallback(responseCallback));
    }

    private ResponseListener responseListenerForCallback(final Callback responseCallback) {
        for (ResponseListener listener : registeredListeners) {
            if (listener.associatedCallback() == responseCallback) {
                return listener;
            }
        }

        throw new IllegalStateException("There is no listener registered for this call. Use mzigo.register(this).");
    }

    public void enqueue(final Request request, final Callback callback) {

        if (checkForDuplicatedRequest(request)) return;

        final Call call = client.newCall(request);
        journalCall(request, call, callback);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                notifyFailure(request, e);
                unJournalCall(request, callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                notifyResponse(response);
                unJournalCall(request, callback);

            }
        });
    }

    private void notifyFailure(Request request, IOException exception) {

        for (ResponseListener listener : registeredListeners) {
            listener.onFailure(request, exception);
        }
    }

    private void notifyResponse(Response response) throws IOException {

        for (ResponseListener listener : registeredListeners) {
            listener.onResponse(response);
        }
    }

    public void journalCall(Request request, Call call, Callback callback) {
        ongoingCalls.put(request.urlString(), call);
        responseListenerForCallback(callback).registerTag(request.tag());
    }

    public void unJournalCall(Request request, Callback callback) {
        ongoingCalls.remove(request.urlString());
        responseListenerForCallback(callback).unregisterTag(request.tag());
    }

    public Call ongoingCallForPath(String path) {
        return ongoingCalls.get(path);
    }

    private boolean checkForDuplicatedRequest(final Request request) {

        final Call ongoingCall;
        if (request.method().equalsIgnoreCase("GET")
                && (ongoingCall = ongoingCallForPath(request.urlString())) != null) {

            if (duplicatedRequestPolicy == DUPLICATED_REQUEST_POLICY_CANCEL_ONGOING) {
                ongoingCall.cancel();
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static class Builder {

        private OkHttpClient client;
        private int duplicatedRequestPolicy = DUPLICATED_REQUEST_POLICY_WAIT_FOR_RESPONSE;

        public Builder withClient(OkHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder duplicatedRequestPolicy(int duplicatedRequestPolicy) {
            this.duplicatedRequestPolicy = duplicatedRequestPolicy;
            return this;
        }

        public Mzigo build() {
            if (client == null) {
                client = new OkHttpClient();
            }
            return new Mzigo(client, duplicatedRequestPolicy);
        }
    }
}
