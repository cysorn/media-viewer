package media_viewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan(basePackages = {"media_viewer"})
public class MediaViewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaViewerApplication.class, args);
	}
}
