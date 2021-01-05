package wang.switchy.hin2n.tool;


import java.io.*;

public class IOUtils {

    public static String readTxt(String txtPath){
        File file = new File(txtPath);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String text = null;
                while ((text = bufferedReader.readLine()) != null){
                    stringBuilder.append(text);
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            }catch (Exception e) {
                e.printStackTrace();
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
}
