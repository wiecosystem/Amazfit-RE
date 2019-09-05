# Amazfit app init procedure
(taken from com.huami.watch.companion.service.AppInitService)

* First it gets the application context
* Then it gets it's debug flag, it it's in debug mode, it delete the "sd card log file"
* It deletes the "deprecated sport health log file" no matter in debug or not
* It init the "sync watch health helper", "sync watch sport helper" and "sync watch data helper"
* It init the "wear http support interface"
* It create a connection receiver
* It creates an intent filter on actions "com.huami.watch.WATCH_CONNED_4_COMPANION", "com.huami.watch.companion.action.UnbindDeviceStart", "com.huami.watch.httpsupport.COLLECT_DATA" (interistingly called "CALL_OF_DUTY"), and "com.huami.watch.companion.action.HOST_START_SYNC_DATA", and connects these intents to the ConnectionReceiver
* If the "music controller service" is enabled, it get an instance of "CommandHandler" (com.huami.watch.mediac.CommandHandler)
* It init the SmartHomeServer (mi home connector?)
* It init the "Watch Wifi FTP Util" (?!?)
* It init the "BLE Lost Warning Manager"
* b()
* It init the "File Downloader"
* It init the "Watch exchange", and "add sync" for "Event", "Alarm", "Update" and "Pin"
* It init the "Js Bridge Delegate"
* It create an intent filterr for "android.intent.action.TIME_SET" and "android.intent.action.TIMEZONE_CHANGED", connected to this.a
* It create another intent filter for "android.net.wifi.STATE_CHANGE" and "android.net.conn.CONNECTIVITY_CHANGE", connected to this.b
* It Start the DummyService ("startSchedule")
* It does "NLS.delayRefreshNLS()"
* It subscribe on the RxBus (that calls a(), it is also called in 500 ms also)
* It register the observer for SMS
* It register the receiver for Pin Notification
* It observes the updates (?) and does call b()

