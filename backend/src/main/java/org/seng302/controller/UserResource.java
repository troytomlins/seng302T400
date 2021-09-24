/**
 * Summary. This file contains the definition for the UserResource.
 * <p>
 * Description. This file contains the defintion for the UserResource.
 *
 * @link team-400/src/main/java/org/seng302/user/UserResource
 * @file This file contains the definition for UserResource.
 * @author team-400.
 * @since 5.5.2021
 */
package org.seng302.controller;

import org.seng302.exceptions.IllegalAddressArgumentException;
import org.seng302.exceptions.IllegalUserArgumentException;
import org.seng302.model.Address;
import org.seng302.Authorization;
import org.seng302.utils.PaginationUtils;
import org.seng302.utils.SearchUtils;
import org.seng302.view.incoming.UserIdPayload;
import org.seng302.view.incoming.UserLoginPayload;
import org.seng302.view.incoming.UserProfileModifyPayload;
import org.seng302.view.incoming.UserRegistrationPayload;
import org.seng302.view.outgoing.*;
import org.seng302.model.repository.AddressRepository;
import org.seng302.model.Business;
import org.seng302.model.enums.Role;
import org.seng302.model.User;
import org.seng302.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.seng302.Authorization.*;
import static org.seng302.model.enums.Role.*;

/**
 * UserResource class. This class includes:
 * POST "/login" endpoint used to allow a user to login.
 * POST "/logout" endpoint used to allow a user to logout.
 * POST "/users" endpoint used to create a new user account.
 * GET "/users/{id}" endpoint used to retrieve the details of a user account.
 * GET "/users/search" endpoint used to retrieve user accounts based on search criteria.
 * PUT "/users/{id}/makeAdmin" endpoint used to make a user account a GAA.
 * PUT "/users/{id}/revokeAdmin" endpoint used to revoke admin perms from user account (GAA -> normal user account)
 */
@RestController
public class UserResource {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private static final Logger logger = LogManager.getLogger(UserResource.class.getName());

    // the name of the cookie used for authentication.
    private static final String COOKIE_AUTH = "JSESSIONID";
    // the value of same site attribute.
    private static final String SAME_SITE_STRICT = "strict";
    // the error message to be logged when requested route does not exist.
    private static final String LOGGER_ERROR_REQUESTED_ROUTE = "Requested route does exist, but some part of the request is not acceptable";
    // the message to be returned when there is a 406 error.
    private static final String HTTP_NOT_ACCEPTABLE_MESSAGE = "The requested route does exist (so not a 404) but some part of the request is not acceptable, " +
            "for example trying to access a resource by an ID that does not exist.";

