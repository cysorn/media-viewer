package media_viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import media_viewer.ProgramSetup;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	ProgramSetup pSetup;
	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from a directory outside of 'static'
        registry.addResourceHandler("/media_files/**")
                .addResourceLocations("file:/" + pSetup.getAbsoluteMediaFilesLocation());
        
        registry.addResourceHandler("/uncategorized/**")
        .addResourceLocations("file:/" + pSetup.getAbsoluteUncategorizedLocation());
    }
}