package com.montrosesoftware;

import com.montrosesoftware.entities.User;
import com.montrosesoftware.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class Demo implements CommandLineRunner {

    @Autowired
    private UserRepo uRepo;

    @Override
    public void run(String... strings) throws Exception {
        User u = uRepo.get(1);

        getClass();
    }
}
