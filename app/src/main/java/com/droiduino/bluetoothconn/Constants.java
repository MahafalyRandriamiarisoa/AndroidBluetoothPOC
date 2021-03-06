package com.droiduino.bluetoothconn;

public interface Constants {
    /**
     * Defines several constants used between {@link Data} and the UI.
     */
    // Message types sent from the Data Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the Data Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";


}
