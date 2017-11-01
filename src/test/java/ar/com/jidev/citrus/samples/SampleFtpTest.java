package ar.com.jidev.citrus.samples;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.condition.Condition;
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

        Condition waitUntilFileIsUploaded =  new WaitUntilFileIsUploadedCondition(ftpHomeDirectory);
        waitFor().condition(waitUntilFileIsUploaded).seconds(1200L).interval(5000L);

        // don't care about connection related messages
        receive(ftpServer);
        receive(ftpServer);
        receive(ftpServer);
        receive(ftpServer);
        receive(ftpServer);

        // assert content is as expected
        receive(ftpServer).header("citrus_ftp_command", FTPCmd.STOR.getCommand());
        receive(fileEndpoint)
            .header("file_originalFile", "@contains('/test.txt')@")
            .payload(new ClassPathResource("ar/com/jidev/citrus/samples/expected/test.txt"));
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

    private static class WaitUntilFileIsUploadedCondition implements Condition {
        private String pollingDirectory;

        public WaitUntilFileIsUploadedCondition(String pollingDirectory) {
            this.pollingDirectory = pollingDirectory;
        }

        @Override
        public String getName () {
            return "Check files on FTP";
        }

        @Override
        public boolean isSatisfied (TestContext testContext){
            return new File(pollingDirectory).listFiles().length != 0;
        }

        @Override
        public String getSuccessMessage (TestContext testContext){
            return "Files found in FTP!";
        }

        @Override
        public String getErrorMessage (TestContext testContext){
            return "No file was found in FTP";
        }
    }
}
