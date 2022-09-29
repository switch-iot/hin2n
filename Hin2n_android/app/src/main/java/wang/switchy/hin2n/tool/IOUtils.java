package wang.switchy.hin2n.tool;


import android.text.TextUtils;
import android.util.Log;
import com.chad.library.adapter.base.BaseQuickAdapter;
import java.io.*;

public class IOUtils {

    public static String readTxt(String txtPath) {
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                fileInputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String text = null;
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuilder.append(text);
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(fileInputStream);
                close(inputStreamReader);
                close(bufferedReader);
            }
        }
        return "";
    }

    public static String readTxtLimit(String txtPath, int size) {
        File file = new File(txtPath);
        if (file.exists() && file.isFile()) {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
                long length = randomAccessFile.length();
                long start = 0;
                if (length > size) {
                    start = length - size;
                }
                randomAccessFile.seek(start);
                StringBuilder stringBuilder = new StringBuilder();
                String text = null;
                while ((text = randomAccessFile.readLine()) != null) {
                    stringBuilder.append(text);
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(randomAccessFile);
            }
        }
        return "";
    }

    private volatile static RandomAccessFile randomAccessFile = null;
    private volatile static boolean isNeedShow = false;
    //isNeedShow = true 当第一个线程在运行while时，之后的线程进来只会重置线程共享的RandomAccessFile状态;
    //isNeedShow = false 第一个线程结束while,之后的线程不走while。关闭RandomAccessFile
    public static void readTxtLimits(boolean showLog, String txtPath, int size, BaseQuickAdapter mAdapter) {
//        Log.d("readTxtLimits", Thread.currentThread() + "");
        isNeedShow = showLog;
        try {
            File file = new File(txtPath);
            if (file.exists() && file.isFile()) {
                if (randomAccessFile == null) {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    long length = randomAccessFile.length();
                    long start = 0;
                    if (length > size) {
                        start = length - size;
                    }
                    randomAccessFile.seek(start);
                    String text = null;
                    while (isNeedShow) {
//                        Log.d("readTxtLimits-1", Thread.currentThread() + "");
                        text = randomAccessFile.readLine();
                        if (!TextUtils.isEmpty(text)) {
                            String finalText = text;
                            ThreadUtils.mainThreadExecutor(new Runnable() {
                                @Override
                                public void run() {
                                    if (mAdapter.getData().size() > 200) {
                                        mAdapter.getData().remove(0);
                                        mAdapter.notifyItemRemoved(0);
                                    }
                                    mAdapter.getData().add(finalText);
                                    int last = mAdapter.getData().size() - 1;
                                    mAdapter.notifyItemChanged(last);
                                    mAdapter.getRecyclerView().scrollToPosition(last);
                                }
                            });
                        }
                    }
                } else if (isNeedShow) {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    long length = randomAccessFile.length();
                    long start = 0;
                    if (length > size) {
                        start = length - size;
                    }
                    randomAccessFile.seek(start);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!isNeedShow) {
                close(randomAccessFile);
                randomAccessFile = null;
            }
        }
    }

    public static boolean clearLogTxt(String txtPath) {
        File file = new File(txtPath);
        File fileBak = new File(txtPath + ".bak");
        if (file.exists()) {
            if (fileBak.exists()) {
                fileBak.delete();
            }
            try {
                file.renameTo(fileBak);
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void close(Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
