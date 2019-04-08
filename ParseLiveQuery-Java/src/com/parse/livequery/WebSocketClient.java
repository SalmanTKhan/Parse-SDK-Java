package com.parse.livequery;

public interface WebSocketClient {

    void open();

    void close();

    void send(String message);

    State getState();

    enum State {NONE, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED}

    interface WebSocketClientCallback {
        void onOpen();

        void onMessage(String message);

        void onClose();

        void onError(Throwable exception);

        void stateChanged();
    }

}
