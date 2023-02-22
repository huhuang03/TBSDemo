package com.jackfruit.tbsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private TextView mTv;
  private TextView mTv2;
  private TextView mTv3;
  private TextView mTv4;
  private TextView tvInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    FileUtils.createDocumentsFile(this);
    loadInfo();
  }

  private void initView() {
    tvInfo = findViewById(R.id.tvInfo);
    mTv = (TextView) findViewById(R.id.tv5);
    mTv.setOnClickListener(this);
    mTv2 = (TextView) findViewById(R.id.tv2);
    mTv2.setOnClickListener(this);
    mTv3 = (TextView) findViewById(R.id.tv3);
    mTv3.setOnClickListener(this);
    mTv4 = (TextView) findViewById(R.id.tvOpenInApp);
    mTv4.setOnClickListener(this);

    findViewById(R.id.btCopyAsset).setOnClickListener(v -> {
      try {
        InputStream in = getAssets().open("111.docx");
        FileOutputStream out = new FileOutputStream(getDestFIle());
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
          out.write(buffer, 0, lengthRead);
          out.flush();
        }
        in.close();
        out.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.tvOpenInApp:
        // 传入的地址不对可能会导致报错，还有一点很重要，如果内核没有初始化完成，不能调用，否则不能使用。
        PreviewAttachmentActivity.start(this, getDestFIle().getAbsolutePath());
        break;
      case R.id.tv5:
        QbSdk.openFileReader(
            this,
            "/sdcard/Android/data/com.jackfruit.tbsdemo/files/Documents/24049_预案公告.pdf",
            null,
            new ValueCallback<String>() {
              @Override
              public void onReceiveValue(String s) {}
            });
        break;
      case R.id.tv2:
        WebActivity.start(this);
        break;
      case R.id.tv3:
        Log.i("QbSdk", "是否可以加载X5内核: " + QbSdk.canLoadX5(this));
        // 重新下载
        // TbsDownloader.startDownload(this);
        // 如果可以加载X5内核，则直接重启，否则重置x5
        // 如果只是需要下载的话则调用TbsDownloader.startDownload(this);
        // 可能会有意想不到加载不了的问题，重启是最简单粗暴的方法。
        if (QbSdk.canLoadX5(this)) {
          rebootApp();
        } else {
          // 重置X5的配置
          QbSdk.reset(this);
          rebootApp();
        }
        break;
      default:
        break;
    }
  }

  private void rebootApp() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    finish();
    // 从操作系统中结束掉当前程序的进程
    android.os.Process.killProcess(android.os.Process.myPid());
    // 退出JVM(java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
    System.exit(0);
  }

  private void loadInfo() {
    StringBuilder sb = new StringBuilder();
    boolean x5DisabledSync = QbSdk.isX5DisabledSync(this);
    if (x5DisabledSync) {
      sb.append("X5内核已禁用");
    } else {
      boolean tbsInstalling = QbSdk.getTBSInstalling();
      if (tbsInstalling) {
        sb.append("X5内核正在安装");
      }
    }

    tvInfo.setText(sb.toString());
  }

  private File getDestFIle() {
    return new File(ContextCompat.getDataDir(this), "test.docx");
  }
}
