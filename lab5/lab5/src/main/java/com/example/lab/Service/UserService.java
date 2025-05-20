package com.example.lab.Service;

import com.example.lab.DTO.UserDTO;
import com.example.lab.Exception.ResourceNotFoundException;
import com.example.lab.model.User;
import com.example.lab.Repository.UserRepository;
import com.example.lab.Cache.InMemoryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InMemoryCache cache;

    @Autowired
    public UserService(UserRepository userRepository, InMemoryCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        if (cache.containsUserKey(id)) {
            return Optional.of(cache.getUser(id));
        }
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        cache.putUser(id, user.get());
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(userDTO.getUsername());
            User updatedUser = userRepository.save(user);
            cache.putUser(id, updatedUser);
            return updatedUser;
        }
        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        cache.removeUser(id);
    }
    public List<User> createUsersBulk(List<UserDTO> userDTOs) {
        List<User> users = userDTOs.stream()
                .map(dto -> {
                    User user = new User();
                    user.setUsername(dto.getUsername());
                    return user;
                })
                .collect(Collectors.toList());
        return userRepository.saveAll(users);
    }

}