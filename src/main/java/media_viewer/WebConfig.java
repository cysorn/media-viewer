package media_viewer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from a directory outside of 'static'
        registry.addResourceHandler("/media_files/**")
                .addResourceLocations("file:/E:/testing_media_viewer/media_files/");
        
        registry.addResourceHandler("/uncategorized/**")
        .addResourceLocations("file:/E:/testing_media_viewer/uncategorized/");
    }
}