    public UserResource(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Gets a unique session UUID, by generating until a session token is generated that does not already exist.
     *
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
     * Checks that the user has attempts remaining. If the user exceeds three attempts, they are locked from their
     * account for 1 hour.
     *
     * @param login    Login payload
     * @param response HTTP Response
     */
    @PostMapping("/login")
    public ResponseEntity<UserIdPayload> loginUser(@RequestBody UserLoginPayload login, HttpServletResponse response) {

        Optional<User> optionalUser = userRepository.findByEmail(login.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Check if account locked
            if (user.isLocked()) {
                if (user.canUnlock()) {
                    user.unlockAccount();
                    userRepository.save(user);
                } else {
                    logger.error("Login Failure - 403 [FORBIDDEN] - Cannot unlock account");
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Exceeded login attempts. Please try again in 1 hour."
                    );
                }
            }

                // User exists, account not locked and password is correct
            if (user.verifyPassword(login.getPassword())) {
                String sessionUUID = getUniqueSessionUUID();

                user.setSessionUUID(sessionUUID);
                userRepository.save(user);

                ResponseCookie cookie = ResponseCookie.from(COOKIE_AUTH, sessionUUID).maxAge(28800).sameSite(SAME_SITE_STRICT).httpOnly(true).build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                logger.info("Successful Login - 200 [OK] - User Id: {}", user.getId());
                return ResponseEntity.status(HttpStatus.OK).body(new UserIdPayload(user.getId()));

                // User either does not exist or the password is incorrect
            } else {

                user.useAttempt();
                userRepository.save(user);
                // Lock account if used up all login attempts
                if (!user.hasLoginAttemptsRemaining()) {
                    user.lockAccount();
                    userRepository.save(user);
                }

                logger.error("Login Failure - 400 [BAD_REQUEST] - Email or password incorrect");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Failed login attempt, email or password incorrect"
                );
            }

        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed login attempt, email or password incorrect"
        );
    }

    /**
     * Attempt to authenticate a user account with a username and password.
     * @param sessionToken Login payload
     * @param response HTTP Response
     */
    @PostMapping("/logout")
    public void logoutUser(@CookieValue(value = COOKIE_AUTH, required = false) String sessionToken,
                           HttpServletResponse response) {
        if (sessionToken != null) {

            ResponseCookie cookie = ResponseCookie.from(COOKIE_AUTH, sessionToken).maxAge(0).sameSite(SAME_SITE_STRICT).httpOnly(true).build(); // maxAge 0 deletes the cookie
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());


        }
    }

    /**
     * Extracts the address parts of the given address
     * @param addressPayload The address to separate into address parts
     * @return address The Address created from the addressPayload
     */
    private Address extractAddress(AddressPayload addressPayload) {
        Address address;
        try {
            String streetNumber = addressPayload.getStreetNumber();
            String streetName = addressPayload.getStreetName();
            String city = addressPayload.getCity();
            String region = addressPayload.getRegion();
            String country = addressPayload.getCountry();
            String postcode = addressPayload.getPostcode();
            String suburb = addressPayload.getSuburb();

            // Check to see if address already exists.
            Optional<Address> storedAddress = addressRepository.findAddressByStreetNumberAndStreetNameAndCityAndRegionAndCountryAndPostcodeAndSuburb(
                    streetNumber, streetName, city, region, country, postcode, suburb);

            // If address already exists it is retrieved.
            // The businesses already existing are also retrieved. These businesses will be
            // used to determine if a business hasn't already been created.
            if (storedAddress.isPresent()) {
                address = storedAddress.get();
            } else {
                // Otherwise, a new address is created and saved.
                address = new Address(
                        streetNumber,
                        streetName,
                        city,
                        region,
                        country,
                        postcode,
                        suburb
                );
                addressRepository.save(address);
            }
        } catch (IllegalAddressArgumentException e) {
            logger.error("Registration Failure - {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
        return address;
    }

    /**
     * Create a new user account.
     * @param registration Registration payload
     */
    @PostMapping("/users")
    public ResponseEntity<UserIdPayload> registerUser(
            @RequestBody UserRegistrationPayload registration, HttpServletResponse response
    ) {
        if (userRepository.findByEmail(registration.getEmail()).isPresent()) {
            logger.error("Registration Failure - Email already in use {}", registration.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email address already in use"
            );
        }

        Address address = extractAddress(registration.getHomeAddress());

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
                    address,
                    registration.getPassword(),
                    LocalDateTime.now(),
                    Role.USER);

            newUser.setSessionUUID(getUniqueSessionUUID());
            User createdUser = userRepository.save(newUser);

            ResponseCookie cookie = ResponseCookie.from(COOKIE_AUTH, createdUser.getSessionUUID()).maxAge(3600).sameSite(SAME_SITE_STRICT).httpOnly(true).build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            logger.info("Successful Registration - User Id {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserIdPayload(createdUser.getId()));

        } catch (IllegalUserArgumentException e) {
            logger.error("Registration Failure - {}", e.getMessage());
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
    public UserPayloadParent retrieveUser(
            @CookieValue(value = COOKIE_AUTH, required = false) String sessionToken, @PathVariable Integer id
    ) throws Exception {
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);

        Optional<User> optionalSelectUser = userRepository.findById(id);

        if (optionalSelectUser.isEmpty()) {
            logger.error(LOGGER_ERROR_REQUESTED_ROUTE);
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    HTTP_NOT_ACCEPTABLE_MESSAGE
            );
        }

        User selectUser = optionalSelectUser.get();

        //stop payload loop
        List<Business> administrators;
        administrators = selectUser.getBusinessesAdministeredObjects();
        for (Business administrator : administrators) {
            administrator.setAdministrators(new ArrayList<>());
        }

        logger.info("User Found - {}", selectUser);
        if (currentUser.getId() == id || isGAAorDGAA(currentUser)){

            Role role = selectUser.getRole();

            // If the current ID matches the retrieved user's ID or the current user is the DGAA, return a normal UserPayload with everything in it.
            return new UserPayload(
                    selectUser.getId(),
                    selectUser.getFirstName(),
                    selectUser.getLastName(),
                    selectUser.getMiddleName(),
                    selectUser.getNickname(),
                    selectUser.getBio(),
                    selectUser.getEmail(),
                    selectUser.getDateOfBirth(),
                    selectUser.getPhoneNumber(),
                    selectUser.getHomeAddress().toAddressPayload(),
                    selectUser.getCreated(),
                    role,
                    administrators,
                    selectUser.getUserImages()
            );
        } else {
            // Otherwise, return a UserPayloadSecure without the phone number, date of birth and a secure address with only the city, region, and country.
            return new UserPayloadSecure(
                    selectUser.getId(),
                    selectUser.getFirstName(),
                    selectUser.getLastName(),
                    selectUser.getMiddleName(),
                    selectUser.getNickname(),
                    selectUser.getBio(),
                    selectUser.getEmail(),
                    selectUser.getHomeAddress().toAddressPayloadSecure(),
                    selectUser.getCreated(),
                    null,
                    administrators,
                    selectUser.getUserImages()
            );
        }


    }

    /**
     * Search for users by first name, middle name, last name, or nickname.
     * Returns paginated and ordered results based on input query params.
     * @param sessionToken Session token
     * @param searchQuery Search query
     * @param orderBy Column to order the results by
     * @param page Page number to return results from
     * @return A list of UserPayload objects matching the search query
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserPayloadSecure>> searchUsers(
            @CookieValue(value = COOKIE_AUTH, required = false) String sessionToken,
            @RequestParam String searchQuery,
            @RequestParam(defaultValue = "fullNameASC") String orderBy,
            @RequestParam(defaultValue = "0") String page
    ) throws Exception {
        logger.debug("User search request received with search query {}, order by {}, page {}", searchQuery, orderBy, page);

        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);

        int pageNo = PaginationUtils.parsePageNumber(page);

        // Front-end displays 5 users per page
        int pageSize = 5;

        Sort sortBy;
        Sort sortByEmailASC = Sort.by(Sort.Order.asc("email").ignoreCase());
        // IgnoreCase is important to let lower case letters be the same as upper case in ordering.
        // Normally all upper case letters come before any lower case ones.
        switch (orderBy) {
            case "fullNameASC":
                sortBy = Sort.by(Sort.Order.asc("firstName").ignoreCase()).and(Sort.by(Sort.Order.asc("middleName").ignoreCase())).and(Sort.by(Sort.Order.asc("lastName").ignoreCase())).and(sortByEmailASC);
                break;
            case "fullNameDESC":
                sortBy = Sort.by(Sort.Order.desc("firstName").ignoreCase()).and(Sort.by(Sort.Order.desc("middleName").ignoreCase())).and(Sort.by(Sort.Order.desc("lastName").ignoreCase())).and(sortByEmailASC);
                break;
            case "nicknameASC":
                sortBy = Sort.by(Sort.Order.asc("nickname").ignoreCase()).and(sortByEmailASC);
                break;
            case "nicknameDESC":
                sortBy = Sort.by(Sort.Order.desc("nickname").ignoreCase()).and(sortByEmailASC);
                break;
            case "emailASC":
                sortBy = sortByEmailASC;
                break;
            case "emailDESC":
                sortBy = Sort.by(Sort.Order.desc("email").ignoreCase());
                break;
            case "addressASC":
                sortBy = Sort.by(Sort.Order.asc("homeAddress.city").ignoreCase()).and(Sort.by(Sort.Order.asc("homeAddress.region").ignoreCase()).and(Sort.by(Sort.Order.asc("homeAddress.country").ignoreCase())).and(sortByEmailASC));
                break;
            case "addressDESC":
                sortBy = Sort.by(Sort.Order.desc("homeAddress.city").ignoreCase()).and(Sort.by(Sort.Order.desc("homeAddress.region").ignoreCase()).and(Sort.by(Sort.Order.desc("homeAddress.country").ignoreCase())).and(sortByEmailASC));
                break;
            default:
                logger.error("400 [BAD REQUEST] - {} is not a valid order by parameter", orderBy);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "OrderBy Field invalid"
                );
        }

        Pageable paging = PageRequest.of(pageNo, pageSize, sortBy);

        Page<User> pagedResult = parseAndExecuteQuery(searchQuery, paging);

        int totalPages = pagedResult.getTotalPages();
        int totalRows = (int) pagedResult.getTotalElements();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Total-Pages", String.valueOf(totalPages));
        responseHeaders.add("Total-Rows", String.valueOf(totalRows));

        logger.info("Search Success - 200 [OK] -  Users retrieved for search query {}, order by {}, page {}", searchQuery, orderBy, pageNo);

        logger.debug("Users Found: {}", pagedResult.toList());
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(convertToPayloadSecureAndRemoveRolesIfNotAuthenticated(pagedResult.getContent(), currentUser));
    }

    /**
     * This method parses the search criteria and then calls the needed methods to execute the "query".
     *
     * @param searchQuery criteria to search for users (user's nickname, first name, middle name, last name or full name).
     * @param paging information used to paginate the retrieved users.
     * @return Page<User> A page of users matching the search criteria.
     *
     * Preconditions:  A non-null string representing a search query that can contain several names to be searched for (can be empty string)
     * Postconditions: A page containing the results of the user search is returned.
     */
    private Page<User> parseAndExecuteQuery(String searchQuery, Pageable paging) {
        if (searchQuery.equals("")) return userRepository.findAll(paging); // All users should be returned.
        List<String> names = SearchUtils.convertSearchQueryToNames(searchQuery);
        return userRepository.findAllUsersByNames(names, paging);
    }

    /**
     * This method converts a list of users to "secure" payloads which omit user details due to privacy concerns.
     *
     * @param userList the list of users the current user is trying to view.
     * @param user the user who is trying to access the details of other users. If they are not an admin then they
     *             can not view extra details of other users.
     * @return List<UserPayloadSecure> A list of users who have had some fields of their address removed due to privacy
     * concerns.
     * @throws Exception thrown if error occurs when converting to secure payload.
     */
    public List<UserPayloadSecure> convertToPayloadSecureAndRemoveRolesIfNotAuthenticated(List<User> userList, User user) throws Exception {
        List<UserPayloadSecure> userPayloadList;
        userPayloadList = UserPayloadSecure.convertToPayloadSecure(userList);

        for (UserPayloadSecure userPayloadSecure : userPayloadList) {
            Role role = null;
            if (verifyRole(user, Role.DEFAULTGLOBALAPPLICATIONADMIN)) {
                role = userPayloadSecure.getRole();
            }
            userPayloadSecure.setRole(role);
        }

        return userPayloadList;
    }

    /**
     * Get method for change the Role of a user from USER to GLOBALAPPLICATIONADMIN by Email address
     * @param id Email address (primary key)
     */
    @PutMapping("/users/{id}/makeAdmin")
    @ResponseStatus(value = HttpStatus.OK, reason = "Action completed successfully")
    public void setGAA(@PathVariable int id, @CookieValue(value = COOKIE_AUTH, required = false) String sessionToken) {
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);

        Optional<User> optionalSelectedUser = userRepository.findById(id);

        if (optionalSelectedUser.isEmpty()) {
            logger.error(LOGGER_ERROR_REQUESTED_ROUTE);
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    HTTP_NOT_ACCEPTABLE_MESSAGE
            );
        } else {
            User selectedUser = optionalSelectedUser.get();
            if (selectedUser.getRole() == USER && currentUser.getRole() == DEFAULTGLOBALAPPLICATIONADMIN) {
                selectedUser.setRole(GLOBALAPPLICATIONADMIN);
                userRepository.saveAndFlush(selectedUser);
                logger.info("User with Id: {} is now GAA.", selectedUser.getId());
            } else {
                logger.error("User does not have permission to perform action.");
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "The user does not have permission to perform the requested action"
                );
            }
        }
    }


    /**
     * Put method to change the Role of a user account from USER to GLOBALAPPLICATIONADMIN by Email address
     * @param id mail address (primary key)
     */
    @PutMapping("/users/{id}/revokeAdmin")
    @ResponseStatus(value = HttpStatus.OK, reason = "Action completed successfully")
    public void revokeGAA(@PathVariable int id, @CookieValue(value = COOKIE_AUTH, required = false) String sessionToken) {
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);

        Optional<User> optionalSelectedUser = userRepository.findById(id);

        if (optionalSelectedUser.isEmpty()) {
            logger.error(LOGGER_ERROR_REQUESTED_ROUTE);
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    HTTP_NOT_ACCEPTABLE_MESSAGE
            );
        } else {
            User selectedUser = optionalSelectedUser.get();
            if (selectedUser.getRole() == GLOBALAPPLICATIONADMIN && currentUser.getRole() == DEFAULTGLOBALAPPLICATIONADMIN) {
                selectedUser.setRole(USER);
                userRepository.saveAndFlush(selectedUser);
                logger.info("User with Id: {} is now USER.", selectedUser.getId());
            } else {
                logger.error("User does not have permission to perform action.");
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "The user does not have permission to perform the requested action"
                );
            }
        }
    }

    /**
     * Update given user by given user payload
     * @param currentUser current user logged in
     * @param selectedUser user to edit
     * @param userProfileModifyPayload user payload
     * @return updated User
     */
    private User updateUserInfo(User currentUser, User selectedUser, UserProfileModifyPayload userProfileModifyPayload) {
        String newEmailAddress = userProfileModifyPayload.getEmail();
        if (userRepository.findByEmail(newEmailAddress).isPresent() && !selectedUser.getEmail().equals(newEmailAddress)) {
            logger.error("Registration Failure - {}", "Email address used");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The Email already been used."
            );
        }
        try {
            Address address = extractAddress(userProfileModifyPayload.getHomeAddress());
            selectedUser.setHomeAddress(address);
            selectedUser.updateFirstName(userProfileModifyPayload.getFirstName());
            selectedUser.updateLastName(userProfileModifyPayload.getLastName());
            selectedUser.updateMiddleName(userProfileModifyPayload.getMiddleName());
            selectedUser.updateNickname(userProfileModifyPayload.getNickname());
            selectedUser.updateBio(userProfileModifyPayload.getBio());
            selectedUser.updateEmail(userProfileModifyPayload.getEmail());
            selectedUser.updateDateOfBirth(userProfileModifyPayload.getDateOfBirth());
            selectedUser.updatePhoneNumber(userProfileModifyPayload.getPhoneNumber());
            if (userProfileModifyPayload.getNewPassword() != null) {
                if (userProfileModifyPayload.getCurrentPassword() != null) {
                    if (validPasswordOrHavePermission(selectedUser, currentUser, userProfileModifyPayload)) {
                        selectedUser.updatePassword(userProfileModifyPayload.getNewPassword());
                    } else if (userProfileModifyPayload.getCurrentPassword() != null
                            && !userProfileModifyPayload.getCurrentPassword().isEmpty()) {
                        logger.error("User Update Failure - {}", "current password error");
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Wrong Password"
                        );
                    }
                } else {
                    logger.error("User Update Failure - {}", "current password not sent");
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Current password not sent"
                    );
                }
            }
        } catch (IllegalUserArgumentException e) {
            logger.error("Registration Failure - {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
        logger.debug("Selected user (ID: {}) update successfully.", selectedUser.getId());
        return selectedUser;
    }

    /**
     * Checks if the current user can change the password of the selected user
     * @param selectedUser User the password is changing for
     * @param currentUser User changing the password
     * @param userProfileModifyPayload Payload containing the modify user data
     * @return boolean T/F if the current user can change the password
     */
    private boolean validPasswordOrHavePermission(User selectedUser, User currentUser, UserProfileModifyPayload userProfileModifyPayload) {
        // Case 1: Valid Password
        if (selectedUser.verifyPassword(userProfileModifyPayload.getCurrentPassword())) {
            return true;
        }
        // Case 2: User is Admin & selected User is not
        if (Authorization.isGAAorDGAA(currentUser) && !Authorization.isGAAorDGAA(selectedUser)) {
            return true;
        }
        // Case 3: User is DGAA & selected user is GAA, if not, returns false
        return currentUser.getRole().equals(DEFAULTGLOBALAPPLICATIONADMIN) && selectedUser.getRole().equals(GLOBALAPPLICATIONADMIN);
    }

    /**
     * Put method to modify user profile.
     * @param id current user id
     * @param sessionToken sessionToken for current user
     * @param userProfileModifyPayload new profile info
     */
    @PutMapping("/users/{id}/profile")
    @ResponseStatus(value = HttpStatus.OK, reason = "Account updated successfully")
    public void modifiedUserProfile(@PathVariable int id,
                                    @CookieValue(value = "JSESSIONID", required = false) String sessionToken,
                                    @RequestBody(required = false) UserProfileModifyPayload userProfileModifyPayload) {
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);

        Optional<User> optionalSelectedUser = userRepository.findById(id);

        if (optionalSelectedUser.isEmpty()) {
            logger.error("Selected user does not exist.");
            throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE,
                    "Selected user does not exist."
            );
        }

        User selectedUser = optionalSelectedUser.get();
        logger.debug("Selected user (ID: {}) retrieve successfully.", selectedUser.getId());

        if (selectedUser.getId() != currentUser.getId() && !Authorization.isGAAorDGAA(currentUser)) {
            logger.error("User does not have permission to perform action.");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The user does not have permission to perform the requested action"
            );
        }

        userRepository.save(updateUserInfo(currentUser, selectedUser, userProfileModifyPayload));
        logger.info("Selected user (ID: {}) profile update saved.", selectedUser.getId());
    }
}
