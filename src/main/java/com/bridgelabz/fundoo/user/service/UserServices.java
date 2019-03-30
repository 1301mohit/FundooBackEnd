package com.bridgelabz.fundoo.user.service;


import javax.mail.MessagingException;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;
import com.bridgelabz.fundoo.user.model.User;

public interface UserServices {
	
	public Response register(UserDTO userDTO) throws MessagingException, Exception;
	public Response login(LoginDTO loginuser) throws Exception;
	public String validateEmailId(String token) throws Exception; 
	public Response forgotPassword(String email) throws Exception;
	public Response resetPassword(String token, String password) throws Exception;
	public Response saveProfileImage(String token, MultipartFile file) throws Exception;
	public Resource getImage(String token) throws Exception;
	
}
