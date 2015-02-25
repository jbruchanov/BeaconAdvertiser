package com.scurab.beaconadvertiser;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.ArrayRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by jiri.bruchanov on 09/12/2014.
 */
public class BeaconAdvertiserFragment extends Fragment {

    private EditText mUUID;
    private Spinner mMode;
    private Spinner mTX;
    private CheckBox mConnectable;
    private Button mButton;
    private boolean mIsAdvertising;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return inflater.inflate(R.layout.fragment_beacon_advertiser, container, false);
        } else {
            TextView tv = new TextView(getActivity());
            tv.setText(R.string.err_a5_needed);
            return tv;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mButton = (Button) view.findViewById(R.id.start_button);
            mUUID = (EditText) view.findViewById(R.id.uuid);
            mMode = (Spinner) view.findViewById(R.id.advertise_mode);
            mTX = (Spinner) view.findViewById(R.id.advertise_tx);
            mConnectable = (CheckBox) view.findViewById(R.id.connectable);
            bind(view);
        }
    }

    private void bind(View view) {
        view.findViewById(R.id.estimote_uuid).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { mUUID.setText(R.string.estimote_uuid_value); }
        });
        view.findViewById(R.id.random_uuid).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { mUUID.setText(UUID.randomUUID().toString().toUpperCase()); }
        });
        view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onStartButtonClick(); }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onStartButtonClick() {
        BluetoothAdapter btAdapter = getBTAdapter();
        if (btAdapter == null) {
            showMessage("BluetoothAdapter is null");
            return;
        }
        BluetoothLeAdvertiser adv = btAdapter.getBluetoothLeAdvertiser();
        if (adv == null) {
            showMessage("BluetoothLeAdvertiser is null\nYour device is not supported, get Nexus 6 or 9");
            return;
        }

        if (mIsAdvertising) {
            adv.stopAdvertising(new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    mIsAdvertising = false;
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    mIsAdvertising = false;
                }
            });
        }

        try {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData advertiseData = buildData();
            adv.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    showMessage("onStartAdvertisingSuccess\n" + settingsInEffect.toString());
                    mButton.setText(R.string.stop);
                    mIsAdvertising = true;
                }

                @Override
                public void onStartFailure(int errorCode) {
                    showMessage("onStartAdvertisingFailure errCode:" + errorCode);
                    mButton.setText(R.string.start);
                    mIsAdvertising = false;
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
            showMessage(t.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(getArrayInt(R.array.advertise_mode_values, mMode.getSelectedItemPosition()));
        builder.setTxPowerLevel(getArrayInt(R.array.advertise_tx_values, mTX.getSelectedItemPosition()));
        builder.setConnectable(mConnectable.isChecked());
        builder.setTimeout(0);
        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData buildData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        UUID uuid = UUID.fromString(mUUID.getText().toString());
        builder.addServiceUuid(new ParcelUuid(uuid));
        builder.setIncludeDeviceName(true);
        builder.setIncludeTxPowerLevel(true);
        builder.addManufacturerData(1, new byte[]{0, 1, 2, 3});
        //TODO:add data array
        //http://www.warski.org/blog/2014/01/how-ibeacons-work/
        return builder.build();
    }

    public BluetoothAdapter getBTAdapter() {
        return ((MainActivity) getActivity()).getBTAdapter();
    }

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private int getArrayInt(@ArrayRes int array, int position) {
        return getResources().getIntArray(array)[position];
    }
}
