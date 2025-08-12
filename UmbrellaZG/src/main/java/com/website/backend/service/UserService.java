package com.website.backend.service;

import com.website.backend.entity.User;
import java.util.Optional;

public interface UserService {

	User registerUser(String username, String password);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

}