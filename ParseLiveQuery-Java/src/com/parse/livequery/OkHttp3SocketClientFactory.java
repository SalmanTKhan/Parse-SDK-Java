package com.parse.livequery;

import android.util.Log;
import okhttp3.*;
import okio.ByteString;

import java.net.URI;
import java.util.Locale;

public class OkHttp3SocketClientFactory implements WebSocketClientFactory {

    OkHttpClient mClient;

    public OkHttp3SocketClientFactory(OkHttpClient client) {
        mClient = client;
    }

    public OkHttp3SocketClientFactory() {
        mClient = new OkHttpClient();
    }

    @Override
    public WebSocketClient createInstance(WebSocketClient.WebSocketClientCallback webSocketClientCallback, URI hostUrl) {
        return new OkHttp3WebSocketClient(mClient, webSocketClientCallback, hostUrl);
    }

    static class OkHttp3WebSocketClient implements WebSocketClient {

        private static final String LOG_TAG = "OkHttpWebSocketClient";

        private final WebSocketClientCallback webSocketClientCallback;
        private final OkHttpClient client;
        private final String url;
        private final int STATUS_CODE = 1000;
        private final String CLOSING_MSG = "User invoked close";
        private WebSocket webSocket;
        private volatile State state = State.NONE;
        private final WebSocketListener handler = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                setState(State.CONNECTED);
                webSocketClientCallback.onOpen();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                webSocketClientCallback.onMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.w(LOG_TAG, String.format(Locale.US,
                        "Socket got into inconsistent state and received %s instead.",
                        bytes.toString()));
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                setState(State.DISCONNECTED);
                webSocketClientCallback.onClose();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                webSocketClientCallback.onError(t);
            }
        };

        private OkHttp3WebSocketClient(OkHttpClient okHttpClient,
                                       WebSocketClientCallback webSocketClientCallback, URI hostUrl) {
            client = okHttpClient;
            this.webSocketClientCallback = webSocketClientCallback;
            url = hostUrl.toString();
        }

        @Override
        public synchronized void open() {
            if (State.NONE == state) {
                // OkHttp3 connects as soon as the socket is created so do it here.
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                webSocket = client.newWebSocket(request, handler);
                setState(State.CONNECTING);
            }
        }

        @Override
        public synchronized void close() {
            if (State.NONE != state) {
                setState(State.DISCONNECTING);
                webSocket.close(STATUS_CODE, CLOSING_MSG);
            }
        }

        @Override
        public synchronized void send(String message) {
            if (state == State.CONNECTED) {
                webSocket.send(message);
            }
        }

        @Override
        public State getState() {
            return state;
        }

        private void setState(State newState) {
            synchronized (this) {
                this.state = newState;
            }
            this.webSocketClientCallback.stateChanged();
        }
    }

}
