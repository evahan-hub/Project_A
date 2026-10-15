package io.artly.data

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

class IosConnectivity : Connectivity {
    override val networkAvailable: Flow<Unit> = callbackFlow {
        val monitor = nw_path_monitor_create()
        // Called with the current path right away, and whenever it changes.
        nw_path_monitor_set_update_handler(monitor) { path ->
            if (nw_path_get_status(path) == nw_path_status_satisfied) trySend(Unit)
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}
