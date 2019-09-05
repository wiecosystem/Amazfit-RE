package com.huami.watch.companion.bind;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.huami.watch.companion.CompanionApplication;
import com.huami.watch.companion.cloud.api.DeviceBindAPI;
import com.huami.watch.companion.device.Device;
import com.huami.watch.companion.device.DeviceManager;
import com.huami.watch.companion.device.DeviceUtil;
import com.huami.watch.companion.event.DeviceUnboundEvent;
import com.huami.watch.companion.initial.InitialState.BindingState;
import com.huami.watch.companion.usersettings.UserSettings;
import com.huami.watch.companion.usersettings.UserSettingsKeys;
import com.huami.watch.hmwatchmanager.R;
import com.huami.watch.ui.dialog.AlertDialog;
import com.huami.watch.util.BTUtil;
import com.huami.watch.util.Log;
import com.huami.watch.util.Rx;
import com.huami.watch.util.RxBus;
import java.util.ArrayList;
import java.util.List;

public class BindUtil {
    private static boolean a(final Context context, String str, boolean z) {
        boolean z2 = true;
        if (TextUtils.isEmpty(str)) {
            StringBuilder sb = new StringBuilder();
            sb.append("UnbindDeviceWithCloud is Empty, Abort : ");
            sb.append(str);
            Log.d("Bind-Util", sb.toString(), new Object[0]);
            return true;
        }
        Device find = DeviceManager.getManager(context).find(str);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("UnbindDeviceWithCloud : ");
        sb2.append(str);
        sb2.append(", ");
        sb2.append(find);
        sb2.append(", Async : ");
        sb2.append(z);
        Log.d("Bind-Util", sb2.toString(), new Object[0]);
        final String str2 = null;
        if (find != null) {
            str2 = find.getCpuId();
        }
        d(context, str);
        if (TextUtils.isEmpty(str2)) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("UnbindDeviceWithCloud : DeviceId is Empty, ");
            sb3.append(str2);
            Log.w("Bind-Util", sb3.toString(), new Object[0]);
            return true;
        }
        if (!z) {
            z2 = a(context, str2);
        } else {
            Rx.io((Runnable) new Runnable() {
                public void run() {
                    BindUtil.a(context, str2);
                }
            }).safeSubscribe();
        }
        return z2;
    }

    public static boolean unbindDeviceWithCloud(Context context, String str) {
        return a(context, str, true);
    }

    static boolean a(Context context, Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sync device bound to Cloud : ");
        sb.append(device);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (device == null) {
            Log.w("Bind-Util", "No bound device!!", new Object[0]);
            return false;
        }
        boolean postDeviceBindInfo = DeviceBindAPI.postDeviceBindInfo(context, device);
        if (postDeviceBindInfo) {
            device.setSyncedToCloud(true);
            DeviceManager.getManager(context).save(device);
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Sync device to Cloud : ");
        sb2.append(device.address());
        sb2.append(", success : ");
        sb2.append(postDeviceBindInfo);
        Log.d("Bind-Util", sb2.toString(), new Object[0]);
        return postDeviceBindInfo;
    }

    static boolean b(Context context, Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sync DeviceUnbound to Cloud : ");
        sb.append(device);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (device == null || a(context, device.getCpuId())) {
            return true;
        }
        return false;
    }

    static boolean a(Context context, String str) {
        boolean deleteDeviceBindInfo = !TextUtils.isEmpty(str) ? DeviceBindAPI.deleteDeviceBindInfo(context, str) : true;
        StringBuilder sb = new StringBuilder();
        sb.append("Sync DeviceUnbound to Cloud : ");
        sb.append(str);
        sb.append(", ");
        sb.append(deleteDeviceBindInfo);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        return deleteDeviceBindInfo;
    }

    public static void unbindUnfinishedDevice(Context context) {
        String savedBindingDevice = BindingState.getSavedBindingDevice();
        if (!TextUtils.isEmpty(savedBindingDevice)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unbind UnfinishedDevice : ");
            sb.append(savedBindingDevice);
            Log.w("Bind-Util", sb.toString(), new Object[0]);
            BindingState.clear();
            unbindDeviceWithCloud(context, savedBindingDevice);
        }
    }

    public static void unbindCurrentDevice(Context context) {
        Device currentDevice = DeviceManager.getManager(context).getCurrentDevice();
        StringBuilder sb = new StringBuilder();
        sb.append("Unbind CurrentDevice : ");
        sb.append(currentDevice);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (currentDevice != null) {
            d(context, currentDevice.address());
        }
    }

    private static void b(Context context) {
        List all = DeviceManager.getManager(context).getAll();
        if (all.size() > 0) {
            switchMainDevice(context, ((Device) all.get(0)).address());
        }
    }

    public static void switchMainDevice(Context context, String str) {
        Device find = DeviceManager.getManager(context).find(str);
        StringBuilder sb = new StringBuilder();
        sb.append("Switch MainDevice To : ");
        sb.append(str);
        sb.append(", ");
        sb.append(find);
        Log.i("Bind-Util", sb.toString(), new Object[0]);
        if (!TextUtils.isEmpty(str) && find != null) {
            Device currentDevice = DeviceManager.getManager(context).getCurrentDevice();
            if (currentDevice != null && currentDevice.address().equals(str)) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Switch MainDevice, Already is MainDevice : ");
                sb2.append(currentDevice);
                Log.w("Bind-Util", sb2.toString(), new Object[0]);
            }
            a(context);
            c(context, find);
            d(context, find);
            BindCallback.b(context);
        }
    }

    public static void activeLastMainDevice(Context context) {
        String str = UserSettings.get(context.getContentResolver(), UserSettingsKeys.KEY_DEVICE_MAIN);
        StringBuilder sb = new StringBuilder();
        sb.append("Active Last MainDevice : ");
        sb.append(str);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        DeviceManager manager = DeviceManager.getManager(context);
        Device findByDeviceId = !TextUtils.isEmpty(str) ? manager.findByDeviceId(str) : null;
        if (findByDeviceId != null) {
            c(context, findByDeviceId);
            return;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Find no Device : ");
        sb2.append(str);
        sb2.append(", to Active any one!!");
        Log.w("Bind-Util", sb2.toString(), new Object[0]);
        List all = manager.getAll();
        if (all.size() > 0) {
            c(context, (Device) all.get(0));
        }
    }

    static void b(Context context, String str) {
        Device find = DeviceManager.getManager(context).find(str);
        if (find == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Find no Device : ");
            sb.append(str);
            sb.append(", abort!!");
            Log.w("Bind-Util", sb.toString(), new Object[0]);
            return;
        }
        c(context, find);
    }

    private static void c(Context context, @NonNull Device device) {
        DeviceManager manager = DeviceManager.getManager(context);
        Device currentDevice = manager.getCurrentDevice();
        if (currentDevice != null) {
            manager.inactive(currentDevice);
        }
        manager.active(device);
        UserSettings.putString(context.getContentResolver(), UserSettingsKeys.KEY_DEVICE_MAIN, device.getCpuId());
    }

    private static boolean d(Context context, @NonNull Device device) {
        return !DeviceUtil.isLastBoundByIOS(device) && c(context, device.address());
    }

    static boolean c(Context context, String str) {
        boolean bind = !TextUtils.isEmpty(str) ? CompanionApplication.get().mConnection.bind(str) : false;
        StringBuilder sb = new StringBuilder();
        sb.append("Connect Device : ");
        sb.append(str);
        sb.append(", Success : ");
        sb.append(bind);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        return bind;
    }

    public static void connectCurrentDevice(Context context) {
        Device currentDevice = DeviceManager.getManager(context).getCurrentDevice();
        StringBuilder sb = new StringBuilder();
        sb.append("Connect CurrentDevice : ");
        sb.append(currentDevice);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (currentDevice != null) {
            d(context, currentDevice);
        }
    }

    public static void disconnectAllDevices(Context context) {
        ArrayList<Device> arrayList = new ArrayList<>(DeviceManager.getManager(context).getAll());
        StringBuilder sb = new StringBuilder();
        sb.append("Disconnect AllDevices : ");
        sb.append(arrayList.size());
        sb.append(", ");
        sb.append(arrayList);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (arrayList.size() > 0) {
            for (Device address : arrayList) {
                disconnectDevice(context, address.address());
            }
        }
    }

    static void a(Context context) {
        Device currentDevice = DeviceManager.getManager(context).getCurrentDevice();
        StringBuilder sb = new StringBuilder();
        sb.append("Disconnect CurrentDevice : ");
        sb.append(currentDevice);
        Log.i("Bind-Util", sb.toString(), new Object[0]);
        if (currentDevice != null) {
            disconnectDevice(context, currentDevice.address());
        }
    }

    public static void disconnectDevice(Context context, String str) {
        disconnectDevice(context, str, false);
    }

    public static void disconnectDevice(Context context, String str, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Disconnect Device : ");
        sb.append(str);
        sb.append(", RemoveSystemPairing : ");
        sb.append(z);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        if (!TextUtils.isEmpty(str)) {
            CompanionApplication.get().mConnection.unbind(str);
            if (z) {
                BTUtil.removeBond(str);
            }
        }
    }

    static void d(Context context, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unbind Device : ");
        sb.append(str);
        Log.d("Bind-Util", sb.toString(), new Object[0]);
        a(context, str, true, true);
        if (DeviceManager.getManager(context).getCurrentDevice() == null) {
            b(context);
        }
    }

    private static void a(Context context, String str, boolean z, boolean z2) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unbind Device via : ");
        sb.append(str);
        sb.append(", RemoveSavedDevice : ");
        sb.append(z);
        sb.append(", RemoveSystemPairing : ");
        sb.append(z2);
        Log.i("Bind-Util", sb.toString(), new Object[0]);
        if (!TextUtils.isEmpty(str)) {
            Log.i("Bind-Util", "Unbind Device 1, Disconnect...", new Object[0]);
            disconnectDevice(context, str, z2);
            if (z) {
                DeviceManager manager = DeviceManager.getManager(context);
                Device find = manager.find(str);
                if (find != null) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Unbind Device 2, Remove : ");
                    sb2.append(find);
                    Log.i("Bind-Util", sb2.toString(), new Object[0]);
                    String str2 = DeviceUtil.UNKNOWN_DID;
                    String address = find.address();
                    boolean isActive = find.isActive();
                    manager.remove(find);
                    RxBus.get().post(new DeviceUnboundEvent(str2, address, isActive));
                }
            }
        }
    }

    public static void showDeviceResetTipDialog(Activity activity) {
        try {
            activity.getFragmentManager().beginTransaction().add(AlertDialog.setMessage(activity.getString(R.string.bt_pairing_device_reset_tip)).setNeutralBtn(activity.getString(R.string.btn_known), null).setCancelable(false).build(), "DeviceResetTip").commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
