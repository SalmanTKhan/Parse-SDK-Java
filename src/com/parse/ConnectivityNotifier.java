/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ConnectivityNotifier {
    private static final String TAG = "com.parse.ConnectivityNotifier";
    private static final ConnectivityNotifier singleton = new ConnectivityNotifier();
    private final Object lock = new Object();
    private Set<ConnectivityListener> listeners = new HashSet<>();
    private boolean hasRegisteredReceiver = false;

    public static ConnectivityNotifier getNotifier() {
        singleton.tryToRegisterForNetworkStatusNotifications();
        return singleton;
    }

    public static boolean isConnected() {
        return true;
    }

    public void addListener(ConnectivityListener delegate) {
        synchronized (lock) {
            listeners.add(delegate);
        }
    }

    public void removeListener(ConnectivityListener delegate) {
        synchronized (lock) {
            listeners.remove(delegate);
        }
    }

    private boolean tryToRegisterForNetworkStatusNotifications() {
        synchronized (lock) {
            if (hasRegisteredReceiver) {
                return true;
            }

            hasRegisteredReceiver = true;
            return true;
        }
    }

    public void onReceive() {
        List<ConnectivityListener> listenersCopy;
        synchronized (lock) {
            listenersCopy = new ArrayList<>(listeners);
        }
        for (ConnectivityListener delegate : listenersCopy) {
            delegate.networkConnectivityStatusChanged();
        }
    }

    public interface ConnectivityListener {
        void networkConnectivityStatusChanged();
    }
}
