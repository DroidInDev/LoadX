package com.cn.loadx.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cn.loadx.R;
import com.cn.loadx.interfaces.TripListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.http.HTTP;

/**
 * Created by Admin on 27-01-2018.
 */

public class ApplicationUtil {
    public static final String APPTAG = "LDHOME";
    private static ApplicationUtil mInstance;

    public static ApplicationUtil getmInstance() {
        if (mInstance == null) {
            mInstance = new ApplicationUtil();
        }
        return mInstance;
    }

    public Dialog checkUrInterntConnection(Context context, DialogInterface.OnClickListener positiveButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.no_internet);
        builder.setTitle(R.string.no_internet);
        builder.setMessage(R.string.check_network_connection);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, positiveButtonListener);
        return builder.create();
    }

    public Dialog noTripDialog(Context context, DialogInterface.OnClickListener positiveButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.loadx_appicon);
        builder.setTitle(R.string.no_trip);
        builder.setMessage(R.string.no_ongoig_trip);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, positiveButtonListener);
        return builder.create();
    }

    public void callCustomerCareDialog(final Context context, final String mobileNumber) {
        final Dialog supervisorDialog = new Dialog(context);
        supervisorDialog.setContentView(R.layout.dialog_customer_care);
        ImageView closeDialog = (ImageView) supervisorDialog.findViewById(R.id.dialogClose);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supervisorDialog.dismiss();
            }
        });
        TextView txtCustomerCareNo = supervisorDialog.findViewById(R.id.txtCustomerCareno);
        txtCustomerCareNo.setText(AppConstants.CUSTOMER_CARE_NO);
        TextView txtBtnCall = (TextView) supervisorDialog.findViewById(R.id.txtBtnCall);
        txtBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callMobileNumber(mobileNumber,context);
                supervisorDialog.dismiss();
            }
        });
        supervisorDialog.show();
    }
    public void noTripDialog(final Context context){
        final TripListener tripListener =(TripListener)context;
        final Dialog noTripDlg = new Dialog(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        noTripDlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        noTripDlg.setContentView(R.layout.dialog_no_trip);
        TextView tryAgainTxt = (TextView)noTripDlg.findViewById(R.id.txtBtnLoadTry);
        tryAgainTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noTripDlg.dismiss();
               tripListener.tryLoadingTripAgain();

            }
        });
        noTripDlg.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if(context instanceof Activity){
                        ((Activity)context).finish(); }
                }
                return true;
            }
        });
        noTripDlg.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        noTripDlg.show();
    }
    public void callSupervisorDialog(final Context context,final  String name,final String mobileNumber) {
        final Dialog supervisorDialog = new Dialog(context);
        supervisorDialog.setContentView(R.layout.dialog_trip_supervisor);
        TextView txtName = (TextView)supervisorDialog.findViewById(R.id.txtSupervisorName);
        txtName.setText(name);
        TextView txtNo = (TextView)supervisorDialog.findViewById(R.id.txtSupervisorNo);
        txtNo.setText(mobileNumber);
        ImageView closeDialog = (ImageView) supervisorDialog.findViewById(R.id.dialogClose);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supervisorDialog.dismiss();
            }
        });
        TextView txtBtnCall = (TextView) supervisorDialog.findViewById(R.id.txtBtnCall);
        txtBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               callMobileNumber(mobileNumber,context);
                supervisorDialog.dismiss();
            }
        });
        TextView txtBtnMessage = (TextView) supervisorDialog.findViewById(R.id.txtBtnMessage);
        txtBtnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              // sendSMS(context,mobileNumber,name);

                supervisorDialog.dismiss();
                sendMessage(context,mobileNumber);
            }
        });
        supervisorDialog.show();
    }
    private void sendSMS(Context context,String phoneNumber, String message)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(context,"SMS Sent Succesfully",Toast.LENGTH_SHORT).show();
    }
    private void sendMessage(Context context,String phoneNumber){
        Uri sms_uri = Uri.parse("sms:"+phoneNumber);
        Intent sms_intent = new Intent(Intent.ACTION_SENDTO);
        sms_intent.setType("text/plain");
        sms_intent.setData(sms_uri);
        sms_intent.putExtra("sms_body","");
        if (sms_intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(sms_intent);
        }
    }
    private void callMobileNumber(String mobileNumber, Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + mobileNumber));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        context.startActivity(intent);
    }

    public static Bitmap getMapBitmap(Bitmap mapBitmap, String locationName){
        Bitmap mutableBitmap = mapBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40);
        Rect boundsText = new Rect();
       // String str = locationName.substring(0,1);
        paint.getTextBounds(locationName,
                0,locationName.length(),
                boundsText);
        int x = (mutableBitmap.getWidth() - boundsText.width()) / 2;
        int y = (((mutableBitmap.getHeight() + boundsText.height()) / 2))/2+20;
        canvas.drawText(locationName.toUpperCase(), x, y, paint);
        return mutableBitmap;
    }
    public static boolean contains(JSONObject jsonObject, String key) {
        return jsonObject != null && jsonObject.has(key) && !jsonObject.isNull(key);
    }

    public static Dialog showDialog(final Context context, String positiveButton,DialogInterface.OnClickListener positiveButtonListener,String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButton,positiveButtonListener );
        builder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
    public static String getTimeStamp(String id){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            return dateFormat.format(new Date()); // Find todays date
           /* Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            return ts;*/
        } catch (Exception e) {
            e.printStackTrace();

            return id;
        }
    }
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    public static String getExtension(String path){
      return   path.substring(path.lastIndexOf("."));
    }

}
