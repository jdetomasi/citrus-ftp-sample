package ar.com.jidev.citrus.samples;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.container.Assert;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.ftp.server.FtpServer;
import org.apache.commons.net.ftp.FTPCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import java.io.File;

@Test
public class SampleFtpTest extends TestNGCitrusTestDesigner {
    @Autowired
    private FtpServer ftpServer;
    @Autowired
    private ChannelEndpoint fileEndpoint;

    @Value("${ftpserver.user.anonymous.userpassword}")
    private String ftpUserPassword;
    @Value("${ftpserver.user.anonymous.homedirectory}")
    private String ftpHomeDirectory;

    @CitrusTest
    public void testFileContent() {
        // launch integration simulator...
        new Thread(new FtpIntegrationSimulator()).start();

        Condition waitUntilFileIsUploaded =  new FilesSuccessfullyUploadedCondition(ftpHomeDirectory);
        waitFor().condition(waitUntilFileIsUploaded).seconds(1200L).interval(5000L);

        receive(fileEndpoint)
            .header("file_originalFile", "@contains('/test.txt')@")
            .payload(new ClassPathResource("ar/com/jidev/citrus/samples/expected/test.txt"));

        // note: extra asserts can be done here using action dsl and TestNG (for example: if the filename is unknown)
    }

    private class FtpIntegrationSimulator implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(5000L);
                Runtime.getRuntime().exec("curl -T test-classes/ar/com/jidev/citrus/samples/expected/test.txt ftp://localhost:22222 --user anonymous:" + ftpUserPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
