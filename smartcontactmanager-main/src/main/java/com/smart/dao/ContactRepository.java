package com.smart.dao;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	//pagination...
	
	@Query("from Contact as c where c.user.id =:userId")
	//currentPage-page
	//Contact Per page - 5
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pePageable);
	
	// Find contact by ID and user ID (for security to ensure the contact belongs to the current user)
	@Query("from Contact as c where c.cId =:contactId and c.user.id =:userId")
	public Contact findContactByIdAndUser(@Param("contactId") int contactId, @Param("userId") int userId);
}
