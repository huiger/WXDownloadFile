package top.huiger.wxdownloadfile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.huige.library.utils.ToastUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import java.util.List;

/**
 * <pre>
 *  Author : huiGer
 *  Time   : 2019/2/23 0023 下午 05:47.
 *  Email  : zhihuiemail@163.com
 *  Desc   :
 * </pre>
 */
public class PermissionUtils {

    /**
     * 获取权限
     *
     * @param ctx
     * @param granted
     * @param permissions
     */
    public static void getPermission(final Context ctx, Action<List<String>> granted, final String... permissions) {
        AndPermission.with(ctx)
                .runtime()
                .permission(permissions)
                .onGranted(granted)
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (AndPermission.hasAlwaysDeniedPermission(ctx, data)) {
                            showSettingDialog(ctx, data);
                        }
                    }
                })
                .start();
    }

    /**
     * Display setting dialog.
     */
    private static void showSettingDialog(final Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission(context);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private static void setPermission(final Context ctx) {
        AndPermission.with(ctx)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        ToastUtils.showToast("授权失败!");
                    }
                })
                .start();
    }
}
