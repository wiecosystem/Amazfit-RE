# Amazfit app services (\* (service name) // on (intents))

* com.huami.watch.transport.DataTransportService // on com.huami.watch.transport.DataTransportService
* com.huami.watch.companion.notification.NotificationAccessService // android.service.notification.NotificationListenerService
* com.huami.watch.companion.notification.NotificationAccessService_Api18 // on android.service.notification.NotificationListenerService
* com.huami.watch.companion.agps.AGpsSyncService
* com.huami.watch.companion.weather.WeatherService
* com.huami.watch.companion.service.DummyService
* com.huami.watch.companion.service.DummyJobService
* com.huami.watch.companion.service.CompanionService // on com.huami.watch.companion.action.COMPANION_SERVICE
* com.huami.watch.companion.otaphone.service.OtaService // on com.huami.watch.otaphone.service.OtaService
* com.huami.watch.companion.IME.IMEservice // on com.huami.watch.otaphone.service.OtaService
* com.huami.watch.companion.service.AppInitService
* com.huami.watch.hmwatchmanager.bt_connect.BGService_msg // cn.com.huami_service.action

# Receiver
* com.huami.watch.companion.bluetooth.BluetoothReceiver // on android.bluetooth.device.action.PAIRING_REQUEST, android.bluetooth.device.action.PAIRING_CANCEL, android.bluetooth.device.action.BOND_STATE_CHANGED
