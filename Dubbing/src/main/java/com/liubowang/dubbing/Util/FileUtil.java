package com.liubowang.dubbing.Util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URISyntaxException;

public class FileUtil {
    public static final String LOG_TAG = "LOGCAT";
//    public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
    public static String packageFilesDirectory = null;
    public static String storagePath = null;
    private static String mDefaultFolder = "Dubbing";



    public static void setDefaultFolder(String defaultFolder) {
        mDefaultFolder = defaultFolder;
    }

    public static String getPath() {
        return getPath(null);
    }

    public static String getCurrentTimeMillisPath(String suffix){
        String tmp = getTmpPath();
        String newPath = tmp + System.currentTimeMillis() + "." + suffix;
        return newPath;
    }

    public static String getTmpPath(){
        String tmp = getPath()+"/temp/";
        File f = new File(tmp);
        if (!f.exists()){
            f.mkdirs();
        }
        return tmp;
    }

    public static void removeTmpFile(){
        String tmp = getTmpPath();
        deleteDir(tmp);
        String pcm = getPath()+"/pcm/";
        deleteDir(pcm);
        String wav = getPath()+"/wav/";
        deleteDir(wav);
    }

    //删除文件夹和文件夹里面的文件
    private static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        if (dir.exists()){
            deleteDirWihtFile(dir);
        }
    }
    private static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()){
            return;
        }
        if (dir.listFiles() == null){
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static String getVideoResultPath(String name, String suffix){
        String resultPath = getVideoResultPath();
        String newPath = resultPath + name + "." + suffix;
        return newPath;
    }

    public static String getVideoResultPath(){
        String resultPath = getPath()+"/Videos/";
        File f = new File(resultPath);
        if (!f.exists()){
            f.mkdirs();
        }
        return resultPath;
    }

    public static String getPath(String name,String suffix){
        String rootPath = getPath();
        if (suffix.contains(".")){
            suffix.replace(".","");
        }
        if (name == null){
            name = System.currentTimeMillis() + "";
        }
        String newPath = rootPath + "/" + name + "." + suffix;
        return newPath;
    }
    public static String getPath(Context context) {
        if(storagePath == null) {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            storagePath = externalStorageDirectory.getAbsolutePath() + "/" + mDefaultFolder;
            File file = new File(storagePath);
            if(!file.exists()) {
                if(!file.mkdirs()) {
                    storagePath = getPathInPackage(context, true);
                }
            }
        }
        return storagePath;
    }

    public static String getSuffix( String path){
        if (path == null) return null;
        File f = new File(path);
        String fileName = f.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        return suffix;
    }

    public static String getPathInPackage(Context context, boolean grantPermissions) {
        if(context == null || packageFilesDirectory != null)
            return packageFilesDirectory;
        //手机不存在sdcard, 需要使用 data/data/name.of.package/files 目录
        String path = context.getFilesDir() + "/" + mDefaultFolder;
        File file = new File(path);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                Log.e(LOG_TAG, "在pakage目录创建CGE临时目录失败!");
                return null;
            }
            if(grantPermissions) {
                //设置隐藏目录权限.
                if (file.setExecutable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is executable");
                }
                if (file.setReadable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is readable");
                }
                if (file.setWritable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is writable");
                }
            }
        }

        packageFilesDirectory = path;
        return packageFilesDirectory;
    }

    public static void saveTextContent(String text, String filename) {
        Log.i(LOG_TAG, "Saving text : " + filename);
        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            fileout.write(text.getBytes());
            fileout.flush();
            fileout.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
        }
    }

    public static String getTextContent(String filename) {
        Log.i(LOG_TAG, "Reading text : " + filename);
        if(filename == null) {
            return null;
        }
        String content = "";
        byte[] buffer = new byte[256]; //Create cache for reading.
        try {
            FileInputStream filein = new FileInputStream(filename);
            int len;
            while(true) {
                len = filein.read(buffer);
                if(len <= 0)
                    break;
                content += new String(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
            return null;
        }
        return content;
    }

    /**
     * 创建txt文件
     */
    public static void createTxtFile(String _msg){
        File dir = new File(storagePath+"/list4concat.txt");
        if (!dir.exists()) {
            try {
                //在指定的文件夹中创建文件
                dir.createNewFile();
            } catch (Exception e) {
            }
        }
        writeTxtFile(_msg);
    }
//
    /**
     * 写入txt文件
     * @param str
     */
    public static void writeTxtFile(String str){
//        str="file ./split.mp4 \nfile ./split1.mp4";
        FileWriter fw = null;
        BufferedWriter bw = null;
        String datetime = "";
        try {
            fw = new FileWriter(storagePath+"/list4concat.txt", false);//
            // 创建FileWriter对象，用来写入字符流
            bw = new BufferedWriter(fw); // 将缓冲对文件的输出
            bw.write(str); // 写入文件
            bw.newLine();
            bw.flush(); // 刷新该流的缓冲
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                bw.close();
                fw.close();
            } catch (Exception e1) {
            }
        }
    }

    /**
     * 获取文件绝对路径,4.4系统之前和之后返回的结构不同，所以要不同处理。这里要用到DocumentProvider
     * @param context
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {//4.4以后
            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {//4.2.2以后4.4以前，判断协议是以content://开头还是file://开头
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {//4.2.2以前
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
