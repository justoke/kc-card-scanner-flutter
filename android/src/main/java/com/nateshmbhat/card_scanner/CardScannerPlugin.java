package com.nateshmbhat.card_scanner;
import android.os.Build;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.nateshmbhat.card_scanner.scanner_core.models.CardDetails;
import com.nateshmbhat.card_scanner.scanner_core.models.CardScannerOptions;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class CardScannerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    private static final int SCAN_REQUEST_CODE = 49193;
    private Activity activity;
    public static MethodChannel channel;
    public final static String METHOD_CHANNEL_NAME = "nateshmbhat/card_scanner";
    private Context context;
    private Result pendingResult;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), METHOD_CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        context = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("scan_card".equals(call.method)) {
            if (activity == null) {
                result.error("no_activity", "card_scanner plugin requires a foreground activity.", null);
                return;
            }
            if (pendingResult != null) {
                result.error("ALREADY_ACTIVE", "Scan card is already active", null);
                return;
            }
            pendingResult = result;
            showCameraActivity(call);
        } else {
            result.notImplemented();
        }
    }

    void showCameraActivity(MethodCall call) {
        // Check if arguments is a Map<String, String>
        if (!(call.arguments instanceof Map)) {
            pendingResult.error("INVALID_ARGUMENTS", "Expected Map<String, String> arguments", null);
            pendingResult = null;
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) call.arguments;
        CardScannerOptions cardScannerOptions = new CardScannerOptions(map);
        Intent intent = new Intent(context, CardScannerCameraActivity.class);
        intent.putExtra(CardScannerCameraActivity.CARD_SCAN_OPTIONS, cardScannerOptions);
        activity.startActivityForResult(intent, SCAN_REQUEST_CODE);
    }

    /*@Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra(CardScannerCameraActivity.SCAN_RESULT)) {
                    // Use the non-deprecated getParcelableExtra with class type
                    CardDetails cardDetails = data.getParcelableExtra(CardScannerCameraActivity.SCAN_RESULT, CardDetails.class);
                    pendingResult.success(cardDetails != null ? cardDetails.toMap() : null);
                } else {
                    pendingResult.success(null);
                }
                pendingResult = null;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                pendingResult.success(null);
                pendingResult = null;
            }
            return true;
        }
        return false;
    }*/

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra(CardScannerCameraActivity.SCAN_RESULT)) {
                    CardDetails cardDetails;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        cardDetails = data.getParcelableExtra(CardScannerCameraActivity.SCAN_RESULT, CardDetails.class);
                    } else {
                        @SuppressWarnings("deprecation")
                        cardDetails = (CardDetails) data.getParcelableExtra(CardScannerCameraActivity.SCAN_RESULT);
                    }
                    pendingResult.success(cardDetails != null ? cardDetails.toMap() : null);
                } else {
                    pendingResult.success(null);
                }
                pendingResult = null;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                pendingResult.success(null);
                pendingResult = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    }

    @Override
    public void onDetachedFromActivity() {
    }
}
