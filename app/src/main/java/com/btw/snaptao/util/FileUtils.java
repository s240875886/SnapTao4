package com.btw.snaptao.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by thp on 2015/12/17.
 */
public class FileUtils {
    public static String TAG = "FileUtils";
    //取得SD卡文件路径
    public static File SDPATH = Environment.getExternalStorageDirectory();


    /**
     * 检查SD卡是否存在
     */
    public static boolean ExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得SD卡剩余空间
     * 单位MB
     */
    public static long getSDFreeSize() {
        //获得文件的空间信息
        StatFs sf = new StatFs(SDPATH.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; //单位MB
    }

    /**
     * 获得SD卡总容量
     *
     * @return MB
     */
    public static long getSDAllSize() {
        //获得文件的空间信息
        StatFs sf = new StatFs(SDPATH.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; //单位MB
    }


    /**
     * @param bmp      需要保存的图片bitmap
     * @param filename 图片的名字
     */
    public static void saveImage(final Bitmap bmp, final String dirname, final String filename) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                File imgDir = new File(SDPATH, dirname);
                //如果该目录不存在 新建目录
                if (!imgDir.exists()) {
                    imgDir.mkdirs();
                }
                String fileName = filename + ".jpg";
                File file = new File(imgDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "保存文件出错:" + e.toString());
                }
            }
        }).start();

    }

    /**
     * @param Imgname 图片的名字
     * @return
     */
    public static Bitmap getImage(String dirname, String Imgname) {
        Bitmap bitmap = null;
        String fileDir = SDPATH + "/" + dirname + "/" + Imgname + ".jpg";
        File ImgFile = new File(fileDir);
        //若该文件存在
        if (ImgFile.exists()) {
            //将文件转换成bitmap
            bitmap = BitmapFactory.decodeFile(fileDir);

        } else {
            Log.e(TAG, "没找到该图片" + fileDir);
        }
        return bitmap;
    }
}
