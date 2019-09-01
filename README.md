# Amazfit-RE
Reverse engineering notes for the Amazfit watches (based on ingenic)

## General notes

* The amazfit pace is apparently called "HuangHe", and the stratos/stratos+ "Everest" ("EverestS" for the stratos +, also called "sports watch 2s"), and the verge "Qogir"
* Most of the logic is in com.huami.watch
* com.huami.watch.companion seems like the background service that send notifications and weather data
* The actual iwds init is made in com.huami.watch.connect.BaseConnection (used in PhoneConnection and WatchConnection)
* Apparently the iwds jar and jni communicate on a double client-server model (?)
* The amazfit app seems to work with these watches
* * A1602 pace chinese version
* * A1612 pace international version
* * A1609: stratos chinese version 
* * A1619: stratos international version
* * A1801: verge chinese version
* * A1811: verse international version
* * AC1807: verge 2 chinese version
* * AC1903: ???
* * AC1817: verge 2 international version
* * A1916: Health Watch (bip 2?!?)
* * (to fill: Amazfit Sports Watch // Amazfit Sports Watch 2 // Amazfit Sports Watch 2S // Amazfit Stratos // Amazfit Stratos + // Amazfit Smartwatch // Amazfit Verge)
* There is differences between "mainland" (chinese) and "oversea" (international) versions:
* There's references to "TestModeBackDoor" (?!?), TestMode, and FlavorDev
* There's some "features" (that seems to vary from model to model): "Alarm", "AmazonAlexa", "AssistantOTA", "AssistantFlight", "AssistantUpdates", "LvMi", "MIAI2_2", "MIAI_tts", "NfcDoor", "NfcUnionPay", "StepTargetSet", "WidgetManager2"

## Layers

* The base layer is the iwds protocol from ingenic, apparently not modified
* On top of this, data are sent "packed" (from com.huami.watch.hmwatchmanager.DataPackingManager:packing())
* The packing actually creates a CMyTransObj (from com.huami.watch.CMYTransObj)
* Apparently, it's based on android's os.bundle and os.parcel
* Not many things uses the ingenic libraries, they are:
* * com.huami.watch.companion.CompanionApplication (DeviceDescriptor)
* * com.huami.watch.connect.BaseConnection (DeviceDescriptor, IwdsApplication, app.ConnectionHelper, uniconnect.ConnectionServiceManager, uniconnect.link.Adapter, uniconnect.link.AdapterManager)
* * com.huami.watch.connect.Connectable (DeviceDescriptor)
* * com.huami.watch.connect.PhoneConnection (DeviceDescriptor, uniconnect.link.Adapter, uniconnect.link.Adapter.DeviceDiscoveryCallbacks, uniconnect.link.Link, uniconnect.link.RemoteDevice)
* * com.huami.watch.transport.DataTransportService (DeviceDescriptor, common.api.ServiceManagerContext, datatransactor.DataTransactor, datatransactor.DataTransactor.DataTransactResult, datatransactor.DataTransactor.DataTransactorCallback)
* * com.huami.watch.companion.agps.AGpsSyncHelper (datatransactor.DataTransactor.DataTransactResult)
* * com.huami.watch.companion.ble.lostwarning.LostWarningConstant (utils.serializable.UtilsConstants)
* * com.huami.watch.companion.ble.utils.BLEScanRecord (utils.serializable.UtilsConstants)
* * com.huami.watch.companion.components.bluetoothproxyserver.utils.HttpData (utils.serializable.UtilsConstants)
* * com.huami.watch.companion.otaphone.service.OtaModel (DeviceDescriptor, datatransactor.DataTransactor.DataTransactResult, datatransactor.FileInfo, datatransactor.FileTransactionmodel, datatransactor.FileTransactionmodel.FileTransactionInterruptCallback, datatransactor.FileTransactionmodel.FileTransactionModelCallback)
* * com.huami.watch.companion.sync.SyncWatchFaceBgHelper (datatransactor.DataTransactor.DataTransactResult)
* * com.huami.watch.companion.xiaomiai.FileTransfer (datatransactor.FileTransactionmodel, datatransactor.FileTransactionmodel.FileTransactionInterruptCallback, datatransactor.FileTransactionmodel.FileTransactionModelCallback)
* * com.huami.watch.companion.xiaomiai.FileTransferCallback (DeviceDescriptor, datatransactor.DataTransactor.DataTransactResult, datatransactor.FileInfo, datatransactor.FileTransactionModel, datatransactor.FileTransactionModel.FileTransactionModelCallback)
* * com.huami.watch.companion.xiaomiai.FileTransferInterruptCallback (datatransactor.FileTransactionmodel.FileTransactionInterruptCallback)
* * com.huami.watch.connect.WatchConnection (DeviceDescriptor, uniconnect.link.Adapter, uniconnect.link.Link)
* * com.huami.watch.dataflow.model.health.process.HeartRateDetailsInfo (utils.serializable.UtilsConstants)
* * com.huami.watch.hmwatchmanager.bt_connect.BGService_msg (DeviceDescriptor, datatransactor.DataTransactor, datatransactor.DataTransactor.DataTransactResult, datatransactor.DataTransactor.DataTransactorCallback
* * com.huami.watch.transport.FileTransporter (DeviceDescriptor, datatransactor.DataTransactor.DataTransactResult, datatransactor.FileInfo, datatransactor.FileTransactionModel, datatransactor.FileTransactionModel.FileTransactionModelCallback)
* * com.huami.watch.transport.httpsupport.transporter.file.MD5FileUtils (utils.serializable.UtilsConstants)
* * com.huami.watch.util.BytesUtul (utils.serializable.UtilsConstants)
* * com.xiaomi.account.utils.Base64Coder (utils.serializable.UtilsConstants)
* * com.xiaomi.ai.utils.WaveMaker (utils.serializable.UtilsConstants)
