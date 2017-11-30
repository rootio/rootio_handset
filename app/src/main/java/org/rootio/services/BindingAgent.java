package org.rootio.services;

import android.app.Service;
import android.os.Binder;

/**
 * This class provides binding functionality to services
 *
 * @author Jude Mukundane
 */
public class BindingAgent extends Binder {

    private Service service;

    BindingAgent(Service service) {
        this.service = service;
    }

    /**
     * Returns the service for which this class is providing a binding
     * connection
     *
     * @return Service object for the service bound to
     */
    Service getService() {
        return this.service;
    }
}
