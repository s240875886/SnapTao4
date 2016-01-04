package com.btw.snaptao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.btw.snaptao.util.Tools;
import com.btw.snaptao.util.UploadHttp;

import java.io.File;

public class EditActivity extends Activity implements View.OnClickListener {

    private LinearLayout take_photo, choose_photo;
    private Button but_cancle, but_submit;//资料填写的对话框的按钮
    private EditText edit_shopname, edit_shopaddress;//edit_shoptel, edit_shopmark;//资料填写的对话框的编辑框
    public static ImageView showimage;//资料填写完之后要显示的图片

    private String show = "";// 上传成功之后提示
    public static final int UPLOAD_SUCCESS = 1;// 1代表上传成功
    public static final int UPLOAD_FAILE = 0;// 0代表上传失败

    public static final int TAKE_PHOTO = 1;//选择拍照的参数
    public static final int CHOOSE_PHOTO = 2;//选择图库的参数

    private String myname, myadd, mytel = null, mymark = null, mylat, mylng;//要上传的数据
    private ProgressDialog progressdialog;// 加载提示框

    private Uri imageUri;// 图片路径的uri格式
    private Intent intent;
    public static File file, file1;// 创建file对象，用于存储拍照后的图片

    private Tools tools;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLOAD_SUCCESS://处理上传成功后的逻辑
                    progressdialog.dismiss();// 关闭加载框
                    showimage.setVisibility(View.GONE);//隐藏image
                    edit_shopname.setText("");//把edittext的内容清空
                    edit_shopaddress.setText("");
//                    edit_shoptel.setText("");
//                    edit_shopmark.setText("");
                    Toast.makeText(EditActivity.this, show, Toast.LENGTH_LONG).show();//弹出Toast显示成功
                    EditActivity.this.setResult(UPLOAD_SUCCESS);
                    finish();          //提交完之后关闭activity
                    break;
                case UPLOAD_FAILE://处理上传失败后的逻辑
                    progressdialog.dismiss();// 线程处理完成之后关闭加载框
                    Toast.makeText(EditActivity.this, show, Toast.LENGTH_LONG).show();//弹出Toast显示失败
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //透明状态栏

        take_photo = (LinearLayout) findViewById(R.id.take_photo);
        choose_photo = (LinearLayout) findViewById(R.id.choose_photo);
        but_cancle = (Button) findViewById(R.id.but_cancle);
        but_submit = (Button) findViewById(R.id.but_submit);
        edit_shopname = (EditText) findViewById(R.id.edit_shopname);
        edit_shopaddress = (EditText) findViewById(R.id.edit_shopaddress);
//        edit_shoptel = (EditText) findViewById(R.id.edit_shoptel);
//        edit_shopmark = (EditText) findViewById(R.id.edit_shopmark);
        showimage = (ImageView) findViewById(R.id.edit_image);

        take_photo.setOnClickListener(this);//设置拍照按钮的点击事件
        choose_photo.setOnClickListener(this);//设置选择照片按钮的点击事件
        but_cancle.setOnClickListener(this);//设置取消按钮的点击事件
        but_submit.setOnClickListener(this);//设置提交按钮的点击事件

        intent = getIntent();
        mylat = intent.getStringExtra("lat");
        mylng = intent.getStringExtra("lng");

        tools = new Tools(this);//实例化Tools类

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo://点击拍照的逻辑
                tools.getTimeStr();// 获取以时间为文件名的储存地址
                imageUri = Uri.fromFile(file);// 把file转换成uri格式
                intent = new Intent("android.media.action.IMAGE_CAPTURE");// 设置intent
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);// 保存的路径
                startActivityForResult(intent, TAKE_PHOTO);// 启动相机
                break;
            case R.id.choose_photo://点击选择相册的逻辑
                tools.getTimeStr();// 获取以时间为文件名的储存地址
                intent = new Intent("android.intent.action.GET_CONTENT");// 设置intent
                intent.setType("image/*");// 设置类型
                startActivityForResult(intent, CHOOSE_PHOTO);// 打开相册
                break;
            case R.id.but_submit://点击提交的逻辑、
                //获取数据
                myname = edit_shopname.getText().toString();
                myadd = edit_shopaddress.getText().toString();
//                mytel = edit_shoptel.getText().toString();
//                mymark = edit_shopmark.getText().toString();
                if (myname.equals("") || myadd.equals("")) {//加入数据为空显示Toast
                    Toast.makeText(EditActivity.this, "请填写完整必要的信息", Toast.LENGTH_SHORT).show();
                } else {
                    progressdialog = ProgressDialog.show(EditActivity.this, "请稍后...",// 显示加载框
                            "上传中...");
                    new Thread(new Runnable() {// 开启上传的线程
                        @Override
                        public void run() {
                            // 上传压缩后的图片和输入的文字，并返回显示的文字
                            // 传入的参数一个是压缩后图片的路径，另外的是keyword
                            show = UploadHttp.uploadFile(file1, myname, myadd, mytel,
                                    mymark, mylat, mylng);
                            Message message = new Message();// 创建一个message
                            if (show.equals("上传成功")) {
                                message.what = UPLOAD_SUCCESS; // 设置参数
                            } else {
                                message.what = UPLOAD_FAILE; // 设置参数
                            }
                            handler.sendMessage(message);// 向handler发送参数
                        }
                    }).start();// 线程开始
                }
                break;
            case R.id.but_cancle://取消提交
                showimage.setVisibility(View.GONE);//隐藏image
                edit_shopname.setText("");//把edittext的内容清空
                edit_shopaddress.setText("");
//                edit_shoptel.setText("");
//                edit_shopmark.setText("");
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {// 处理intent返回的参数
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    tools.handlerImage(file.toString());// 传入地址，处理图片
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        tools.handleImageOnKitkat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        tools.handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }
}
