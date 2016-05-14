package edu.perphy.enger.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/18 0018.
 * 文件操作工具类
 */
public class FileUtils {
    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean createDir(File path) {
        if (isExternalStorageWritable()) {
            // 获取词典文件的父目录
            if (DEBUG) Log.i(TAG, "FileUtils.createDir: " + path.toString());
            path.mkdirs();
        } else {
            Log.e(TAG, "FileUtils.createDir: External storage is not writable", null);
            return false;
        }
        return true;
    }

    public static boolean createDir(String path) {
        return createDir(new File(path));
    }

    public static boolean isFileExists(File path, String name) {
        return new File(path, name).exists();
    }

    public static boolean isFileExists(String path, String name) {
        return isFileExists(new File(path), name);
    }

    /**
     * 输出文本文件到json文件
     *
     * @param data 要保存的文本
     * @param path 保存的目的路径
     * @param name 保存的文件名
     */
    public static boolean save2json(String data, File path, String name) {
        // 创建笔记的根目录
        if (createDir(path)) {
            //notice 覆盖已有文件
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(path, name), false), "UTF-8")) {
                osw.write(data);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "FileUtils.save2json: err", e);
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean save2json(String data, String path, String name) {
        return save2json(data, new File(path), name);
    }

    /**
     * 从json文件中获取文本数据
     *
     * @param is 源输入流
     * @return 文本数据， null如果出错
     */
    public static String get5json(InputStream is) {
        //notice 只读取第一个文件
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "FileUtils.get5json: err", e);
            e.printStackTrace();
            return null;
        }
    }

    public static String get5json(File src) {
        try {
            return get5json(new FileInputStream(src));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileUtils.get5json: err", e);
            e.printStackTrace();
            return null;
        }
    }

    public static String get5json(String src) {
        return get5json(new File(src));
    }


    public static int bytes2Int(byte[] src) {
        return ((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF);
    }

    /**
     * 文件过滤器<br>
     * postfix: 后缀<br>
     * shallow: 是否浅层（1层）搜索
     */
    public static class PostfixFileFilter implements FileFilter {
        private String postfix;
        private boolean shallow;

        public PostfixFileFilter(String postfix) {
            this(postfix, false);
        }

        public PostfixFileFilter(String postfix, boolean shallow) {
            this.postfix = postfix;
            this.shallow = shallow;
        }

        @Override
        public boolean accept(File pathname) {
            if (shallow) {
                return pathname.getName().endsWith(postfix);
            } else {
                return pathname.isDirectory() || pathname.getName().endsWith(postfix);
            }
        }
    }
}
