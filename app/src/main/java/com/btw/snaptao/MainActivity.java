package com.btw.snaptao;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.btw.snaptao.bean.StoreInfo;
import com.btw.snaptao.db.LocationDB;
import com.btw.snaptao.util.HttpCallbackListener;
import com.btw.snaptao.util.HttpUtils;
import com.btw.snaptao.util.JsonUtils;
import com.btw.snaptao.util.PositionUtil;
import com.btw.snaptao.util.Tools;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.annotations.SpriteFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements MapView.OnMapLongClickListener, MapView.InfoWindowAdapter, View.OnClickListener, MapView.OnMarkerClickListener, View.OnTouchListener, MapView.OnMapClickListener, MapView.OnInfoWindowClickListener, TencentLocationListener, MapView.OnMyLocationChangeListener {

    public static String TAG = "MainActivity";
    public static MapView mapView = null;//地图
    private Button but_login, but_mylocation;
    private LinearLayout but_refresh;

    private View infowindow1;//选择编辑或删除的view
    private Button info1_edit, info1_delete;//编辑/删除按钮
    private Marker select_marker;//被选中的marker

    private double lat, lng;//经纬度
    private Intent intent;

    public static final int OPENGPS_SUCCESS = 7;// 2代表请求成功
    public static final int GET_SUCCESS = 2;// 2代表请求成功
    public static final int GET_FAILE = 3;// 3代表请求失败
    public static final int GETBITMAP_SUCCESS = 4;// 4代表获取图片成功
    public static final int GPS_START = 5;// 4代表获取图片成功
    public static final int GPS_FAILE = 6;// 4代表获取图片成功

    private View shopview;
    private LinearLayout info_show, info_show1;
    private ImageView shopimage, shopimage1;
    private TextView info_shopname, info_shopadd, info_shopname1, info_shopadd1;
    private String shopname, shopadd, shopimageurl;
    private Bitmap bitmap;

    private SpriteFactory spriteFactory;
    private Sprite sprite, sprite1;

    private Location myLocation = null;//自己的位置
    private ArrayList<StoreInfo> infos = new ArrayList<StoreInfo>();//获取请求回来的商品信息
    private LocationDB db;
    private SQLiteDatabase sqliteDatabase;
    private Tools tools;

    private boolean infowindowshow = false;//判断infowindow是否显示

    private List<MarkerOptions> list_marker = new ArrayList<MarkerOptions>();

    //定时检查定位是否成功
    private final Timer timer = new Timer();
    private TimerTask task;
    final int[] max_time = {0};//请求定位的最大次数

    TencentLocationManager locationManager;//腾讯位置管理
    /*
     * 线程的handler处理
     */
    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SUCCESS:
                    for (int i = 0; i < infos.size(); i++) {
                        double lat = infos.get(i).getLat();
                        double lng = infos.get(i).getLng();
                        MarkerOptions mo = tools.getMarkerOptions("" + i, lat, lng, sprite1);
                        if (!list_marker.contains(mo)) {
                            list_marker.add(mo);
                            mapView.addMarker(mo);
                        }
                    }
                    break;
                case GET_FAILE:
                    Toast.makeText(MainActivity.this, "获取数据失败，请检查网络数据并刷新数据", Toast.LENGTH_LONG).show();//弹出Toast显示获取数据失败
                    break;
                case GETBITMAP_SUCCESS:
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        info_show.setVisibility(View.VISIBLE);
                        shopimage.setImageBitmap(bitmap);
                        info_show1.setVisibility(View.GONE);
                    } else {
                        info_show1.setVisibility(View.VISIBLE);
                        shopimage1.setImageBitmap(bitmap);
                        info_show.setVisibility(View.GONE);
                    }
                    break;
                case GPS_START:
                    Toast.makeText(MainActivity.this, "正在gps定位...", Toast.LENGTH_LONG).show();
                    break;
                case GPS_FAILE:
                    Toast.makeText(MainActivity.this, "请到GPS信号好的地方再定位", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止横屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //透明状态栏
        mapView = (MapView) findViewById(R.id.mapboxMapView);//实例化

        mapView.setStyleUrl(Style.MAPBOX_STREETS);//设置mapview的类型
        mapView.setZoomLevel(14);//设置mapview的地图级别
//        mapView.setMyLocationEnabled(true);//打开定位图层
        mapView.setLogoVisibility(View.GONE);
        mapView.setAttributionVisibility(View.GONE);
        mapView.onCreate(savedInstanceState);//这个不懂
        spriteFactory = new SpriteFactory(mapView);
        sprite = spriteFactory.fromResource(R.drawable.marker2);
        sprite1 = spriteFactory.defaultMarker();
        initialization();//实例化控件
        Getdata_start(); //请求获取数据

    }

    boolean w = false;

    //控件点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                Toast.makeText(MainActivity.this, "内测期间不用登录", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_refresh:
                Getdata_start();
                break;
            case R.id.but_mylocation:
                Toast.makeText(this, mapView.getMyLocation() + "", Toast.LENGTH_LONG).show();
                if (mapView.getMyLocation() == null) {
                    Toast.makeText(MainActivity.this, "您的定位尚未成功，请到GPS信号好的地方再次定位", Toast.LENGTH_SHORT).show();
                } else {
                    mapView.setCenterCoordinate(new LatLng(mapView.getMyLocation().getLatitude(), mapView.getMyLocation().getLongitude()));
                    but_mylocation.setBackgroundResource(R.drawable.mylocation);
                    Getdata_start();
                }
                break;
            case R.id.info1_edit:
                intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("lat", "" + lat);
                intent.putExtra("lng", "" + lng);
                startActivityForResult(intent, EditActivity.UPLOAD_SUCCESS);
                break;
            case R.id.info1_delete:
                mapView.removeMarker(select_marker);//点击删除清除选中的marker
                break;
            default:
                break;
        }
    }

    //点击marker事件
    @Override
    public boolean onMarkerClick(Marker marker) {
        mapView.setCenterCoordinate(marker.getPosition());

        if (marker.getTitle().equals("me") && !marker.isInfoWindowShown()) {
            return false;
        }

        if (!marker.getTitle().equals("添加地点") && !marker.isInfoWindowShown()) {
            int i = Integer.parseInt(marker.getTitle());
            shopname = "店名：" + infos.get(i).getMyname();
            shopadd = "地址：" + infos.get(i).getMyadd();
            shopimageurl = getString(R.string.getImg) + infos.get(i).getImg();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    bitmap = HttpUtils.getNetWorkBitmap(shopimageurl);
                    Message message = new Message();
                    message.what = GETBITMAP_SUCCESS;
                    handler.sendMessage(message);
                }
            }).start();
            infowindowshow = true;
        } else if (!marker.getTitle().equals("添加地点") && marker.isInfoWindowShown()) {
            infowindowshow = false;
        }

        //获取改marker与对应的经纬度
        select_marker = marker;
        lat = marker.getPosition().getLatitude();
        lng = marker.getPosition().getLongitude();
        return false;
    }

    //长按地图的事件
    @Override
    public void onMapLongClick(LatLng point) {
        //长按地图添加一个marker
        mapView.addMarker(tools.getMarkerOptions("添加地点", point.getLatitude(), point.getLongitude(), sprite));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override//返回infowindow
    public View getInfoWindow(Marker marker) {
        if (marker.getTitle().equals("添加地点")) {
            return infowindow1;
        } else {
            info_shopname.setText(shopname);
            info_shopadd.setText(shopadd);
            info_shopname1.setText(shopname);
            info_shopadd1.setText(shopadd);
            return shopview;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        //保存自己的位置到数据库
//        if (myLocation != null) {
//            db = new LocationDB(MainActivity.this, "Point.db", 1);
//            sqliteDatabase = db.getWritableDatabase();
//            db.updateData(sqliteDatabase, myLocation.getLatitude(), myLocation.getLongitude());
//        }

        //移除腾讯地图的监听
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putBoolean("myLocationEnabled", false);
    }

    private void initialization() {
        but_login = (Button) findViewById(R.id.btn_login);
        but_refresh = (LinearLayout) findViewById(R.id.btn_refresh);
        but_mylocation = (Button) findViewById(R.id.but_mylocation);

        //infowindow实例化
        infowindow1 = LayoutInflater.from(this).inflate(R.layout.infowindow1, null);
        info1_edit = (Button) infowindow1.findViewById(R.id.info1_edit);
        info1_delete = (Button) infowindow1.findViewById(R.id.info1_delete);

        //商店显示View实例化
        shopview = LayoutInflater.from(this).inflate(R.layout.infowindow_show, null);
        info_show = (LinearLayout) shopview.findViewById(R.id.info_show);
        info_show1 = (LinearLayout) shopview.findViewById(R.id.info_show1);
        shopimage = (ImageView) shopview.findViewById(R.id.ifw_shopimage);
        info_shopname = (TextView) shopview.findViewById(R.id.ifw_shopname);
        info_shopadd = (TextView) shopview.findViewById(R.id.ifw_shopadd);
        shopimage1 = (ImageView) shopview.findViewById(R.id.ifw_shopimage1);
        info_shopname1 = (TextView) shopview.findViewById(R.id.ifw_shopname1);
        info_shopadd1 = (TextView) shopview.findViewById(R.id.ifw_shopadd1);

        but_login.setOnClickListener(this);
        but_refresh.setOnClickListener(this);
        but_mylocation.setOnClickListener(this);

        mapView.setOnMapLongClickListener(this);//地图长按事件
        mapView.setOnMapClickListener(this);//地图长按事件
        mapView.setInfoWindowAdapter(this);//自定义Infowindow
        mapView.setOnMarkerClickListener(this);//点击marker事件
        mapView.setOnInfoWindowClickListener(this);
        mapView.setOnTouchListener(this);
        mapView.setOnMyLocationChangeListener(this);
        info1_edit.setOnClickListener(this);//编辑按钮点击
        info1_delete.setOnClickListener(this);//删除按钮点击

        tools = new Tools(this);//实例化Tools类

        myLocation = mapView.getMyLocation();
        Log.e(TAG, myLocation + "");
        //提示打开GPS定位
        Tools.initGPS(MainActivity.this);
        //如果mapbox定位不到自己的位置
//        if (myLocation == null || !myLocation.getProvider().equals("gps")) {
        //使用腾讯定位SDK来获取大概位置
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(0);//包含经纬度
        locationManager = TencentLocationManager.getInstance(this);
        locationManager.requestLocationUpdates(request, this);

//            if (Tools.getGpsState(MainActivity.this)) {
//                task = new Task();
//                timer.schedule(task, 2000, 2000);
//            }
//            db = new LocationDB(this, "Point.db", 1);
//            sqliteDatabase = db.getReadableDatabase();
//            double lng = db.getCursorToList().get(0).get("lng");
//            double lat = db.getCursorToList().get(0).get("lat");
//            //表示数据表还没有上次的定位信息
//            if (lng == 111 && lat == 111) {
//                mapView.setCenterCoordinate(new LatLng(22.517, 113.3638));//设置为默认中心点
//            } else {
//                mapView.setCenterCoordinate(new LatLng(lat, lng));
//            }
//        } else {
//            mapView.setCenterCoordinate(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));//设置map开始时的点
//        }
    }

    long waitTime = 2000;
    long touchTime_back;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - touchTime_back) >= waitTime) {
                //让Toast的显示时间和等待时间相同
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
                touchTime_back = currentTime;
            } else {
                //退出程序
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getUrl(double lnt, double lat, int radius) {
        //请求的网址前缀
        String url = getString(R.string.getUrl);
        url = url + "?lng=" + lnt + "&lat=" + lat + "&radius=" + radius + "&myuser=11";
        return url;
    }

    /**
     * 获取数据
     */
    private String getdata(double lng, double lat) {
        String str = HttpUtils.sendHttpRequest(getUrl(lng, lat, 5), new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {

                for (StoreInfo item : JsonUtils.getStoreInfo(response)) {
                    if (!infos.contains(item)) {
                        infos.add(item);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
            }
        });
        return str;
    }

    private void Getdata_start() {
        Log.e(TAG, "刷新数据");

        lat = mapView.getCenterCoordinate().getLatitude();
        lng = mapView.getCenterCoordinate().getLongitude();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String get_show = "";
                get_show = getdata(lng, lat);
                Message message = new Message();
                if (get_show.equals(HttpUtils.GET_SUCCESS)) {
                    message.what = GET_SUCCESS;
                } else {
                    message.what = GET_FAILE;
                }
                handler.sendMessage(message);
            }
        }).start();
    }

    long touchTime = 0;
    long currentTime = 0;


    double downX, downY;
    double upX, upY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!infowindowshow) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                downX = event.getX();
                downY = event.getY();
            }
            if (MotionEvent.ACTION_UP == event.getAction()) {
                upX = event.getX();
                upY = event.getY();
                currentTime = System.currentTimeMillis();
                if (downX - upX != 0 && downY - upY != 0) {
                    but_mylocation.setBackgroundResource(R.drawable.notmylocation);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            touchTime = System.currentTimeMillis();
                            if (touchTime - currentTime >= 2000) {
                                Getdata_start();
                            }
                        }
                    }, 2000);
                }
            }
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        infowindowshow = false;
    }

    /**
     * 腾讯位置监听
     */
    boolean isLoation = false;
    boolean State = false;
    MarkerOptions mos_old;

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        LatLng position = PositionUtil.gcj_To_Gps84(tencentLocation.getLatitude(), tencentLocation.getLongitude());
        MarkerOptions mos_new = tools.getMarkerOptions("me", position.getLatitude(), position.getLongitude(), sprite);
        if (!State) {
            if (mos_old != null) {
                mapView.removeMarker(mos_old.getMarker());
            }
            mapView.addMarker(mos_new);
        }
        mos_old = mos_new;

        if (!isLoation) {
            //设置地图中心点
            mapView.setCenterCoordinate(position);
            isLoation = true;

        }

    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        Log.e(TAG, s + "\n" + i + "\n" + s1);
        if (s.equals("gps") && i == 3) {//GPS可用，代表GPS开关打开，且搜星定位成功
            mapView.setMyLocationEnabled(true);
            if (mos_old != null) {
                mapView.removeMarker(mos_old.getMarker());
            }
            State = true;
        } else if (s.equals("gps") && i == 0) {//gps开关关闭
            mapView.setMyLocationEnabled(false);
            State = false;
        } else if (s.equals("gps") && i == 4) {//GPS不可用，不可用有多种可能，比如GPS开关被关闭，GPS开关开着但是没办法搜星或者在室内等定位不成功的情况
            mapView.setMyLocationEnabled(false);
            State = false;
        } else if (s.equals("gps") && i == 1) {//gps打开
            mapView.setMyLocationEnabled(false);
            State = false;
        }
    }

    @Override
    public void onMyLocationChange(@Nullable Location location) {
    }

//    public class Task extends TimerTask {
//
//        @Override
//        public void run() {
//            Message message = new Message();
//            max_time[0]++;
//            myLocation = mapView.getMyLocation();
//            if (max_time[0] == 0) {
//                message.what = GPS_START;
//            }
//            if (max_time[0] > 12) {
//
//                message.what = GPS_FAILE;
//                timer.cancel();
//                handler.sendMessage(message);
//                return;
//            }
//            if (myLocation != null) {
//                double lat = myLocation.getLatitude();
//                double lng = myLocation.getLongitude();
//                mapView.setCenterCoordinate(new LatLng(lat, lng));
//                timer.cancel();
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {// 处理intent返回的参数
        switch (requestCode) {
            case EditActivity.UPLOAD_SUCCESS:
                Getdata_start();
                break;
            case OPENGPS_SUCCESS:
                Toast.makeText(MainActivity.this, "正在定位请耐心等候....", Toast.LENGTH_LONG).show();
//                task = new Task();
//                timer.schedule(task, 2000, 2000);
                break;
            default:
                break;
        }
    }
}
