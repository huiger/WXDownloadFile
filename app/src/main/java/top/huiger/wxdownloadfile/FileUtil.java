package top.huiger.wxdownloadfile;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <pre>
 *  Author : huiGer
 *  Time   : 2018/11/22 0022 下午 06:51.
 *  Email  : zhihuiemail@163.com
 *  Desc   : 文件操作
 * </pre>
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {

    /**
     * 创建目录
     *
     * @param dirName 目录名称
     * @return flag 0 已经存在，1 创建成功， -1 创建失败
     */
    public static int createDir(String dirName) {

        int flag;
        String directory = getInnerSDCardPath() + "/" + dirName;
        File file = new File(directory);

        if (!file.exists()) {
            file.mkdirs();
            flag = 1;
        } else {
            flag = 0;
        }

        return flag;
    }

    /**
     * 创建文件
     *
     * @param dirPath  目录路径
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @return flag 0 已经存在，1 创建成功， -1 创建失败
     */
    public static int createFile(String dirPath, String fileName, String fileType) {

        int flag = 0;
        File file = new File(dirPath + "/" + fileName + fileType);
        if (!file.exists()) {
            try {
                file.createNewFile();
                flag = 1;
            } catch (IOException e) {
                flag = -1;
                e.printStackTrace();
            }
        }

        return flag;
    }

    /**
     * 是否存在该文件
     *
     * @param filePath 文件路径
     * @return boolean false不存在，true存在
     */
    private boolean isExistFile(String filePath) {

        boolean flag = false;
        File file = new File(filePath);
        if (file.exists()) {
            flag = true;
        }

        return flag;
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file          要删除的根目录
     * @param deleteDirFlag true 删除目录，false 不删除目录
     */
    public static void deleteFile(File file, boolean deleteDirFlag) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                if (deleteDirFlag) {
                    file.delete();
                }
                return;
            }
            for (File f : childFile) {
                deleteFile(f, deleteDirFlag);
            }

            file.delete();
        }
    }

    /**
     * 写文件
     *
     * @param fileName  文件名
     * @param write_str 信息
     * @param flag      是否为追加标记
     * @throws IOException
     */
    public static void writeSDFile(String fileName, String write_str, boolean flag) throws IOException {

        File file = new File(fileName);

        FileOutputStream fos = new FileOutputStream(file, flag);

        byte[] bytes = write_str.getBytes();

        fos.write(bytes);

        fos.close();
    }

    /**
     * 获取内置SD卡路径
     *
     * @return path
     */
    public static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


}
