package com.btw.snaptao.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

import com.btw.snaptao.EditActivity;
import com.btw.snaptao.MainActivity;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class    Tools {

    private static Context context;
    private static Bitmap bitmap;// 图片设置成bitmap格式
    private static String str_TIME;// 获取当前时间作为保存文件名
    private static Time time;// 获取系统当前时间
    private static int month;// 月份

    public Tools(Context context) {
        this.context = context;
    }

    /**
     * 获取系统当前时间和设置file的文件名
     */
    public static void getTimeStr() {
        time = new Time();
        time.setToNow();// 获取新的系统时间
        month = time.month++;// 月份加1
        str_TIME = "" + time.year + month + time.monthDay + time.hour
                + time.minute + time.second;// 设置文件名为时间
        EditActivity.file = new File("/sdcard/HintMint/image/" + str_TIME + ".jpg");// 这个是拍照后保存的名字
        EditActivity.file1 = new File("/sdcard/HintMint/image/" + str_TIME + 1 + ".jpg");// 这个是压缩后保存的名字
        try {
            if (!EditActivity.file.getParentFile().exists()) {// 判断有无这个文件夹
                EditActivity.file.getParentFile().mkdirs();// 没有就创建文件夹
                EditActivity.file.createNewFile();// 创建文件
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩图片
     */
    public static Bitmap compressBySizeTest(String pathName, int targetWidth) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;// 不去真的解析图片，只是获取图片的头部信息，包含宽高等；
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
        // 得到图片的宽度、高度；
        float imgWidth = opts.outWidth;
        // 分别计算图片宽度、高度与目标宽度、高度的比例；取大于等于该比例的最小整数；
        int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
        opts.inSampleSize = widthRatio;
        // 设置好缩放比例后，加载图片进内容；
        opts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(pathName, opts);
        return bitmap;
    }

    /**
     * android4.4及以上的相册选择方法
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void handleImageOnKitkat(Intent data) {
        String imagepath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的Uri ，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                String id = docId.split(":")[1];// 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagepath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagepath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagepath = getImagePath(uri, null);
        }
        handlerImage(imagepath);
    }

    /**
     * android4.4以下的相册选择方法
     */
    public static void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        handlerImage(imagePath);
    }

    /**
     * 获取图片地址
     */
    public static String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings({"deprecation"})
    public static void handlerImage(String imagepath) {
        //根据图片的filepath获取到一个ExifInterface的对象
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagepath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        int digree = 0;
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
            bitmap = BitmapFactory.decodeFile(imagepath);// 将从相册选择完毕的图片转化成bitmap格式
            bitmap = compressBySizeTest(imagepath, 300);
            if (digree != 0) {
                // 旋转图片
                Matrix m = new Matrix();
                m.postRotate(digree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), m, true);
            }
            try {
                FileOutputStream out = new FileOutputStream(EditActivity.file1);// 保存到file1地址
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditActivity.showimage.setVisibility(View.VISIBLE);
            EditActivity.showimage.setImageBitmap(bitmap);
        }
    }

        //描点

    public static MarkerOptions getMarkerOptions(String title, double lat, double lng, Sprite sprite) {
        MarkerOptions marker = new MarkerOptions();
        marker.title(title);
        marker.icon(sprite);
        marker.position(new LatLng(lat, lng));
        return marker;
    }

    /**
     * 监听GPS
     */
    public static void initGPS(final Activity activity) {
        LocationManager locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(activity, "请打开GPS",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage("请打开GPS");
            dialog.setPositiveButton("确定",
                    new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivityForResult(intent, MainActivity.OPENGPS_SUCCESS); // 设置完成后返回到原来的界面

                        }
                    });
            dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            dialog.show();
        } else {
        }
    }

    /**
     * 判断是否开启GPS
     */
    public static boolean getGpsState(Activity activity) {
        LocationManager locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }
}
