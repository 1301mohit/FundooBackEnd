package com.bridgelabz.fundoo.user.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.bridgelabz.fundoo.RabbitMq.MessageProducer;
import com.bridgelabz.fundoo.RabbitMq.RabbitMqBody;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.util.StatusUtil;
import com.bridgelabz.fundoo.util.UserToken;
import com.bridgelabz.fundoo.util.Utility;

@Service
@PropertySource("classpath:message.properties")
public class UserServiceImplimentation implements UserServices {
	
	static final Logger logger = LoggerFactory.getLogger(UserServiceImplimentation.class);
	
	@Autowired
	private Environment environment;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired 
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private RabbitMqBody rabbitMqBody;
	 
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private Response response;
	
	@Autowired
	private MessageServiceImpl messageServiceImpl;
	
	@Autowired
	private MessageProducer messageProcedure;
	
	private final Path fileLocation = Paths.get("/home/admin-122/FundooFile");

	
	
	
	/**
	 * 
	 * @param user
	 * @return
	 * @throws MessagingException 
	 * @throws Exception
	 */
	@Override
	public Response register(UserDTO userDTO)
	{	
		Optional<User> useravailable = userRepository.findByEmail(userDTO.getEmail());
		
		//To check user is available or not
		if(useravailable.isPresent())
		{
			throw new UserException(environment.getProperty("status.register.dublicateUser"));
		}
		
		//Copy user data userDTO to user class
		User user=modelMapper.map(userDTO, User.class);
		
		//set password in encrypted form
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		user.setRegisteredDate(LocalDate.now());
		
		//save user data in data base
	    user = userRepository.save(user);
	    
	    //Set the subject, emailid and activationlink in rabbitmqbody
	    rabbitMqBody.setSubject("Verify emailId");
	    rabbitMqBody.setToEmailId(user.getEmail());
	    String userActivationLink = Utility.getUrl(user.getUserId());
	    rabbitMqBody.setUrl(userActivationLink);
	    System.out.println("RabbitMqBody--------------------------->"+rabbitMqBody);
	    
	    //Send the rabbitMqbody in queue through message procedure Class
	    messageProcedure.sendMessage(rabbitMqBody);
	    
	    //EmailUtil.send(user.getEmail(), environment.getProperty("status.register.mailForRegistration"), getUrl(user.getuserId()));
	    //messageServiceImpl.sendEmail(user);
	    
	    Response response = StatusUtil.statusInfo(environment.getProperty("status.register.successful"), environment.getProperty("status.code.success"));
	    return response;
	    
	}
	
	@Override
	public Response login(LoginDTO loginuser)
	{
		Optional<User> userAvailable = userRepository.findByEmail(loginuser.getEmail());
		if(userAvailable.get().isIsverification())
		{
			if(userAvailable.isPresent() && passwordEncoder.matches(loginuser.getPassword(),userAvailable.get().getPassword())) 
			{ 
				String generateToken = UserToken.generateToken(userAvailable.get().getUserId());
				Response response = StatusUtil.tokenStatusInfo(environment.getProperty("status.login.successful"), environment.getProperty("status.code.success"), generateToken);
				return response; 
			} 
			else 
			{ 
				throw new UserException(environment.getProperty("status.login.unSuccessful")); 
			}
		}
		else
		{
			throw new UserException(environment.getProperty("status.email.verify"));
		}
	}

	@Override
	public String validateEmailId(String token)
	{
		Long id = UserToken.tokenVerify(token);
		User user = userRepository.findById(id).orElseThrow(() -> new UserException(environment.getProperty("status.email.user"))); 
		user.setIsverification(true);
		userRepository.save(user);
		return environment.getProperty("status.email.userVerify");
	}
	
	@Override
	public Response forgotPassword(String email)  
	{
		Optional<User> userAvailable = userRepository.findByEmail(email);
		if(userAvailable.isPresent()) 
		{
			User user = userAvailable.get();
			//EmailUtil.send(email, environment.getProperty("status.password.reset"), Utility.getBody(user, "user"));
			RabbitMqBody rabbitmqBody = new RabbitMqBody();
			rabbitmqBody.setToEmailId(user.getEmail());
			rabbitmqBody.setSubject("password recovery link");
			String url = Utility.getBody(user.getUserId(), "resetPassword");
			rabbitmqBody.setUrl(url);
			messageServiceImpl.sendEmail(rabbitmqBody);
			Response response = StatusUtil.statusInfo(environment.getProperty("status.password.successful"), environment.getProperty("status.code.success"));
			//response.setStatusCode(environment.getProperty("status.code.success"));
			//response.setStatusMessage(environment.getProperty("status.password.successful"));
			return response;
		}
		else 
		{
			throw new UserException(environment.getProperty("status.password.email"));
		}
	}
	
	@Override
	public Response resetPassword(String token, String password)
	{
			long userId = UserToken.tokenVerify(token);
			User user = userRepository.findById(userId).get();
			user.setPassword(passwordEncoder.encode(password));
			user.setAccountUpdateDate(LocalDate.now());
			userRepository.save(user);
			Response response = StatusUtil.statusInfo(environment.getProperty("status.password.resetpassword"), environment.getProperty("status.code.success"));
		//	response.setStatusCode(environment.getProperty("status.code.success"));
		//	response.setStatusMessage(environment.getProperty("status.password.resetpassword"));
			return response;
	}

	@Override
	public Response saveProfileImage(String token, MultipartFile file) {
		Long userId = UserToken.tokenVerify(token);
		User user = userRepository.findById(userId).get();
		UUID uuid = UUID.randomUUID();
		String uniqueId = uuid.toString();
		try {
			Files.copy(file.getInputStream(), fileLocation.resolve(uniqueId), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//throw new UserException(exception);
			e.printStackTrace();
			//throw new Exception(e);
		}
//		}catch(IOException e) {
//			e.printStackTrace();
//		}
		
		user.setProfileImage(uniqueId);
		userRepository.save(user);
		Response response = StatusUtil.statusInfo(environment.getProperty("status.save.profile"),  environment.getProperty("status.code.success"));
		return response;
	}
	
	@Override
	public Resource getImage(String token) {
		Long userId = UserToken.tokenVerify(token);
		User user = userRepository.findById(userId).get();
		
		//get image name from database
		Path imagePath = fileLocation.resolve(user.getProfileImage());
		
		//Creating url resource based on uri object
		Resource resource;
		try {
			resource = new UrlResource(imagePath.toUri());
			if(resource.exists() || resource.isReadable()) {
				return resource;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
}












//	@Override
//	public User resetPassword(String token) throws Exception {
//		Long id = UserToken.tokenVerify(token);
//		return userRepository.findById(id).get();
//	}
//	
//	public String getBody(User user, String link) throws Exception {
//		return "192.168.0.84:8080/"+link+UserToken.generateToken(user.getId());
//	}

