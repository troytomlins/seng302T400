package org.seng302.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.seng302.user.Role.*;

/**
 * UserResource class
 */
@RestController
public class UserResource {

    @Autowired
    private UserRepository userRepository;

    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verifies the session token, throws an error if it does not exist, and if it does, returns the User object.
     * @param sessionToken Session token
     * @return User object
     */
    public User getUserVerifySession(String sessionToken) {
        Optional<User> user = userRepository.findBySessionUUID(sessionToken);
        if (sessionToken == null || user.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Access token is missing or invalid"
            );
        } else {
            return user.get();
        }
    }

    /**
     * Gets a unique session UUID, by generating until a session token is generated that does not already exist.
     * @return Unique session UUID
     */
    public String getUniqueSessionUUID() {
        String sessionUUID = User.generateSessionUUID();
        while (userRepository.findBySessionUUID(sessionUUID).isPresent()) {
            sessionUUID = User.generateSessionUUID();
        }
        return sessionUUID;
    }

    /**
     * Attempt to authenticate a user account with a username and password.
     * @param login Login payload
     * @param response HTTP Response
     */
    @PostMapping("/login")
    public UserIdPayload loginUser(@RequestBody LoginPayload login, HttpServletResponse response) {
        Optional<User> user = userRepository.findByEmail(login.getEmail());

        if (user.isPresent()) {
            if (user.get().verifyPassword(login.getPassword())) {
                String sessionUUID = getUniqueSessionUUID();

                user.get().setSessionUUID(sessionUUID);
                userRepository.save(user.get());

                Cookie cookie = new Cookie("JSESSIONID", sessionUUID);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);

                return new UserIdPayload(user.get().getId());
            }
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed login attempt, email or password incorrect"
        );
    }

    /**
     * Create a new user account.
     * @param registration Registration payload
     */
    @PostMapping("/users")
    public ResponseEntity<UserIdPayload> registerUser(
            @RequestBody RegistrationPayload registration, HttpServletResponse response
    ) {

        if (userRepository.findByEmail(registration.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email address already in use"
            );
        }

        try {
            User newUser = new User(
                    registration.getFirstName(),
                    registration.getLastName(),
                    registration.getMiddleName(),
                    registration.getNickname(),
                    registration.getBio(),
                    registration.getEmail(),
                    registration.getDateOfBirth(),
                    registration.getPhoneNumber(),
                    registration.getHomeAddress(),
                    registration.getPassword(),
                    LocalDateTime.now(),
                    USER);

            newUser.setSessionUUID(getUniqueSessionUUID());
            User createdUser = userRepository.save(newUser);

            Cookie cookie = new Cookie("JSESSIONID", createdUser.getSessionUUID());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.CREATED).body(new UserIdPayload(createdUser.getId()));

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    /**
     * Get method for retrieving a specific user account.
     * @param id Integer Id (primary key)
     * @return User object if it exists
     */
    @GetMapping("/users/{id}")
    public UserPayload retrieveUser(
            @CookieValue(value = "JSESSIONID", required = false) String sessionToken, @PathVariable Integer id
    ) {
        getUserVerifySession(sessionToken);

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    "The requested route does exist (so not a 404) but some part of the request is not acceptable, " +
                            "for example trying to access a resource by an ID that does not exist."
            );
        }

        Role role = null;

        if (verifyRole(sessionToken, Role.DEFAULTGLOBALAPPLICATIONADMIN)) {
            role = user.get().getRole();
        }

        return new UserPayload(
                user.get().getId(),
                user.get().getFirstName(),
                user.get().getLastName(),
                user.get().getMiddleName(),
                user.get().getNickname(),
                user.get().getBio(),
                user.get().getEmail(),
                user.get().getDateOfBirth(),
                user.get().getPhoneNumber(),
                user.get().getHomeAddress(),
                user.get().getCreated(),
                role
        );
    }

    /**
     * Search for users by some criteria, for now using names and nickname.
     * @param sessionToken Session token
     * @param searchQuery Search query
     * @return A list of UserPayload objects matching the search query
     */
    @GetMapping("/users/search")
    public List<UserPayload> searchUsers(
            @CookieValue(value = "JSESSIONID", required = false) String sessionToken, @RequestParam String searchQuery
    ) {
        getUserVerifySession(sessionToken);

        List<UserPayload> users;

        String[] searchQuerySplit = searchQuery.split(" ");

        if (searchQuerySplit.length == 3) {  // Query including the first, middle and last names.
            users = userRepository.findByFirstNameIgnoreCaseAndMiddleNameIgnoreCaseAndLastNameIgnoreCase(
                    searchQuerySplit[0], searchQuerySplit[1], searchQuerySplit[2]
            );
        } else if (searchQuerySplit.length == 2) {  // Query including the first and last names.
            users = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                    searchQuerySplit[0], searchQuerySplit[1]
            );
        } else {  // Query including either the nickname, first, middle or last name.
            users = new ArrayList<>(userRepository.findByNicknameIgnoreCase(searchQuery));
            users.addAll(userRepository.findByFirstNameIgnoreCase(searchQuery));
            users.addAll(userRepository.findByLastNameIgnoreCase(searchQuery));
            users.addAll(userRepository.findByMiddleNameIgnoreCase(searchQuery));
        }

        if (!verifyRole(sessionToken, Role.DEFAULTGLOBALAPPLICATIONADMIN)) {
            for (UserPayload user: users) {
                user.setRole(null);
            }
        }

        return users.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Checks if the current user's role matches the role parameter.
     * This method is useful for user authentication/identification.
     * @param sessionToken Session token
     * @param role Role being matched
     * @return boolean Returns true if the current user's role matches the role parameter, otherwise false.
     */
    private boolean verifyRole(String sessionToken, Role role) {
        Optional<User> userOptional = userRepository.findBySessionUUID(sessionToken);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get method for change the Role of a user from USER to GLOBALAPPLICATIONADMIN by Email address
     * @param id Email address (primary key)
     */
    @PutMapping("/users/{id}/makeAdmin")
    @ResponseStatus(value = HttpStatus.OK, reason = "Action completed successfully")
    public void setGAA(@PathVariable int id, @CookieValue(value = "JSESSIONID", required = false) String sessionToken){
        User currentUser = getUserVerifySession(sessionToken);

        Optional<User> optionalSelectedUser = userRepository.findById(id);

        if (optionalSelectedUser.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    "The requested route does exist (so not a 404) but some part of the request is not acceptable, " +
                            "for example trying to access a resource by an ID that does not exist."
            );
        } else {
            User selectedUser = optionalSelectedUser.get();
            if (selectedUser.getRole() == USER && currentUser.getRole() == DEFAULTGLOBALAPPLICATIONADMIN){
                selectedUser.setRole(GLOBALAPPLICATIONADMIN);
                userRepository.saveAndFlush(selectedUser);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "The user does not have permission to perform the requested action"
                );
            }
        }
    }


    /**
     * Get method for change the Role of a user account from USE to GLOBALAPPLICATIONADMIN by Email address
     * @param id mail address (primary key)
     */
    @PutMapping("/users/{id}/revokeAdmin")
    @ResponseStatus(value = HttpStatus.OK, reason = "Account created successfully")
    public void revokeGAA(@PathVariable int id, @CookieValue(value = "JSESSIONID", required = false) String sessionToken) {
        User currentUser = getUserVerifySession(sessionToken);

        Optional<User> optionalSelectedUser = userRepository.findById(id);

        if (optionalSelectedUser.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    "The requested route does exist (so not a 404) but some part of the request is not acceptable, " +
                            "for example trying to access a resource by an ID that does not exist."
            );
        } else {
            User selectedUser = optionalSelectedUser.get();
            if (selectedUser.getRole() == GLOBALAPPLICATIONADMIN && currentUser.getRole() == DEFAULTGLOBALAPPLICATIONADMIN) {
                selectedUser.setRole(USER);
                userRepository.saveAndFlush(selectedUser);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "The user does not have permission to perform the requested action"
                );
            }
        }
    }
}
