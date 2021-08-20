package com.logAndReg.services;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.logAndReg.models.LoginUser;
import com.logAndReg.models.User;
import com.logAndReg.repositories.UserRepository;

@Service
public class UserService {
	@Autowired
    private UserRepository userRepo;
    

    // handles validations that we need to perform to create/register a new user
    public User register(User newUser, BindingResult result) {
    		//make sure an email that is not already taken
        if(this.userRepo.findByEmail(newUser.getEmail()).isPresent()) {
            result.rejectValue("email", "Unique", "This email is already in use!");
        }
        
        // password and confirm password match
        if(!newUser.getPassword().equals(newUser.getConfirm())) {
            result.rejectValue("confirm", "Matches", "The Confirm Password must match Password!");
        }
        
        
        if(result.hasErrors()) {
            return null;
        } else {
        		//if the form is filled out properly password will hash
            String hashed = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
            newUser.setPassword(hashed);
            return userRepo.save(newUser); //save the user object to database
        }
    }
    
    
    
    
    public User login(LoginUser newLogin, BindingResult result) {
        if(result.hasErrors()) {
            return null;
        }
        
        //finding the users email 
        Optional<User> potentialUser = this.userRepo.findByEmail(newLogin.getEmail());
        
        // create a validation message saying the email is not found
        if(!potentialUser.isPresent()) {
            result.rejectValue("email", "Unique", "Unknown email!");
            return null;
        }
        //get the user object that was found from the db who has that email from the login form
        User user = potentialUser.get();
        
        //use bcrypt to check if the user object from db has a password that matched the one from the form (the form object is represented by newLogin variable)
        if(!BCrypt.checkpw(newLogin.getPassword(), user.getPassword())) {
            result.rejectValue("password", "Matches", "Invalid Password!");//if the password does not match, create another validation error message
        }
        if(result.hasErrors()) {
            return null;
        } else {
            return user;
        }
    }
    
    
    public User findUser(Long id) {
    		return this.userRepo.findById(id).orElse(null);
    }
    
}