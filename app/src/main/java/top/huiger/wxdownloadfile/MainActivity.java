package top.huiger.wxdownloadfile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * <pre>
 *  Author : huiGer
 *  Time   : 2019-2-23 15:09:49
 *  Email  : zhihuiemail@163.com
 *  Desc   :
 */
public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;

    private List<File> mList;
    private BaseQuickAdapter<File, BaseViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

        PermissionUtils.getPermission(this, new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                getFiles();
            }
        }, Permission.READ_EXTERNAL_STORAGE);

        Log.d("msg", "MainActivity -> onCreate: " + FileUtil.getInnerSDCardPath());


    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        initRecycler();


    }

    private void initListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFiles();
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                File file = mList.get(position);
                if(file.getName().contains(".apk")) {
                    checkIsAndroidO(file);
                }else {
                    openFile(file);
                }
            }
        });
    }


    /**
     * 处理 apk
     * 8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO(File file) {
        AndPermission.with(this)
                .install()
                .file(file)
                .rationale(new Rationale<File>() {
                    @Override
                    public void showRationale(Context context, File data, RequestExecutor executor) {
                        showPermissionDialog(executor);
                    }
                })
                .start();
    }

    private void showPermissionDialog(final RequestExecutor executor) {
        new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("您未授予应用安装权限")
                    .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.execute();
                        }
                    })
                    .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.cancel();
                        }
                    })
                    .show();
    }

    private void getFiles() {
        File path = new File(FileUtil.getInnerSDCardPath() + "/tencent/MicroMsg/Download");

        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                Log.d("msg", "MainActivity -> onCreate: " + file.getName());
            }
            if (!mList.isEmpty()) mList.clear();
            mList.addAll(Arrays.asList(files));
            Collections.sort(mList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return (int) (o2.lastModified() - o1.lastModified());
                }
            });
            mAdapter.notifyDataSetChanged();
        }

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void initRecycler() {

        mList = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BaseQuickAdapter<File, BaseViewHolder>(
                R.layout.adapter_item, mList
        ) {
            @Override
            protected void convert(BaseViewHolder helper, File item) {
                helper.setText(R.id.tv, item.getName())
                        .setText(R.id.tv_time, setFormatTime(item.lastModified()));
            }

        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        intent.setDataAndType(uri, getMIMEType(file));
        startActivity(intent);
    }

    private String setFormatTime(long time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        if (fName.endsWith(".1")) {
            fName = fName.replace(".1", "");
        }
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex + 1, fName.length()).toLowerCase(Locale.CHINA);
        if (end.equals("")) return type;
        HashMap<String, String> map = MyMimeMap.getMimeMapAll();
        if (!TextUtils.isEmpty(end) && map.keySet().contains(end)) {
            type = map.get(end);
        }
        return type;
    }
}
