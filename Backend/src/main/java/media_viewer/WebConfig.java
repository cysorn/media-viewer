package media_viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import media_viewer.file_system.FileSystem;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	FileSystem fileSystem;
	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	
        // Serve files from a directory outside of 'static'
        registry.addResourceHandler("/media_files/**")
                .addResourceLocations("file:/" + fileSystem.getAbsoluteMediaFilesLocation());
        
        registry.addResourceHandler("/uncategorized/**")
        		.addResourceLocations("file:/" + fileSystem.getAbsoluteUncategorizedLocation());
    }
}