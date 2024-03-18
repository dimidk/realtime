package realTimeEdt.realTime.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import realTimeEdt.realTime.model.Role;
import realTimeEdt.realTime.model.TypeMessage;
import realTimeEdt.realTime.model.User;
import realTimeEdt.realTime.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addNew(User user) {

        if (user == null) {
            throw new IllegalArgumentException("no user");
        }
        userRepository.insert(user);
        //userRepository.save(user);
    }

    public boolean exists(User user) {

        return userRepository.existsById(user.getName());

    }

    public boolean existsByName(String name) {

        if (name == null) {
            throw new IllegalArgumentException("no name");
        }

        log.info("UserService: check is user exists in db {}", name);

        if (userRepository.existsByName(name)) {

            log.info("UserService: user exists {}", userRepository.findUserByName(name).getName());
        }

        return userRepository.existsByName(name);
    }
    public User findUser(String name) {

        if (name == null) {
            throw new IllegalArgumentException("no name");
        }

        return userRepository.findUserByName(name);
        //return userRepository.findById(id).get();
    }

    public List<User> findAllConnectedUsers() {

        return userRepository.findAllByStatus(TypeMessage.JOIN);
    }

    public void updateUser(String name, TypeMessage status) {

        if (name == null) {
            throw new IllegalArgumentException("no name");
        }
        if (status == null) {
            throw new IllegalArgumentException("no given status");
        }

        User user = findUser(name);
        user.setStatus(status);
        userRepository.save(user);
    }


    public Role getRole(User user) {

        return user.getRole();
    }
}
