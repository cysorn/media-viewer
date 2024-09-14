package media_viewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomProperties {

    @Value("${custom.workingLocationPath}")
    private String url;

    public String getWorkingLocationPath() {
        return url;
    }
}