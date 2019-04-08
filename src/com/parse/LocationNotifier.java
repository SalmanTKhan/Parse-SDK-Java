/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import android.content.Context;
import android.location.Criteria;
import bolts.Capture;
import bolts.Task;
import bolts.TaskCompletionSource;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * LocationNotifier is a wrapper around fetching the current device's location. It looks for the GPS
 * and Network LocationProviders by default (printStackTrace()'ing if, for example, the app doesn't
 * have the correct permissions in its AndroidManifest.xml). This class is intended to be used for a
 * <i>single</i> location update.
 * <p>
 * When testing, if a fakeLocation is provided (via setFakeLocation()), we don't wait for the
 * LocationManager to fire or for the timer to run out; instead, we build a local LocationListener,
 * then call the onLocationChanged() method manually.
 */
class LocationNotifier {
    private static Location fakeLocation = null;

    /**
     * Asynchronously gets the current location of the device.
     * <p>
     * This will request location updates from the best provider that match the given criteria
     * and return the first location received.
     * <p>
     * You can customize the criteria to meet your specific needs.
     * * For higher accuracy, you can set {@link Criteria#setAccuracy(int)}, however result in longer
     * times for a fix.
     * * For better battery efficiency and faster location fixes, you can set
     * {@link Criteria#setPowerRequirement(int)}, however, this will result in lower accuracy.
     *
     * @param context  The context used to request location updates.
     * @param timeout  The number of milliseconds to allow before timing out.
     * @param criteria The application criteria for selecting a location provider.
     */
    /* package */
    static Task<Location> getCurrentLocationAsync(Context context,
                                                  long timeout, Criteria criteria) {
        final TaskCompletionSource<Location> tcs = new TaskCompletionSource<>();
        final Capture<ScheduledFuture<?>> timeoutFuture = new Capture<>();

        timeoutFuture.set(ParseExecutors.scheduled().schedule(new Runnable() {
            @Override
            public void run() {
                tcs.trySetError(new ParseException(ParseException.TIMEOUT, "Location fetch timed out."));
            }
        }, timeout, TimeUnit.MILLISECONDS));

        if (fakeLocation != null) {
            //listener.onLocationChanged(fakeLocation);
        }

        return tcs.getTask();
    }

    /**
     * Helper method for testing.
     */
    /* package */
    static void setFakeLocation(Location location) {
        fakeLocation = location;
    }
}
