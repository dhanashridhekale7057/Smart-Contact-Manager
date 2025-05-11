package com.smart.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	// Define the upload directory path - changing to an absolute path
	private static final String UPLOAD_DIR = System.getProperty("user.home") + "/smartcontact_images";
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//get the user using usernamne(Email)		
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		
		model.addAttribute("user",user);
		
	
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			Principal principal,HttpSession session) {
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// Set default image or empty string
			contact.setImage("");
			
			// Use helper method to add contact
			user.addContact(contact);
			
			this.userRepository.save(user);		
			
			System.out.println("DATA "+contact);
			System.out.println("Added to data base");
			
			//message success.......
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success") );
			
		} catch (Exception e) {		
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			//message error
			session.setAttribute("message", new Message("Some went wrong !! Try again..", "danger") );
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		//contact ki list ko bhejni hai
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		//currentPage-page
		//Contact Per page - 5
		Pageable pageable = PageRequest.of(page, 8);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	// Handler for deleting contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {
		try {
			System.out.println("DELETE REQUEST FOR CONTACT ID: " + cId);
			
			// Get current user
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			System.out.println("USER: " + user.getName() + " (ID: " + user.getId() + ")");
			
			// Check if contact exists and belongs to user
			Contact contact = this.contactRepository.findContactByIdAndUser(cId, user.getId());
			
			if(contact != null) {
				System.out.println("CONTACT FOUND: " + contact.getName() + " (ID: " + contact.getcId() + ")");
				
				try {
					// First method: delete directly by ID
					this.contactRepository.deleteById(cId);
					System.out.println("CONTACT DELETED SUCCESSFULLY VIA REPOSITORY");
				} catch (Exception e) {
					// Fallback method: use the user's contact collection
					user.getContacts().remove(contact);
					this.userRepository.save(user);
					System.out.println("CONTACT DELETED SUCCESSFULLY VIA USER RELATIONSHIP");
				}
				
				session.setAttribute("message", new Message("Contact deleted successfully", "success"));
			} else {
				System.out.println("CONTACT NOT FOUND WITH ID: " + cId + " FOR USER ID: " + user.getId());
				session.setAttribute("message", new Message("Contact not found or you don't have permission to delete it", "danger"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR IN DELETE: " + e.getMessage());
			session.setAttribute("message", new Message("Something went wrong: " + e.getMessage(), "danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	// Handler for showing update form
	@GetMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		model.addAttribute("title", "Update Contact");
		
		try {
			System.out.println("UPDATE FORM REQUEST FOR CONTACT ID: " + cId);
			
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			System.out.println("USER: " + user.getName() + " (ID: " + user.getId() + ")");
			
			// Find the contact by ID and user ID (for security)
			Contact contact = this.contactRepository.findContactByIdAndUser(cId, user.getId());
			
			if (contact != null) {
				System.out.println("CONTACT FOUND FOR UPDATE: " + contact.getName() + " (ID: " + contact.getcId() + ")");
				model.addAttribute("contact", contact);
				return "normal/update_contact_form";
			} else {
				System.out.println("CONTACT NOT FOUND FOR UPDATE WITH ID: " + cId);
				model.addAttribute("message", new Message("Contact not found", "danger"));
				return "redirect:/user/show-contacts/0";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR IN UPDATE FORM: " + e.getMessage());
			model.addAttribute("message", new Message("Something went wrong: " + e.getMessage(), "danger"));
			return "redirect:/user/show-contacts/0";
		}
	}
	
	// Handler for processing update form
	@PostMapping("/process-update")
	public String updateContact(
			@ModelAttribute Contact contact,
			Principal principal,
			HttpSession session) {
		
		try {
			System.out.println("PROCESS UPDATE REQUEST FOR CONTACT ID: " + contact.getcId());
			
			// Get old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			System.out.println("OLD CONTACT DETAILS FOUND: " + oldContactDetail.getName());
			
			// Keep the existing image (or empty string)
			contact.setImage(oldContactDetail.getImage());
			
			// Get current user
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			System.out.println("USER FOR UPDATE: " + user.getName() + " (ID: " + user.getId() + ")");
			
			// Set user to contact to maintain the relationship
			contact.setUser(user);
			
			// Direct update using repository - more reliable
			Contact updatedContact = this.contactRepository.save(contact);
			System.out.println("CONTACT UPDATED SUCCESSFULLY: " + updatedContact.getName());
			
			session.setAttribute("message", new Message("Contact updated successfully", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR IN PROCESS UPDATE: " + e.getMessage());
			session.setAttribute("message", new Message("Error updating contact: " + e.getMessage(), "danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	// User profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			// If user doesn't have an image, set default
			if (user.getImageUrl() == null || user.getImageUrl().isEmpty()) {
				user.setImageUrl("default.png");
			}
			
			model.addAttribute("title", "User Profile");
			model.addAttribute("user", user);  // Explicitly add user to model
			return "normal/user_profile";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("title", "Error");
			model.addAttribute("errorMessage", "Error loading profile: " + e.getMessage());
			return "error";
		}
	}
	
	@PostMapping("/update-profile-image")
	public String updateProfileImage(
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) {
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// Process file
			if (!file.isEmpty()) {
				// Create directory if it doesn't exist
				File uploadDirFile = new File(UPLOAD_DIR);
				if (!uploadDirFile.exists()) {
					uploadDirFile.mkdirs();
					System.out.println("Created upload directory: " + uploadDirFile.getAbsolutePath());
				}
				
				// Delete old file if exists
				if (user.getImageUrl() != null && !user.getImageUrl().equals("default.png")) {
					File oldFile = new File(UPLOAD_DIR + File.separator + user.getImageUrl());
					if (oldFile.exists()) {
						oldFile.delete();
					}
				}
				
				// Save new file
				String originalFilename = file.getOriginalFilename();
				String randomID = UUID.randomUUID().toString();
				String newFileName = randomID.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));
				
				// Upload file - create absolute path
				File saveFile = new File(UPLOAD_DIR + File.separator + newFileName);
				
				// Use FileOutputStream to ensure reliable file write
				try (FileOutputStream fos = new FileOutputStream(saveFile)) {
					fos.write(file.getBytes());
				}
				
				// Update user
				user.setImageUrl(newFileName);
				this.userRepository.save(user);
				
				System.out.println("Profile image saved successfully at: " + saveFile.getAbsolutePath());
				session.setAttribute("message", new Message("Profile picture updated successfully!", "alert-success"));
			} else {
				session.setAttribute("message", new Message("Please select a file to upload", "alert-warning"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error uploading profile image: " + e.getMessage(), "alert-danger"));
		}
		
		return "redirect:/user/profile";
	}
}
