package wang.switchy.hin2n.tool;


import java.io.*;

public class IOUtils {

    public static String readTxt(String txtPath){
        File file = new File(txtPath);
        if(file.isFile() && file.exists()){
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                fileInputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String text = null;
                while ((text = bufferedReader.readLine()) != null){
                    stringBuilder.append(text);
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                close(fileInputStream);
                close(inputStreamReader);
                close(bufferedReader);
            }
        }
        return "";
    }

    public static boolean clearLogTxt(String txtPath){
        File file = new File(txtPath);
        File fileBak = new File(txtPath+".bak");
        if(file.exists()){
            if(fileBak.exists()){
                fileBak.delete();
            }
            try {
                file.renameTo(fileBak);
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void close(Closeable closeable){
        try {
            if(null != closeable){
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
