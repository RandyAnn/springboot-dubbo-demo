package com.example.common.service;
import com.example.common.entity.User;

public interface UserService {
    User getUserByUsername(String username);
    User createUser(User user);
}
