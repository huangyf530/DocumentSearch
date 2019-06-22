import com.baidu.aip.speech.AipSpeech;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
/**
 * Created by liuzhonghua on 2018/8/14.
 */
public class HyfListener {

    static final Logger logger = LoggerFactory.getLogger(HyfListener.class);
    /**
     * 音频文件频率8k转16k。必须要转，因为不转百度识别不出来，错误信息是音质太差
     * @param sourceFile
     * @return
     */
    public static File cover8xTo16x(File sourceFile){
        String targetPath = null;
        try {
            File ffmpegPath = new File("voice"); //存放ffmpeg程序的目录
            targetPath = sourceFile.getAbsolutePath().replaceAll(".wav" , "_16x.wav");
            // ffmpeg.exe -i source.wav -ar 16000 target.wav
            List<String> wavToPcm = new ArrayList<String>();
            wavToPcm.add(ffmpegPath.getAbsolutePath());
            wavToPcm.add("-i");
            wavToPcm.add(sourceFile.getAbsolutePath());
            wavToPcm.add("-ar");
            wavToPcm.add("16000");
            wavToPcm.add(targetPath);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(wavToPcm);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            logger.error("录音文件8k转化16k失败"+e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (StringUtils.isNotEmpty(targetPath)) {
            return new File(targetPath);
        }
        logger.error("传入的文件路径有误");
        return null;
    }

    public static String targetString(File sourcefile)
    {
        String ans = "";
        AipSpeech client = new AipSpeech("16608070", "EONvWoB4KstUv1Ij2jfpQ8MH", "xwnRykoIw8r5srtmonnpS6MkNUmoqALj");
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        JSONObject res = client.asr(sourcefile.getAbsolutePath(), "wav", 16000, null);
        System.out.println(res);

        return ans;
    }


    public static void main(String[] args) {
        File file = new File("/Users/gengwei/Desktop/录音.wav");
        File targetfile = cover8xTo16x(file);


        //targetString(targetfile);



    }
}