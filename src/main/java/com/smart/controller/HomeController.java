package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import javax.servlet.http.Cookie;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// Static path for image storage
	private static final String UPLOAD_DIR = System.getProperty("user.home") + "/smartcontact_images";

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		try {
			model.addAttribute("title", "About - Smart Contact Manager");
			return "about";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("title", "Error");
			model.addAttribute("errorMessage", "Error loading about page: " + e.getMessage());
			model.addAttribute("errorTitle", "Error");
			return "error";
		}
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session, HttpServletRequest request) {

		try {
			// Clear any existing user data in session
			session.removeAttribute("user");
			
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("ERROR " + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement " + agreement);
			System.out.println("USER " + user);

			User result = this.userRepository.save(user);

			// Create a brand new empty User object for the form
			model.addAttribute("user", new User());
			
			// Clear session except for the success message
			Enumeration<String> attributeNames = session.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String attr = attributeNames.nextElement();
				if (!attr.equals("message")) {
					session.removeAttribute(attr);
				}
			}

			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			// Handle cookies to prevent form data persistence
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().startsWith("form_")) {
						cookie.setMaxAge(0);
						cookie.setValue("");
						cookie.setPath("/");
					}
				}
			}
			
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}

	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login";
	}

	// Handler to serve profile images
	@GetMapping("/profile-image/{imageName}")
	public ResponseEntity<byte[]> getProfileImage(@PathVariable("imageName") String imageName) throws IOException {
		try {
			byte[] imageBytes;
			String contentType;
			
			// First try loading from file system (absolute path)
			File imgFile = new File(UPLOAD_DIR + File.separator + imageName);
			
			if (imgFile.exists()) {
				// Load from file system
				imageBytes = Files.readAllBytes(imgFile.toPath());
				contentType = determineContentType(imageName);
			} else {
				// Try loading from resources
				ClassPathResource resource = new ClassPathResource("static/img/" + imageName);
				if (resource.exists()) {
					imageBytes = Files.readAllBytes(Paths.get(resource.getURI()));
					contentType = determineContentType(imageName);
				} else {
					// Fall back to default image
					ClassPathResource defaultResource = new ClassPathResource("static/img/default.png");
					imageBytes = Files.readAllBytes(Paths.get(defaultResource.getURI()));
					contentType = "image/png";
				}
			}
			
			// Set the appropriate content type
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(contentType));
			
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// Determine content type based on file extension
	private String determineContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		switch (extension) {
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			default:
				return "image/jpeg";  // Default to JPEG
		}
	}

	// Handler for logout
	@GetMapping("/logout-success")
	public String handleLogout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		// Get the security context authentication
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// If authenticated, perform logout
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		
		// Invalidate session
		if (session != null) {
			// Remove attributes individually
			Enumeration<String> attributeNames = session.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				session.removeAttribute(attributeNames.nextElement());
			}
			// Finally invalidate the session
			session.invalidate();
		}
		
		// Clear cookies
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookie.setValue("");
				cookie.setPath("/");
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}
		
		return "redirect:/signin?logout=true";
	}
}
