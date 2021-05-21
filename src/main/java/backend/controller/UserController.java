package backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import backend.domain.User;
import backend.dto.UserDTO;
import backend.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/users")
@RestController
@CrossOrigin
public class UserController {

    private UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUser() {
        List<User> users = userService.findAll();
        if (users.size() == 0)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            userDTOList.add(userDTOConverter(user));
        }
        return new ResponseEntity<>(userDTOList, HttpStatus.OK);
    }

    private UserDTO userDTOConverter(User user) {
        return UserDTO.builder()
                .username(user.getUsername())
                .build();
    }
}
