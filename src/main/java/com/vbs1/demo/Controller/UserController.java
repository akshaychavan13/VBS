package com.vbs1.demo.Controller;

import com.vbs1.demo.dto.DisplayDTO;
import com.vbs1.demo.dto.LoginDto;
import com.vbs1.demo.dto.UpdateDto;
import com.vbs1.demo.models.History;
import com.vbs1.demo.models.Transaction;
import com.vbs1.demo.models.User;
import com.vbs1.demo.repositories.HistoryRepo;
import com.vbs1.demo.repositories.TransactionRepo;
import com.vbs1.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    TransactionRepo transactionRepo;
    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "Signup Successful";
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginDto u) {
        User user = userRepo.findByUsername(u.getUsername());
        if (user == null) {
            return "User not found";
        }
        if (!u.getPassword().equals(user.getPassword())) {
            return "Password Incorrect";
        }
        if (!u.getRole().equals(user.getRole())) {
            return "Role Incorrect";
        }
        return String.valueOf(user.getId());
    }
    @GetMapping("/get-details/{id}")
    public DisplayDTO display(@PathVariable int id){
        User user = userRepo.findById(id)
                .orElseThrow(()->new RuntimeException("User Not Found"));
        DisplayDTO displayDTO = new DisplayDTO();
        displayDTO.setUsername(user.getUsername());
        displayDTO.setBalance(user.getBalance());
        return displayDTO;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user = userRepo.findById(obj.getId())
                .orElseThrow(()->new RuntimeException("Not Found"));


        if(obj.getKey().equalsIgnoreCase("Name"))
        {
            if(obj.getValue().equals(user.getName())){ return "cannot be same";}
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("Password"))
        {
            if(obj.getValue().equals(user.getPassword())){ return "cannot be same";}
            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equals(user.getEmail())){ return "cannot be same";}
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2!=null){return "Email already exist";}
            user.setEmail(obj.getValue());
        }
        else
        {
            return "Invalid Key";
        }
        userRepo.save(user);
        return "updated successfully";
    }
    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        userRepo.save(user);
        History h1 = new History();
        h1.setDescription("Admin" + adminId + "created user"+user.getUsername());
        if(user.getBalance()>0)
        {
            User user2 = userRepo.findByUsername(user.getUsername());
            Transaction transaction = new Transaction();
            transaction.setAmount(user.getBalance());
            transaction.setCurrBalance(user.getBalance());
            transaction.setDescription("Rs. " +user.getBalance()+ " Deposited Successfully");
            transaction.setUserId(user.getId());
            transactionRepo.save(transaction);
        }
        historyRepo.save(h1);
        userRepo.save(user);
        return "Added Successfully";
    }

    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId,@PathVariable int adminId)
    {
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new RuntimeException("Not found"));
        if(user.getBalance()>0)
        {
            return "Balance should be zero";
        }
        History h1 = new History();
        h1.setDescription("Admin"+adminId+"Deleted User"+user.getUsername());
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully";
    }
    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole( "customer",sort);
    }
    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }
}

