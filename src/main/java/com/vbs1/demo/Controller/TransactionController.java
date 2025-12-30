package com.vbs1.demo.Controller;

import com.vbs1.demo.dto.TransactionDto;
import com.vbs1.demo.dto.TransferDto;
import com.vbs1.demo.models.Transaction;
import com.vbs1.demo.models.User;
import com.vbs1.demo.repositories.TransactionRepo;
import com.vbs1.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;
    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto t) {
        User user = userRepo.findById(t.getId())
                .orElseThrow(() -> new RuntimeException("Error"));
        double newBalance = user.getBalance() + t.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);
        Transaction transaction = new Transaction();
        transaction.setAmount(t.getAmount());
        transaction.setCurrBalance(user.getBalance());
        transaction.setDescription("Rs. " +t.getAmount()+ " Deposited Successfully");
        transaction.setUserId(t.getId());
        transactionRepo.save(transaction);
        return "Amount successfully deposited";
    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto t) {
        User user = userRepo.findById(t.getId())
                .orElseThrow(() -> new RuntimeException("Error"));
        double newBalance = user.getBalance() - t.getAmount();
        if(newBalance<0)
        {
            return "Balance not Sufficient";
        }
        user.setBalance(newBalance);
        userRepo.save(user);
        Transaction transaction = new Transaction();
        transaction.setAmount(t.getAmount());
        transaction.setCurrBalance(user.getBalance());
        transaction.setDescription("Rs. " +t.getAmount()+ " Withdrawal Successfully");
        transaction.setUserId(t.getId());
        transactionRepo.save(transaction);
        return "Withdrawal successful!";
    }
    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj){
        User sender = userRepo.findById(obj.getId())
                .orElseThrow(()->new RuntimeException("Error"));
        User rec = userRepo.findByUsername(obj.getUsername());

        if(rec==null){return "User Not found";}
        if(obj.getAmount()<0){return "Invalid Transaction";}
        if(sender.getId()==rec.getId()){return "Self transaction not allowed";}
        double sbalance = sender.getBalance()-obj.getAmount();
        double rbalance = rec.getBalance()+obj.getAmount();
        if(sbalance<0){return "Insufficient Balance";}

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);
        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sender.getBalance());
        t1.setDescription("Rs."+obj.getAmount()+"Transfered to"+rec.getUsername());
        t1.setUserId(obj.getId());
        transactionRepo.save(t1);

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rec.getBalance());
        t2.setDescription("Rs."+obj.getAmount()+"Recived from "+sender.getUsername());
        t2.setUserId(rec.getId());
        transactionRepo.save(t2);
        return "Transfered Successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);

    }
}
