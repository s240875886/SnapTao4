package com.btw.snaptao.util;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class UploadHttp {

    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10000; // 超时时间1000为一秒
    private static final String CHARSET = "utf-8"; // 设置编码
    public static final String SUCCESS = "上传成功";// 成功上传返回的值
    public static final String FAILURE = "上传失败";// 上传失败返回的值
    public static final String FILE_NULL = "请添加图片";// 内容为空失败返回的值
    public static final String OUT_TIME = "连接超时，上传失败";// 连接超时失败返回的值
    public static final String RequestURL = "http://jetso.hintmint.com.cn/api/GetDiscounts";// 上传地址
    public static final String keyname[] = {"myname", "myadd", "mytel",
            "mymark", "myuser", "mykey", "lat", "lng"};

    private static String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
    private static String PREFIX = "--", LINE_END = "\r\n";// 必要的分隔符
    private static String CONTENT_TYPE = "multipart/form-data"; // 内容类型
    private static String CONTENT_BOTTOM = PREFIX + BOUNDARY + PREFIX
            + LINE_END;// 协议的末尾

    public static String uploadFile(File file, String myname, String myadd,
                                    String mytel, String mymark, String lat, String lng) {

        String keyword[] = {myname, myadd, mytel, mymark, null, null, lat, lng};

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);// 设置读取超时
            conn.setConnectTimeout(TIME_OUT);// 设置连接服务器超时时间
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                    + BOUNDARY);
            if (file != null) {// 当文件不为空，把文件包装并且上传

                OutputStream outputSteam = conn.getOutputStream();// 获取连接的输出流

                DataOutputStream dos = new DataOutputStream(outputSteam);// 数据输出

                StringBuffer sb = new StringBuffer();// 定义一个StringBuffer

                sb.append(PREFIX + BOUNDARY + LINE_END);// 定义协议的头
                /**
                 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的,比如:abc.png,
                 * 数据内容前面需要有Content-Disposition,
                 * Content-Type以及Content-Transfer-Encoding等说明字段
                 */
                sb.append("Content-Disposition: form-data; name=\"imgfile\"; filename=\""
                        + file.getName() + "\"" + LINE_END);// 设置传入图片
                sb.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINE_END + LINE_END);

                dos.write(sb.toString().getBytes());// 输出协议的头

                InputStream is = new FileInputStream(file);// 把文件转化为字节类型
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len); // 输出文件
                }
                is.close();// 关闭写入流

                dos.write(getNewcontent(keyword).getBytes());// 输出文本数据

                dos.write(LINE_END.getBytes());// 换行符

                dos.write(CONTENT_BOTTOM.getBytes());// 输出协议的末尾
                dos.flush();// 将内存流中的内容写入到文件中并且清空内存流中的内容

                String str = inputStreamString(conn.getInputStream());
                str = str.substring(1, 10);
                if (str.equals("\"error\":0")) {
                    Log.e(TAG, SUCCESS);
                    return SUCCESS;
                } else if (str.equals("\"error\":1")) {
                    Log.e(TAG, FAILURE);
                    return FAILURE;
                }
            } else {
                Log.e(TAG, FILE_NULL);
                return FILE_NULL;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, FAILURE);
        return FAILURE;
    }

    private static String getNewcontent(String keyword[]) {

        String key = "";
        for (int i = 0; i < 8; i++) {
            key = key + LINE_END + PREFIX + BOUNDARY + LINE_END
                    + "Content-Disposition: form-data; name=\"" + keyname[i]
                    + "\"" + LINE_END
                    + "Content-Type: application/octet-stream; charset="
                    + CHARSET + LINE_END + LINE_END + keyword[i];
        }
        return key;
    }

    private static String inputStreamString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
