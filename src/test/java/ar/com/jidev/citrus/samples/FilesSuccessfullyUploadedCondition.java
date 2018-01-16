package ar.com.jidev.citrus.samples;

import com.consol.citrus.condition.Condition;
import com.consol.citrus.context.TestContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FilesSuccessfullyUploadedCondition implements Condition {
    private Map<String, Long> filesSizes = new HashMap<>();
    private final String path;

    public FilesSuccessfullyUploadedCondition(final String path) {
        this.path = path;
    }

    @Override
    public String getName () {
        return "Check file uploaded to FTP";
    }

    @Override
    public boolean isSatisfied (TestContext testContext){
        File[] receiveFiles = new File(path).listFiles();

        boolean anyFileChangedInLastInterval = false;

        if (receiveFiles.length == 0) {
            // we are expecting at least one file
            return false;
        } else {
            for(int i = 0; i < receiveFiles.length; i++){
                String fileName = receiveFiles[i].getName();
                Long currentSize = receiveFiles[i].length();
                Long previousSize = filesSizes.get(fileName);

                boolean fileChangedInLastInterval = null == previousSize || !currentSize.equals(previousSize);

                if (fileChangedInLastInterval) {
                    filesSizes.put(fileName, currentSize);
                    anyFileChangedInLastInterval = true;
                }
            }
        }

        return !anyFileChangedInLastInterval;
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
