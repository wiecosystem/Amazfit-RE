# Watch init

## Mac address and pairing

* The mac address you may get from the watch from simple bluetooth scanning doesn't cut it, you need the "hidden" (or "real" mac address from the QR code (see qrcode.md)
* You can bond using com.huami.iwds.uniconnect.Link.bondAddress() using the mac address you've obtained with the QR code, somehow, it's triggering the watch to do a pairing request to the phone
* The watch and phone will ask for the same pincode, this seems more because of how bluetooth works than something really even wanted by the watch
* After pairing, the watch needs some informations (see next chapter)

## Sync of first time informations

* Firstly it gets a "transporter" with the context, and "com.huami.watch.companion"
* Apparently, there's two "modes", based on if the app/account was previously tied to the watch or not, they are called "OnRestore" and "OnNotRestore"
* "OnRestore" seems to only sync the watchface configuration and the AGPS data
* There's two "modes", depending if the bind was set with OnRestore or OnNotRestore
* In the OnNotRestore case (first time binding this watch), it calls `ACTION_INITIAL_FINISH` ("com.huami.watch.companion.transport.InitialFinish") with the init data
* If not, it will first send `ACTION_SYNC_DATA_START` ("com.huami.watch.companion.transport.SyncDataStart"), and then `ACTION_SYNC_DATA` ("com.huami.watch.companion.transport.SyndData") with the data
* So, You have either `ACTION_SYNC_START` then `ACTION_SYNC_DATA` when you have OnRestore, or `ACTION_INITIAL_FINISH` when OnNotRestore, so it should be possible to init the watch with only the second one

### Init data

* * UID (string) 
* * USER_OVERSEA (boolean, i guess it would enable NFC and the like)
* * USER_MI_ID (string, note, the watch apparently can't check it given that at this point, it doesn't have wifi yet)
* * USER_INFO (parcelable)
* * EXTRA_INFO (json string, only contains `{"cmiitId"=2016DP4306"}`)
* * USER_SETTINGS (json string)
* * if "find", SPORT_ORDER (json string)
* UID and MI_ID aren't the same, but both come from the same class, `Account` 

