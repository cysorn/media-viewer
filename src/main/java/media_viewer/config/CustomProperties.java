package media_viewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomProperties {

    @Value("${custom.workingLocationPath}")
    private String path;

    public String getWorkingLocationPath() {
        return path;
    }
}