package org.rootio.services.synchronization;

import org.json.JSONObject;

public interface SynchronizationHandler {

    /**
     * Returns JSON data containing records to be synced to the cloud server
     *
     * @return String containing JSON data
     */
    JSONObject getSynchronizationData();

    /**
     * Processes the response returned from the server upon synchronization
     *
     * @param synchronizationResponse The JSON response returned by the server in response to the
     *                                synchronization operation
     */
    void processJSONResponse(JSONObject synchronizationResponse);

    /**
     * Constructs the URL to check for EventTime updates
     *
     * @return
     */
    String getSynchronizationURL();
}
