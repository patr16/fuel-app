package com.fuel;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import androidx.appcompat.app.AlertDialog;

public class Alert {
    public interface FlagButtonCallback {
        void onFlagButtonChanged(String flag);
    }

    // --------------------------------------------------Alert normal
    public static void alertDialog(final Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text)
                .setTitle("Info")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        }, 10000); // 5000 millisecondi = 5 secondi
    }

    // -------------------------------------------------- Alert whit Flag Button
    public static void alertDialogFlag(final Context context, String text, final String[] flagButton, final FlagButtonCallback callback) {

        //public static void alertDialogFlag(final Context context, String text, final String[] flagButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text)
                .setTitle("Info")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (callback != null) {
                            callback.onFlagButtonChanged("2");

                        dialog.dismiss();
                        }
                    }
                });

        if (flagButton[0].equals("1")) {
            System.out.println("alert flag in ingresso " + flagButton[0]);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    flagButton[0] = "2"; // Imposta la variabile flagButton a "1"
                    dialog.dismiss();
                }
            });
        }
        System.out.println("alert flag  zzzz " + flagButton[0]);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        }, 20000); // 5000 millisecondi = 5 secondi
    }
}
