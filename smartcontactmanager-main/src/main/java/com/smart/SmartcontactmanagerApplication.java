package com.smart;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartcontactmanagerApplication {

	public static void main(String[] args) {
		// Create upload directories if they don't exist
		createUploadDirectories();
		SpringApplication.run(SmartcontactmanagerApplication.class, args);
	}
	
	private static void createUploadDirectories() {
		try {
			// Create the local resource directory
			File localUploadDir = new File("src/main/resources/static/img/");
			if (!localUploadDir.exists()) {
				localUploadDir.mkdirs();
				System.out.println("Created local upload directory: " + localUploadDir.getAbsolutePath());
			}
			
			// Create the external storage directory
			String externalPath = System.getProperty("user.home") + "/smartcontact_images";
			File externalUploadDir = new File(externalPath);
			if (!externalUploadDir.exists()) {
				externalUploadDir.mkdirs();
				System.out.println("Created external upload directory: " + externalUploadDir.getAbsolutePath());
			}
			
			// Copy default image if needed
			File defaultImgResource = new File("src/main/resources/static/img/default.png");
			File externalDefaultImg = new File(externalPath + "/default.png");
			
			if (!defaultImgResource.exists() || !externalDefaultImg.exists()) {
				System.out.println("Default image creation might be needed. Please ensure default.png exists in both directories.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error creating upload directories: " + e.getMessage());
		}
	}
}
