package com.applex.utsav.utility;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.applex.utsav.ActivityNotification;
import com.applex.utsav.ActivityProfile;
import com.applex.utsav.MainActivity;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.models.AccessToken;
import com.applex.utsav.preferences.IntroPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.Objects;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MessagingService extends FirebaseMessagingService {

    public static String nCount;
    public static final String INTENT_FILTER = "INTENT_FILTER";
    private Handler handler;

    public MessagingService() {}

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();

        /////////////////DAY OR NIGHT MODE///////////////////
        if(new IntroPref(this).getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getBoolean("night_mode"))) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        } else if(new IntroPref(this).getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(new IntroPref(this).getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        AccessToken accessToken = new AccessToken();
        accessToken.setRegToken(s);

        if(FirebaseAuth.getInstance().getUid() != null){
            FirebaseFirestore.getInstance()
                    .collection("Users/" + FirebaseAuth.getInstance().getUid() + "/AccessToken/")
                    .document("Token").set(accessToken);
        }
    }

    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData().get("clickAction") != null) {
            String message = remoteMessage.getData().get("body");
            String title = remoteMessage.getData().get("title");
            String action = remoteMessage.getData().get("clickAction");
            String type = remoteMessage.getData().get("type");
            String postID = remoteMessage.getData().get("postID");
            String dp = remoteMessage.getData().get("dp");
            String ts = remoteMessage.getData().get("ts");
            String pCom_ts = remoteMessage.getData().get("pCom_ts");
            nCount = remoteMessage.getData().get("notifCount");

            if(ActivityNotification.active) {
                new ActivityNotification().buildRecyclerView();
            } else {
                Intent intent = new Intent(INTENT_FILTER);
                sendBroadcast(intent);

                if(dp != null) {
//                    try {
//                        URL url = new URL(dp);
//                        image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                    } catch(IOException e) {
//                        e.printStackTrace();
//                    }
                    runOnUiThread(() -> Picasso.get().load(dp)
                            .resize(200, 200)
                            .transform(new CropCircleTransformation())
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    sendNotification1(MessagingService.this, message, title, Objects.requireNonNull(action), type, postID, ts, pCom_ts, bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    sendNotification2(MessagingService.this, message, title, Objects.requireNonNull(action), type, ts, pCom_ts, postID);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) { }
                            }));
                } else {
                    sendNotification2(this, message, title, Objects.requireNonNull(action), type, ts, pCom_ts, postID);
                }
            }
        }
        else {
            shownotification(this, Objects.requireNonNull(remoteMessage.getNotification()).getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    public static void sendNotification1(Context context, String message, String title, String action, String type, String postID, String ts, String pCom_ts, Bitmap dp) {

        Intent intent;

        if(action.matches("Feeds") && type == null) {
            intent = new Intent(context, ViewMoreHome.class);
            intent.putExtra("postID", postID);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setLargeIcon(dp)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if (action.matches("Feeds")) {
            intent = new Intent(context, ViewMoreHome.class);
            intent.putExtra("type", type);
            intent.putExtra("postID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setLargeIcon(dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Feeds_Text")) {
            intent = new Intent(context, ViewMoreText.class);
            intent.putExtra("type", type);
            intent.putExtra("postID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setLargeIcon(dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Reels")) {
            intent = new Intent(context, ReelsActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("docID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("bool", "1");
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setLargeIcon(dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Profile")) {
//            intent = new Intent(context, ActivityProfileCommittee.class);
            intent = new Intent(context, ActivityProfile.class);
            intent.putExtra("uid", FirebaseAuth.getInstance().getUid());
            intent.putExtra("to", "profile");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setLargeIcon(dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
    }

    public static void sendNotification2(Context context, String message, String title, String action, String type, String postID, String ts, String pCom_ts) {

        Intent intent;

        if(action.matches("Feeds") && type == null) {
            intent = new Intent(context, ViewMoreHome.class);
            intent.putExtra("postID", postID);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if (action.matches("Feeds")) {
            intent = new Intent(context, ViewMoreHome.class);
            intent.putExtra("type", type);
            intent.putExtra("postID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Feeds_Text")) {
            intent = new Intent(context, ViewMoreText.class);
            intent.putExtra("type", type);
            intent.putExtra("postID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Reels")) {
            intent = new Intent(context, ReelsActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("postID", postID);
            intent.putExtra("ts", ts);
            intent.putExtra("pCom_ts", pCom_ts);
            intent.putExtra("from", "noti");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
        else if(action.matches("Profile")) {
//            intent = new Intent(context, ActivityProfileCommittee.class);
            intent = new Intent(context, ActivityProfile.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "MyNotifications")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
    }

    public void shownotification(Context context, String title, String message) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MyNotifications")
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(ContextCompat.getColor(context, R.color.colorNotif))
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int)System.currentTimeMillis(), builder.build());
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {

        if (bitmap != null && !bitmap.isRecycled()) {
            final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);

            final int color = Color.RED;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawOval(rectF, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
            return output;
        }
        return null;
    }

}
