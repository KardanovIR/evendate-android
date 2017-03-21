package ru.evendate.android.ui.checkin;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.Ticket;

public class QrScannerFragment extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        QRCodeReaderView.OnQRCodeReadListener {

    public static final String KEY_EVENT_ID = "event_id";
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    @Bind(R.id.qr_reader_container) FrameLayout frameLayout;
    QRCodeReaderView qrCodeReaderView;
    CheckInContract.QRReadListener mListener;
    private boolean torchEnabled = false;


    public static QrScannerFragment newInstance(int eventId) {

        Bundle args = new Bundle();
        args.putInt(KEY_EVENT_ID, eventId);
        QrScannerFragment fragment = new QrScannerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
        return view;
    }

    private void initQRCodeReaderView() {
        qrCodeReaderView = new QRCodeReaderView(getContext());
        frameLayout.addView(qrCodeReaderView);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setFrontCamera();
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.qr_scanner_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_torch) {
            if (qrCodeReaderView == null) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                torchEnabled = !torchEnabled;
                qrCodeReaderView.setTorchEnabled(torchEnabled);
            }
            return true;
        }
        return false;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            //todo finish activity
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Ticket ticket;
        try {
            Gson gson = new Gson();
            ticket = gson.fromJson(text, Ticket.class);
        } catch (JsonSyntaxException e) {
            mListener.onQrReadError();
            qrCodeReaderView.setQRDecodingEnabled(false);
            new Handler().postDelayed(() -> qrCodeReaderView.setQRDecodingEnabled(true), 400);
            return;
        }
        mListener.onQrRead(getArguments().getInt(KEY_EVENT_ID), ticket.getUuid());
        qrCodeReaderView.setQRDecodingEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (CheckInContract.QRReadListener)context;
    }

    public void startQrDecoding() {
        if (qrCodeReaderView != null) {
            qrCodeReaderView.setQRDecodingEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
