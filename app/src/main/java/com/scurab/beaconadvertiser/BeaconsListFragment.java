package com.scurab.beaconadvertiser;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easibeacon.protocol.IBeacon;
import com.easibeacon.protocol.IBeaconListener;
import com.easibeacon.protocol.IBeaconProtocol;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by jiri.bruchanov on 09/12/2014.
 */
public class BeaconsListFragment extends Fragment implements IBeaconListener {

    private ArrayAdapter<IBeacon> mAdapter;
    private IBeaconProtocol mIBeaconProto;
    private TextView mLastEvent;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm.ss");
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beacons_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.scan_beacons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartScan();
            }
        });
        mLastEvent = (TextView) view.findViewById(R.id.last_event_value);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mIBeaconProto = IBeaconProtocol.getInstance(getActivity());
        IBeaconProtocol.configureBluetoothAdapter(getActivity());
        mIBeaconProto.setListener(this);
        mAdapter = new ArrayAdapter<IBeacon>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                IBeacon beacon = getItem(position);

                text1.setText(beacon.getUuidHexStringDashed());
                text1.setTypeface(Typeface.MONOSPACE);
                text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

                text2.setText("Major: " + beacon.getMajor() + " Minor: " + beacon.getMinor() + " Distance: " + beacon.getProximity() + "m.");
                text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                return view;
            }
        };
        ListView lv = (ListView) view.findViewById(R.id.listview);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onBeaconClick(mAdapter.getItem(position));
            }
        });
    }

    public void onBeaconClick(IBeacon beacon) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.beacon_detail)
        .setMessage(getBeaconInfo(beacon))
        .show();
    }

    public void onStartScan() {
        mAdapter.clear();
        if (mIBeaconProto.isScanning()) {
            mIBeaconProto.stopScan();
        }
        mIBeaconProto.reset();
        mIBeaconProto.startScan();
    }

    @Override
    public void enterRegion(IBeacon ibeacon) {
        setLastEvent("enterRegion:" + ibeacon.getMacAddress());
    }

    @Override
    public void exitRegion(IBeacon ibeacon) {
        setLastEvent("exitRegion:" + ibeacon.getMacAddress());
    }

    @Override
    public void beaconFound(IBeacon ibeacon) {
        mAdapter.add(ibeacon);
        setLastEvent("beaconFound:" + ibeacon.getMacAddress());
    }

    @Override
    public void searchState(int state) {
        setLastEvent("searchState:" + state);
        mProgressBar.setVisibility(state == IBeaconProtocol.SEARCH_STARTED ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void operationError(int status) {
        setLastEvent("operationError:" + status);
    }

    private void setLastEvent(String msg) {
        Log.d("BeaconsList", msg);
        mLastEvent.setText(String.format("[%s] %s", mDateFormat.format(new Date(System.currentTimeMillis())), msg));
    }

    public static CharSequence getBeaconInfo(IBeacon beacon) {
        SpannableString uuid = new SpannableString(beacon.getUuidHexStringDashed());
        uuid.setSpan(new RelativeSizeSpan(0.8f), 0, uuid.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        CharSequence name = TextUtils.concat(bold("NAME:"), String.valueOf(beacon.getName()));
        CharSequence details = TextUtils.concat(bold("Major:"), String.valueOf(beacon.getMajor()), " ",
                bold("Minor:"), String.valueOf(beacon.getMinor()));
        CharSequence distance = TextUtils.concat(bold("Distance:"), String.valueOf(beacon.getProximity()), "meter(s)");
        CharSequence mac = TextUtils.concat(bold("MAC:"), beacon.getMacAddress());
        CharSequence battery = TextUtils.concat(bold("Battery:"), String.valueOf(beacon.getBattery()));
        CharSequence brrate = TextUtils.concat(bold("BroadcastRate:"), String.valueOf(beacon.getBroadcastRate()));
        CharSequence power = TextUtils.concat(bold("PowerValue:"), String.valueOf(beacon.getPowerValue()));
        CharSequence tx = TextUtils.concat(bold("TxValue:"), String.valueOf(beacon.getTxPower()));

        return TextUtils.concat(name, "\n", uuid, "\n", details, "\n", distance, "\n", mac, "\n", power, "\n", tx, "\n", battery, "\n", brrate, "\n");
    }

    public static CharSequence bold(String msg){
        SpannableString ids = new SpannableString(msg);
        ids.setSpan(new StyleSpan(Typeface.BOLD), 0, ids.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ids;
    }
}
