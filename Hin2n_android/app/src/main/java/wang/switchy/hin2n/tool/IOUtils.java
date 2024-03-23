package wang.switchy.hin2n.tool;

import android.text.TextUtils;
import android.util.Log;
import com.chad.library.adapter.base.BaseQuickAdapter;
import java.io.*;

public class IOUtils {

    public static String readTxt(String txtPath) {
        File file = new File(txtPath);
        if (file.isFile() && file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String readTxtLimit(String txtPath, int size) {
        File file = new File(txtPath);
        if (file.exists() && file.isFile()) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                long length = randomAccessFile.length();
                long start = Math.max(0, length - size);
                randomAccessFile.seek(start);

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = randomAccessFile.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static RandomAccessFile randomAccessFile;
    private static boolean isNeedShow;

    public static void readTxtLimits(boolean showLog, String txtPath, int size, BaseQuickAdapter mAdapter) {
        isNeedShow = showLog;
        try {
            File file = new File(txtPath);
            if (file.exists() && file.isFile()) {
                if (randomAccessFile == null || isNeedShow) {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    long length = randomAccessFile.length();
                    long start = Math.max(0, length - size);
                    randomAccessFile.seek(start);

                    String line;
                    while (isNeedShow && (line = randomAccessFile.readLine()) != null) {
                        if (!TextUtils.isEmpty(line)) {
                            ThreadUtils.mainThreadExecutor(() -> {
                                if (mAdapter.getData().size() > 200) {
                                    mAdapter.getData().remove(0);
                                    mAdapter.notifyItemRemoved(0);
                                }
                                mAdapter.getData().add(line);
                                int last = mAdapter.getData().size() - 1;
                                mAdapter.notifyItemChanged(last);
                                mAdapter.getRecyclerView().scrollToPosition(last);
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
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
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
