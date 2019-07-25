package com.bridgelabz.fundoo.user.controller;

//import javax.mail.MessagingException;
import javax.validation.Valid;

//import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.user.service.UserServices;
//import com.bridgelabz.fundoo.util.EmailUtil;
import com.bridgelabz.fundoo.util.EmailUtil;
import com.bridgelabz.fundoo.util.UserToken;

import io.swagger.annotations.ApiOperation;


@RequestMapping("/user")
@RestController
@PropertySource("classpath:message.properties")
@CrossOrigin(origins = "*", allowedHeaders = "*")
//@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = {"jwtToken"})
public class UserController {
	
	static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	public Environment environment;
	
	@Autowired
	private UserServices userServices;
	
	@PostMapping
	@ApiOperation(value = "Api of register for new user")
	public ResponseEntity<Response> register(@Valid @RequestBody UserDTO userDTO/*BindingResult bindingResult*/)
	{
		logger.info("userDTO:"+userDTO);
		logger.trace("User Registration");
		Response response = userServices.register(userDTO);
		return new ResponseEntity<Response>(response , HttpStatus.OK);
	}
	
	@PostMapping("/login")
	@ApiOperation(value = "Api of login for user")
	public ResponseEntity<Response> login(@Valid @RequestBody LoginDTO loginDTO)
	{
		logger.info("loginDTO:"+loginDTO);
		logger.trace("Login");
		//boolean flag = false;
		Response response = userServices.login(loginDTO);
		logger.info("Response:"+response);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
	
	@GetMapping("/{token}")
	@ApiOperation(value = "Api of email verification")
	public String emailValidation(@PathVariable String token)
	{
		logger.info("Token:"+token);
		String result = userServices.validateEmailId(token);
		return result;
	}

	@PostMapping("/forgotPassword")
	@ApiOperation(value = "Api of forgot password")
	public ResponseEntity<Response> forgotPassword(@RequestParam("email") String email)
	{
		logger.info("email:"+email);
		logger.trace("Forgot Password");
		Response response = userServices.forgotPassword(email);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
//		return new ResponseEntity<String>("Change your password", HttpStatus.OK);
	}
	
	@PutMapping("/{token}")
	@ApiOperation(value = "Api of reset password for password change")
	public ResponseEntity<Response> resetPassword(@RequestParam("password") String password, @PathVariable String token)
	{
		logger.info("token:"+token);
		logger.info("password:"+password);
		Response response = userServices.resetPassword(token, password);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
//		return new ResponseEntity<String>("Password reset",HttpStatus.OK);
	}
	
	@PostMapping("/imageUpload")
	@ApiOperation(value = "Api of save image")
	public ResponseEntity<Response> saveImage(@RequestHeader("token") String token,@RequestParam("file") MultipartFile file){
		logger.info("Token:"+token);
		Response response = userServices.saveProfileImage(token, file);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
	
	@GetMapping("/getImage/{token}")
	@ApiOperation(value = "Api of get image")
	public ResponseEntity<Resource> getProfilePicture(@PathVariable String token) {
		logger.info("Token:"+token);
		Resource response = userServices.getImage(token);
		return new ResponseEntity<Resource>(response, HttpStatus.OK);
	}
}















//@CrossOrigin(origins="http://localhost:4200")

//if(bindingResult.hasErrors()) 
//{
//	logger.error("Error in Binding The User Details"); 
//	throw new Exception("Data Not Matching"); 
//}

//@GetMapping("/resetPassword/{token}")
//public ResponseEntity<?> resetPassword(@PathVariable("token") String token,) throws Exception{
//	logger.info("password reset");
//	User user = userServices.resetPassword(token);
//	EmailUtil.send(user.getEmail(), "Change Password", .getBody(user, "reset page"));
//	return new ResponseEntity<String>("Redirect to new password set pqage",HttpStatus.OK);
//}

//@PostMapping("/resetPage/{token}")
//public ResponseEntity<?> resetPage(@PathVariable("token") String token, @RequestBody LoginDTO loginUser) throws Exception{
//	logger.info("Password reset page");
//	long userId = UserToken.tokenVerify(token);
//	User user = userRepository.findById(userId).get();
//	user.setPassword(passwordEncoder.encode(loginUser.getPassword()));
//	userRepository.save(user);
//	return new ResponseEntity<String>("Password Set Successfully",HttpStatus.OK);
//}

/*private String getBody(User user, String link) {
	return "192.168.0.84:8080/"+link+UserToken.generateToken(user.getId());
}*/
