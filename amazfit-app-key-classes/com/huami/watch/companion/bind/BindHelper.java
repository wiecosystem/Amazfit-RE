package com.huami.watch.companion.bind;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;
import com.huami.watch.companion.account.Account;
import com.huami.watch.companion.account.UserInfoManager;
import com.huami.watch.companion.agps.AGpsSyncHelper;
import com.huami.watch.companion.app.WatchWidgetNewManager;
import com.huami.watch.companion.bluetooth.BluetoothReceiver;
import com.huami.watch.companion.common.ASyncHelper;
import com.huami.watch.companion.device.Device;
import com.huami.watch.companion.device.DeviceManager;
import com.huami.watch.companion.device.DeviceUtil;
import com.huami.watch.companion.event.BluetoothPairingConfirmEvent;
import com.huami.watch.companion.event.BluetoothPairingUserConfirmedEvent;
import com.huami.watch.companion.event.ConnectedEvent;
import com.huami.watch.companion.event.DeviceBoundEvent;
import com.huami.watch.companion.initial.InitialState;
import com.huami.watch.companion.initial.InitialState.BindingState;
import com.huami.watch.companion.manager.UnitManager;
import com.huami.watch.companion.notification.NotificationManager;
import com.huami.watch.companion.sport.SportSortManager;
import com.huami.watch.companion.sync.SyncCenter;
import com.huami.watch.companion.sync.SyncDeviceInfoHelper;
import com.huami.watch.companion.sync.SyncResult;
import com.huami.watch.companion.sync.SyncUtil;
import com.huami.watch.companion.transport.CompanionModule;
import com.huami.watch.companion.usersettings.UserSettings;
import com.huami.watch.companion.usersettings.UserSettingsKeys;
import com.huami.watch.companion.usersettings.UserSettingsManager;
import com.huami.watch.companion.util.AnalyticsEvents;
import com.huami.watch.companion.util.Broadcaster;
import com.huami.watch.companion.util.KeyValueUtil;
import com.huami.watch.companion.watchface.WatchFaceManager;
import com.huami.watch.hmwatchmanager.R;
import com.huami.watch.hmwatchmanager.view.AlertDialogFragment;
import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.DataTransportResult;
import com.huami.watch.transport.TransportDataItem;
import com.huami.watch.transport.Transporter;
import com.huami.watch.transport.Transporter.ChannelListener;
import com.huami.watch.transport.Transporter.DataListener;
import com.huami.watch.transport.Transporter.DataSendResultCallback;
import com.huami.watch.util.Analytics;
import com.huami.watch.util.AndroidPermissions;
import com.huami.watch.util.BTUtil;
import com.huami.watch.util.Box;
import com.huami.watch.util.Log;
import com.huami.watch.util.Rx;
import com.huami.watch.util.RxBus;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class BindHelper extends ASyncHelper {
    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final int STATE_BT_PAIRING_CONFIRM = 5;
    public static final int STATE_BT_PAIRING_USER_CANCELED = 7;
    public static final int STATE_BT_PAIRING_USER_CONFIRMED = 6;
    public static final int STATE_BT_REQUEST_PAIRING = 4;
    public static final int STATE_CONNECTED = 9;
    public static final int STATE_CONNECTING = 8;
    public static final int STATE_FAIL_CANCEL = -2;
    public static final int STATE_FAIL_DISCONNECT = -4;
    public static final int STATE_FAIL_TIMEOUT = -3;
    public static final int STATE_FINISH = -7;
    public static final int STATE_PREPARE = -1;
    public static final int STATE_PREPARING = -10;
    public static final int STATE_RESTORE = 13;
    public static final int STATE_RESTORE_CONFIRM = 10;
    public static final int STATE_RESTORE_NO = 14;
    public static final int STATE_RESTORE_USER_CONFIRMED_NO = 12;
    public static final int STATE_RESTORE_USER_CONFIRMED_YES = 11;
    public static final int STATE_RETRY = -5;
    public static final int STATE_SCAN_QR_START = 2;
    public static final int STATE_SCAN_QR_SUCCESS = 3;
    public static final int STATE_START = 1;
    public static final int STATE_SUCCESS = -6;
    private static BindHelper a = null;
    /* access modifiers changed from: private */
    public static int b = 2;
    /* access modifiers changed from: private */
    public a c;
    private Handler d;
    /* access modifiers changed from: private */
    public boolean e;
    /* access modifiers changed from: private */
    public Transporter f;
    private DataListener g = new DataListener() {
        public void onDataReceived(TransportDataItem transportDataItem) {
            if (CompanionModule.ACTION_INITIAL_FINISH.equals(transportDataItem.getAction())) {
                BindHelper.this.a(-6);
            }
        }
    };
    private ChannelListener h = new ChannelListener() {
        public void onChannelChanged(boolean z) {
        }
    };
    private Runnable i = new Runnable() {
        public void run() {
            StringBuilder sb = new StringBuilder();
            sb.append("Check Bind Result : ");
            sb.append(BindHelper.this.e);
            sb.append(", LastState : ");
            sb.append((String) BindHelper.this.mStateDescriptions.get(BindHelper.this.mState));
            Log.d("BindHelper", sb.toString(), new Object[0]);
            if (!BindHelper.this.e) {
                if (BindHelper.this.mState == 14 || BindHelper.this.mState == 13) {
                    BindingState.fail(1);
                }
                BindHelper.this.notifyStateChanged(-3);
            }
        }
    };
    private Disposable j;

    static class a extends ASyncHelper {
        /* access modifiers changed from: private */
        public BindHelper a;
        /* access modifiers changed from: private */
        public AlertDialogFragment b;

        public String tag() {
            return "Bind-Helper-UI";
        }

        a(BindHelper bindHelper) {
            this.a = bindHelper;
        }

        /* access modifiers changed from: 0000 */
        public void a(Activity activity, int i) {
            switch (i) {
                case -6:
                    a((Context) activity, BindingState.isRestoring());
                    return;
                case -5:
                    a(activity, BindingState.isRestoring(), BindingState.failCode());
                    return;
                case -4:
                case -3:
                    b(activity, BindingState.isRestoring());
                    return;
                case -1:
                    a(activity);
                    return;
                case 2:
                    a((Context) activity);
                    return;
                case 4:
                    c(activity);
                    return;
                case 5:
                    d(activity);
                    return;
                case 7:
                    a();
                    return;
                case 8:
                    e(activity);
                    return;
                case 10:
                    f(activity);
                    return;
                case 11:
                    b((Context) activity);
                    return;
                case 12:
                    c((Context) activity);
                    return;
                default:
                    return;
            }
        }

        private void a(Activity activity) {
            Log.i("Bind-Helper-UI", "On Prepare!!", new Object[0]);
            if (!InitialState.environmentVerifyForBinding(activity)) {
                this.a.notifyStateChanged(-10);
            } else if (!AndroidPermissions.checkGranted((Context) activity, "android.permission.ACCESS_COARSE_LOCATION")) {
                b(activity);
                this.a.notifyStateChanged(-10);
            } else {
                this.a.notifyStateChanged(1);
                this.a.notifyStateChanged(2);
            }
        }

        @TargetApi(23)
        private static void b(final Activity activity) {
            Builder builder = new Builder(activity);
            builder.setTitle(activity.getString(R.string.permission_request_location));
            builder.setMessage(activity.getString(R.string.permission_request_location_tip));
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    AndroidPermissions.request(activity, "android.permission.ACCESS_COARSE_LOCATION", 1);
                }
            });
            builder.show();
        }

        private void a(Context context) {
            Log.i("Bind-Helper-UI", "OnScanQR Start...", new Object[0]);
            InitialState.toScan(context);
        }

        private void c(Activity activity) {
            Log.i("Bind-Helper-UI", "OnBT RequestPairing...", new Object[0]);
            g(activity);
            BluetoothReceiver.onBindRequestPairingStart();
        }

        private void d(Activity activity) {
            Log.i("Bind-Helper-UI", "OnBT PairingConfirm...", new Object[0]);
            g(activity);
        }

        private void a() {
            Log.i("Bind-Helper-UI", "OnBT Pairing UserCanceled!!", new Object[0]);
            b();
        }

        private void e(Activity activity) {
            Log.i("Bind-Helper-UI", "OnConnecting...", new Object[0]);
            g(activity);
        }

        private void f(Activity activity) {
            Log.i("Bind-Helper-UI", "OnRestore Confirm...", new Object[0]);
            b();
            String str = BindingState.sDevice;
            if (!TextUtils.isEmpty(str)) {
                a(activity, DeviceUtil.addressShort(str));
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("BindingDevice is Empty, Abort : ");
            sb.append(str);
            Log.w("Bind-Helper-UI", sb.toString(), new Object[0]);
        }

        private void b(Context context) {
            Log.i("Bind-Helper-UI", "OnRestore UserConfirmed Yes!!", new Object[0]);
            b();
            InitialState.toBindingTip(context, true);
        }

        private void c(Context context) {
            Log.i("Bind-Helper-UI", "OnRestore UserConfirmed No!!", new Object[0]);
            b();
            InitialState.toBindingTip(context, false);
        }

        private void g(Activity activity) {
            b();
            this.b = AlertDialogFragment.newInstance(2);
            this.b.setMessage(activity.getString(R.string.binding));
            this.b.setCancelable(false);
            FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
            beginTransaction.add(this.b, "Connecting...");
            beginTransaction.commitAllowingStateLoss();
        }

        private void a(Activity activity, boolean z, int i) {
            StringBuilder sb = new StringBuilder();
            sb.append("OnBind Fail, Rebind..., Restore : ");
            sb.append(z);
            sb.append(", FailCode : ");
            sb.append(i);
            Log.w("Bind-Helper-UI", sb.toString(), new Object[0]);
            b(activity, z, i);
        }

        private void a(Activity activity, String str) {
            b();
            this.b = AlertDialogFragment.newInstance(3);
            this.b.setTitle(activity.getString(R.string.restore_back_up));
            this.b.setMessage(activity.getString(R.string.restore_confirm_msg));
            this.b.setConfirm(activity.getString(R.string.restore_confirm), new View.OnClickListener() {
                public void onClick(View view) {
                    a.this.a.notifyStateChanged(11);
                }
            });
            this.b.setCancel(activity.getString(R.string.restore_no), new View.OnClickListener() {
                public void onClick(View view) {
                    a.this.a.notifyStateChanged(12);
                }
            });
            this.b.setCancelable(false);
            FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
            beginTransaction.add(this.b, "RestoreConfirm");
            beginTransaction.commitAllowingStateLoss();
        }

        private void b(final Activity activity, boolean z, int i) {
            b();
            this.b = AlertDialogFragment.newInstance(4);
            this.b.setTitle(activity.getString(R.string.restored_failed));
            if (z) {
                this.b.setMessage(activity.getString(R.string.restoring_but_disconnected));
            } else {
                this.b.setMessage(activity.getString(R.string.initialing_but_disconnected));
            }
            if (i == 1) {
                this.b.setMessage(activity.getString(R.string.bind_fail_network_disconnect));
            }
            this.b.setNeutral(activity.getString(R.string.initialing_restart), new View.OnClickListener() {
                public void onClick(View view) {
                    a.this.b.dismissAllowingStateLoss();
                    Analytics.event((Context) activity, AnalyticsEvents.EVENT_REBIND, 1);
                    Log.i("Bind-Helper-UI", "User Retry!!", new Object[0]);
                    a.this.a.a((Context) activity, true);
                }
            });
            this.b.setCancel(null, new View.OnClickListener() {
                public void onClick(View view) {
                    a.this.a.notifyStateChanged(-2);
                }
            });
            FragmentTransaction beginTransaction = activity.getFragmentManager().beginTransaction();
            beginTransaction.add(this.b, "Rebind");
            beginTransaction.commitAllowingStateLoss();
        }

        private void a(Context context, boolean z) {
            StringBuilder sb = new StringBuilder();
            sb.append("OnBind Success, Restore : ");
            sb.append(z);
            Log.i("Bind-Helper-UI", sb.toString(), new Object[0]);
            b();
            Toast.makeText(context, R.string.bind_success, 0).show();
        }

        private void b(Context context, boolean z) {
            StringBuilder sb = new StringBuilder();
            sb.append("OnBind Fail, Restore : ");
            sb.append(z);
            Log.i("Bind-Helper-UI", sb.toString(), new Object[0]);
            b();
            if (z) {
                Toast.makeText(context, R.string.bind_restore_fail, 0).show();
            } else {
                Toast.makeText(context, R.string.bind_fail, 0).show();
            }
            Analytics.event(context, AnalyticsEvents.EVENT_PAIR_FAILED, 1);
        }

        private void b() {
            AlertDialogFragment alertDialogFragment = this.b;
            if (alertDialogFragment != null) {
                try {
                    alertDialogFragment.dismissAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.b = null;
            }
        }

        public void release() {
            super.release();
            b();
        }
    }

    public String tag() {
        return "BindHelper";
    }

    static /* synthetic */ int b() {
        int i2 = b;
        b = i2 - 1;
        return i2;
    }

    private BindHelper() {
    }

    public static BindHelper getHelper() {
        if (a == null) {
            a = new BindHelper();
        }
        return a;
    }

    /* access modifiers changed from: private */
    public void a(final Context context, boolean z) {
        Log.i("BindHelper", "Start...", new Object[0]);
        if (this.mState > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Already Start, Ignore...");
            sb.append((String) this.mStateDescriptions.get(this.mState));
            Log.w("BindHelper", sb.toString(), new Object[0]);
            return;
        }
        release();
        if (context instanceof Activity) {
            a((Activity) context);
        }
        this.d = new Handler();
        this.f = Transporter.get(context, "com.huami.watch.companion");
        this.f.addDataListener(this.g);
        this.f.addChannelListener(this.h);
        this.mSubscription = RxBus.get().toObservable().subscribe((Consumer<? super T>) new Consumer<Object>() {
            public void accept(Object obj) {
                StringBuilder sb = new StringBuilder();
                sb.append("Event : ");
                sb.append(obj);
                Log.d("BindHelper", sb.toString(), new Object[0]);
                if (obj instanceof BluetoothPairingConfirmEvent) {
                    if (BindHelper.this.mState == 4 || BindHelper.this.mState == 5) {
                        BindHelper.this.notifyStateChanged(5);
                    }
                } else if (obj instanceof BluetoothPairingUserConfirmedEvent) {
                    if (BindHelper.this.mState == 5) {
                        boolean z = ((BluetoothPairingUserConfirmedEvent) obj).confirmed;
                        BindHelper.this.notifyStateChanged(z ? 6 : 7);
                        BindHelper.this.notifyStateChanged(z ? 8 : -2);
                    }
                } else if ((obj instanceof ConnectedEvent) && 1 <= BindHelper.this.mState && BindHelper.this.mState < 9) {
                    BindHelper.this.notifyStateChanged(9);
                }
            }
        });
        addCallback(new Consumer<Integer>() {
            /* renamed from: a */
            public void accept(Integer num) {
                try {
                    BindHelper.this.a(context, num.intValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        notifyStateChanged(-1);
    }

    public void start(Activity activity) {
        a((Context) activity, false);
    }

    private void a(final Activity activity) {
        String tag = tag();
        StringBuilder sb = new StringBuilder();
        sb.append("Init HelpUI : ");
        sb.append(activity);
        Log.d(tag, sb.toString(), new Object[0]);
        j();
        this.c = new a(this);
        this.c.addCallback(new Consumer<Integer>() {
            /* renamed from: a */
            public void accept(Integer num) {
                try {
                    BindHelper.this.c.a(activity, num.intValue());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(BindHelper.this.c.tag(), "OnStateChangedErr!!", e, new Object[0]);
                }
            }
        });
    }

    public static void onRequestLocationPermissionResult(int[] iArr) {
        if (iArr[0] == 0) {
            Log.d("BindHelper", "Coarse Location Permission Granted!!", new Object[0]);
            getHelper().a(1);
            getHelper().a(2);
            return;
        }
        Log.w("BindHelper", "Coarse Location Permission Not Granted!!", new Object[0]);
    }

    /* access modifiers changed from: private */
    public void a(Context context, int i2) {
        switch (i2) {
            case -7:
                g();
                return;
            case -6:
                n(context);
                return;
            case -4:
                q(context);
                return;
            case -3:
                p(context);
                return;
            case -2:
                o(context);
                return;
            case 1:
                a(context);
                return;
            case 3:
                b(context);
                return;
            case 4:
                c(context);
                return;
            case 5:
                c();
                return;
            case 6:
                d();
                return;
            case 7:
                e();
                return;
            case 8:
                f();
                return;
            case 9:
                d(context);
                return;
            case 11:
                e(context);
                return;
            case 12:
                f(context);
                return;
            case 13:
                g(context);
                return;
            case 14:
                k(context);
                return;
            default:
                return;
        }
    }

    private void a(Context context) {
        Log.i("BindHelper", "OnStart...", new Object[0]);
        BindingState.start();
        BindCallback.a(context);
    }

    private void b(Context context) {
        Log.i("BindHelper", "OnScan QR Success!!", new Object[0]);
        Analytics.event(context, AnalyticsEvents.EVENT_SCAN_QR_SUCCESS, 1);
        BindingState.scanQRSuccess();
        a(4);
    }

    private void c(Context context) {
        Log.i("BindHelper", "OnBT Pairing...", new Object[0]);
        h();
        Rx.io((Runnable) $$Lambda$5w3skXTz4t3XoWnNgXzlASenV1g.INSTANCE).delay(1, TimeUnit.SECONDS).subscribe((Action) new Action(context) {
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BindHelper.this.r(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void r(Context context) {
        BTUtil.cancelDiscovery();
        BindUtil.a(context);
        if (!BindUtil.c(context, BindingState.sDevice)) {
            a(-2);
        }
    }

    private void c() {
        Log.i("BindHelper", "OnBT Pairing Confirm...", new Object[0]);
        i();
    }

    private void d() {
        Log.i("BindHelper", "OnBT Pairing UserConfirmed!!", new Object[0]);
        BindingState.pairConfirmed();
    }

    private void e() {
        Log.i("BindHelper", "OnBT Pairing UserCanceled!!", new Object[0]);
        BindingState.pairCanceled();
    }

    private void f() {
        Log.i("BindHelper", "OnConnecting...", new Object[0]);
        h();
    }

    private void d(final Context context) {
        Log.i("BindHelper", "OnConnected!!", new Object[0]);
        i();
        BindingState.pairSuccess();
        Rx.io((Runnable) new Runnable() {
            public void run() {
                Log.d("BindHelper", "OnConnected, Start SyncDeviceInfo...", new Object[0]);
                String str = BindingState.sDevice;
                if (DeviceManager.getManager(context).find(str) != null) {
                    SyncDeviceInfoHelper.getHelper(context).address(str).start();
                }
            }
        }).subscribe((Action) new Action() {
            public void run() {
                BindHelper.this.notifyStateChanged(InitialState.isNewUser() ? 12 : 10);
            }
        });
        Analytics.event(context, AnalyticsEvents.EVENT_PAIR_SUCCESSFUL, 1);
    }

    private void e(Context context) {
        Log.i("BindHelper", "OnRestore UserConfirmed Yes!!", new Object[0]);
        BindingState.restore();
        Analytics.event(context, AnalyticsEvents.EVENT_CONFIRM_RESTORE, 1);
    }

    private void f(Context context) {
        Log.i("BindHelper", "OnRestore UserConfirmed No!!", new Object[0]);
        BindingState.notRestore();
        Analytics.event(context, AnalyticsEvents.EVENT_CANCEL_RESTORE, 1);
    }

    private void g(final Context context) {
        Log.i("BindHelper", "OnRestore Start...", new Object[0]);
        String str = BindingState.sDevice;
        InitialState.setUserSavedWatchFaceSyncedToWatch(false);
        SyncUtil.syncUserSavedSelectedWatchFace(context, DeviceManager.getManager(context).find(str));
        this.j = i(context).subscribe((Consumer<? super T>) new Consumer<SyncResult>() {
            /* renamed from: a */
            public void accept(SyncResult syncResult) {
                if (!syncResult.success) {
                    BindingState.fail(syncResult.failCode);
                    BindHelper.this.a(-4);
                    return;
                }
                AGpsSyncHelper.getHelper().syncAGpsToWatchWithEventObservable(context, 0, 30).subscribe((Consumer<? super T>) new Consumer<SyncResult>() {
                    /* renamed from: a */
                    public void accept(SyncResult syncResult) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Sync AGpsData Skip RemainRetry : ");
                        sb.append(BindHelper.b);
                        Log.d("BindHelper", sb.toString(), new Object[0]);
                        if (syncResult.success || BindHelper.b <= 0) {
                            BindHelper.this.j(context);
                            return;
                        }
                        BindingState.fail(syncResult.failCode);
                        BindHelper.b();
                        BindHelper.this.a(-4);
                    }
                });
            }
        });
        h();
    }

    /* access modifiers changed from: private */
    @NonNull
    public SyncResult h(Context context) {
        SyncResult syncResult = new SyncResult();
        String str = BindingState.sDevice;
        Device find = DeviceManager.getManager(context).find(str);
        if (find == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No BindingDevice : ");
            sb.append(str);
            Log.w("BindHelper", sb.toString(), new Object[0]);
            return syncResult;
        }
        boolean start = SyncDeviceInfoHelper.getHelper(context).address(str).start();
        if (start) {
            start = BindUtil.a(context, find);
            if (!start) {
                BindUtil.a(context, find.getCpuId());
                start = BindUtil.a(context, find);
            }
            if (!start) {
                syncResult.failCode = 1;
            }
        } else {
            syncResult.failCode = 2;
        }
        syncResult.finish = true;
        syncResult.success = start;
        if (start && find.info().isExperienceMode()) {
            Log.i("BindHelper", "ExperienceMode, Start Sync Health&Sport Data...", new Object[0]);
            SyncCenter.getCenter(context).types(3).transporter(this.f).start();
        }
        return syncResult;
    }

    private Observable<SyncResult> i(final Context context) {
        Log.d("BindHelper", "SyncDeviceInfoDelay!!", new Object[0]);
        return Rx.io((ObservableOnSubscribe<T>) new ObservableOnSubscribe<SyncResult>() {
            public void subscribe(ObservableEmitter<SyncResult> observableEmitter) {
                observableEmitter.onNext(BindHelper.this.h(context));
            }
        }).timer(2, TimeUnit.SECONDS).observable();
    }

    /* access modifiers changed from: private */
    public void j(final Context context) {
        Transporter transporter = this.f;
        if (transporter == null) {
            Log.w("BindHelper", "Transporter is NULL!!", new Object[0]);
        } else {
            transporter.send(CompanionModule.ACTION_SYNC_DATA_START, (DataSendResultCallback) new DataSendResultCallback() {
                public void onResultBack(DataTransportResult dataTransportResult) {
                    if (BindingState.isRestoring()) {
                        if (dataTransportResult.getResultCode() == 0) {
                            AnonymousClass1 r4 = new DataSendResultCallback() {
                                public void onResultBack(DataTransportResult dataTransportResult) {
                                    if (dataTransportResult.getResultCode() != 0) {
                                        BindHelper.this.a(-4);
                                    }
                                }
                            };
                            BindHelper bindHelper = BindHelper.this;
                            bindHelper.a(context, bindHelper.f, (DataSendResultCallback) r4);
                            Analytics.event(context, AnalyticsEvents.EVENT_BIND_START_RESTORE, 1);
                        } else {
                            BindHelper.this.a(-4);
                        }
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void a(Context context, Transporter transporter, DataSendResultCallback dataSendResultCallback) {
        DataBundle dataBundle = new DataBundle();
        dataBundle.putString(CompanionModule.KEY_UID, Account.getUID(context));
        dataBundle.putBoolean(CompanionModule.KEY_USER_OVERSEA, Account.isOversea(context));
        dataBundle.putString(CompanionModule.KEY_USER_MI_ID, Account.getMIID(context));
        dataBundle.putParcelable(CompanionModule.KEY_USER_INFO, UserInfoManager.get());
        dataBundle.putString(CompanionModule.KEY_EXTRA_INFO, m(context));
        dataBundle.putString(CompanionModule.KEY_USER_SETTINGS, KeyValueUtil.toJsonStr(UserSettingsManager.getManager(context).getAllWatch()));
        Device find = DeviceManager.getManager(context).find(BindingState.sDevice);
        if (find != null) {
            dataBundle.putString(CompanionModule.KEY_SPORT_ORDER, SportSortManager.getManager().toBindRestoreJson(context, find.info().getHuamiModel()));
        }
        transporter.send(CompanionModule.ACTION_SYNC_DATA, dataBundle, dataSendResultCallback);
    }

    private void k(final Context context) {
        Log.i("BindHelper", "OnNotRestore Start...", new Object[0]);
        this.j = i(context).subscribe((Consumer<? super T>) new Consumer<SyncResult>() {
            /* renamed from: a */
            public void accept(SyncResult syncResult) {
                if (!syncResult.success) {
                    BindingState.fail(syncResult.failCode);
                    BindHelper.this.a(-4);
                    return;
                }
                AGpsSyncHelper.getHelper().syncAGpsToWatchWithEventObservable(context, 0, 30).subscribe((Consumer<? super T>) new Consumer<SyncResult>() {
                    /* renamed from: a */
                    public void accept(SyncResult syncResult) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Sync AGpsData Skip RemainRetry : ");
                        sb.append(BindHelper.b);
                        Log.d("BindHelper", sb.toString(), new Object[0]);
                        if (syncResult.success || BindHelper.b <= 0) {
                            BindHelper.this.l(context);
                            return;
                        }
                        BindingState.fail(syncResult.failCode);
                        BindHelper.b();
                        BindHelper.this.a(-4);
                    }
                });
            }
        });
        h();
    }

    /* access modifiers changed from: private */
    public void l(final Context context) {
        if (this.f == null) {
            Log.w("BindHelper", "Transporter is NULL!!", new Object[0]);
            return;
        }
        DataBundle dataBundle = new DataBundle();
        dataBundle.putString(CompanionModule.KEY_UID, Account.getUID(context));
        dataBundle.putBoolean(CompanionModule.KEY_USER_OVERSEA, Account.isOversea(context));
        dataBundle.putString(CompanionModule.KEY_USER_MI_ID, Account.getMIID(context));
        dataBundle.putParcelable(CompanionModule.KEY_USER_INFO, UserInfoManager.get());
        dataBundle.putString(CompanionModule.KEY_EXTRA_INFO, m(context));
        this.f.send(CompanionModule.ACTION_INITIAL_FINISH, dataBundle, new DataSendResultCallback() {
            public void onResultBack(DataTransportResult dataTransportResult) {
                if (dataTransportResult.getResultCode() == 0) {
                    Device find = DeviceManager.getManager(context).find(BindingState.sDevice);
                    if (find != null) {
                        WatchFaceManager.getManager().resetDefaultWatchFace(context, find);
                        WatchWidgetNewManager.get().resetDefaultWidgetSequence(context, DeviceUtil.getHuamiModel(find));
                    }
                    String string = UserSettings.getString(context.getContentResolver(), UserSettingsKeys.KEY_WATCHFACE_SELECTED);
                    String string2 = UserSettings.getString(context.getContentResolver(), UserSettingsKeys.KEY_WIDGET);
                    UserSettingsManager.getManager(context).clearAll();
                    NotificationManager.init(context);
                    UnitManager.getInstance(context).init();
                    UserSettings.put(context.getContentResolver(), UserSettingsKeys.KEY_WATCHFACE_SELECTED, string);
                    UserSettings.put(context.getContentResolver(), UserSettingsKeys.KEY_WIDGET, string2);
                    BindHelper.this.a(-6);
                    return;
                }
                BindHelper.this.a(-4);
            }
        });
    }

    private String m(Context context) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("cmiitId", "2016DP4306");
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        return jSONObject.toString();
    }

    private void n(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("OnBind Success, InitialBind : ");
        sb.append(InitialState.isFirstBind());
        Log.i("BindHelper", sb.toString(), new Object[0]);
        this.e = true;
        i();
        InitialState.setNewUserTag(false);
        UserSettings.putBoolean(context.getContentResolver(), UserSettingsKeys.KEY_ACCOUNT_NEW_USER, false);
        BindingState.finish();
        if (InitialState.isFirstBind()) {
            InitialState.setInitialled(true);
        }
        String str = BindingState.sDevice;
        BindUtil.b(context, str);
        Broadcaster.sendLocalBroadcast(context, "com.huami.watch.companion.action.BindDeviceFinished");
        Analytics.event(context, AnalyticsEvents.EVENT_BIND_SUCCESSFUL, 1);
        a(-7);
        BindCallback.b(context);
        if (DeviceUtil.isModelEverest(DeviceManager.getManager(context).getCurrentDevice())) {
            Box.setShowEverestHelpDialog(true);
            Box.setShowEverestHelpCard(true);
        }
        RxBus.get().post(new DeviceBoundEvent(str));
    }

    private void o(Context context) {
        Log.w("BindHelper", "OnBind Fail : Cancel!!", new Object[0]);
        b(context, true);
    }

    private void p(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("OnBind Fail : Timeout, ");
        sb.append(BindingState.failCode());
        Log.w("BindHelper", sb.toString(), new Object[0]);
        Analytics.event(context, AnalyticsEvents.EVENT_BIND_TIME_OUT, 1);
        b(context, false);
    }

    private void q(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("OnBind Fail : Disconnected, ");
        sb.append(BindingState.failCode());
        Log.w("BindHelper", sb.toString(), new Object[0]);
        b(context, false);
    }

    private void b(Context context, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("OnBind Fail, Cancelled : ");
        sb.append(z);
        Log.w("BindHelper", sb.toString(), new Object[0]);
        this.e = false;
        i();
        BindUtil.unbindDeviceWithCloud(context, BindingState.sDevice);
        BindUtil.connectCurrentDevice(context);
        if (!z) {
            Broadcaster.sendLocalBroadcast(context, "com.huami.watch.companion.action.BindDeviceFailed");
            Analytics.event(context, AnalyticsEvents.EVENT_BIND_FAILED, 1);
            a(-5);
            return;
        }
        BindingState.cancel();
        a(-7);
    }

    private void g() {
        Log.i("BindHelper", "OnBind Finish!!", new Object[0]);
        i();
        BindingState.clear();
        k();
    }

    private void h() {
        if (this.d != null) {
            Log.d("BindHelper", "Post Check Bind Result Delay : 60000", new Object[0]);
            this.d.removeCallbacks(this.i);
            this.d.postDelayed(this.i, 60000);
        }
    }

    private void i() {
        if (this.d != null) {
            Log.d("BindHelper", "Remove Check Bind Result!!", new Object[0]);
            this.d.removeCallbacks(this.i);
        }
    }

    public void notifyStateChanged(int i2) {
        String tag = tag();
        StringBuilder sb = new StringBuilder();
        sb.append("NotifyStateChanged : ");
        sb.append(state(i2));
        Log.d(tag, sb.toString(), new Object[0]);
        super.notifyStateChanged(i2);
        a aVar = this.c;
        if (aVar != null) {
            aVar.notifyStateChanged(i2);
        }
    }

    public void notifyStateChangedUI() {
        notifyStateChangedUI(null);
    }

    public void notifyStateChangedUI(Consumer<Integer> consumer) {
        if (!isIdle()) {
            a aVar = this.c;
            if (aVar != null) {
                aVar.addCallback(consumer);
                this.c.notifyStateChanged(this.mState);
            }
        }
    }

    public void removeCallbackUI(Consumer<Integer> consumer) {
        a aVar = this.c;
        if (aVar != null) {
            aVar.removeCallback(consumer);
        }
    }

    public void release() {
        super.release();
        StringBuilder sb = new StringBuilder();
        sb.append("Release, LastState : ");
        sb.append((String) this.mStateDescriptions.get(this.mState));
        Log.i("BindHelper", sb.toString(), new Object[0]);
        j();
        Disposable disposable = this.j;
        if (disposable != null) {
            disposable.dispose();
            this.j = null;
        }
        Transporter transporter = this.f;
        if (transporter != null) {
            transporter.removeDataListener(this.g);
            this.f.removeChannelListener(this.h);
            this.f = null;
        }
        this.d = null;
        this.e = false;
        BindingState.sDevice = null;
    }

    private void j() {
        a aVar = this.c;
        if (aVar != null) {
            aVar.release();
            this.c = null;
        }
    }

    public void fillStateDescriptions(SparseArray<String> sparseArray) {
        sparseArray.put(1, "STATE_START");
        sparseArray.put(2, "STATE_SCAN_QR_START");
        sparseArray.put(3, "STATE_SCAN_QR_SUCCESS");
        sparseArray.put(4, "STATE_BT_REQUEST_PAIRING");
        sparseArray.put(5, "STATE_BT_PAIRING_CONFIRM");
        sparseArray.put(6, "STATE_BT_PAIRING_USER_CONFIRMED");
        sparseArray.put(7, "STATE_BT_PAIRING_USER_CANCELED");
        sparseArray.put(8, "STATE_CONNECTING");
        sparseArray.put(9, "STATE_CONNECTED");
        sparseArray.put(10, "STATE_RESTORE_CONFIRM");
        sparseArray.put(11, "STATE_RESTORE_USER_CONFIRMED_YES");
        sparseArray.put(12, "STATE_RESTORE_USER_CONFIRMED_NO");
        sparseArray.put(13, "STATE_RESTORE");
        sparseArray.put(14, "STATE_RESTORE_NO");
        sparseArray.put(-1, "STATE_PREPARE");
        sparseArray.put(-10, "STATE_PREPARING");
        sparseArray.put(-2, "STATE_FAIL_CANCEL");
        sparseArray.put(-3, "STATE_FAIL_TIMEOUT");
        sparseArray.put(-4, "STATE_FAIL_DISCONNECT");
        sparseArray.put(-5, "STATE_RETRY");
        sparseArray.put(-6, "STATE_SUCCESS");
        sparseArray.put(-7, "STATE_FINISH");
    }

    /* access modifiers changed from: private */
    public void a(final int i2) {
        if (this.d != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Post NotifyStateChanged: ");
            sb.append(state(i2));
            Log.d("BindHelper", sb.toString(), new Object[0]);
            this.d.post(new Runnable() {
                public void run() {
                    BindHelper.this.notifyStateChanged(i2);
                }
            });
        }
    }

    private void k() {
        if (this.d != null) {
            Log.d("BindHelper", "Post Release!!", new Object[0]);
            this.d.post(new Runnable() {
                public void run() {
                    BindHelper.this.release();
                }
            });
        }
    }
}
