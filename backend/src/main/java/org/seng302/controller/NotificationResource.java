package org.seng302.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Authorization;
import org.seng302.model.*;
import org.seng302.model.repository.*;
import org.seng302.view.outgoing.SoldListingNotificationPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.management.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class NotificationResource {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private MarketCardNotificationRepository marketCardNotificationRepository;

    @Autowired
    private KeywordNotificationRepository keywordNotificationRepository;

    @Autowired
    private ListingNotificationRepository listingNotificationRepository;

    @Autowired
    private SoldListingNotificationRepository soldListingNotificationRepository;

    public NotificationResource(UserRepository userRepository,
                                BusinessRepository businessRepository,
                                MarketCardNotificationRepository marketCardNotificationRepository,
                                KeywordNotificationRepository keywordNotificationRepository,
                                ListingNotificationRepository listingNotificationRepository,
                                SoldListingNotificationRepository soldListingNotificationRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.marketCardNotificationRepository = marketCardNotificationRepository;
        this.keywordNotificationRepository = keywordNotificationRepository;
        this.listingNotificationRepository = listingNotificationRepository;
        this.soldListingNotificationRepository = soldListingNotificationRepository;
    }

    private static final Logger logger = LogManager.getLogger(NotificationResource.class.getName());

    /**
     * Retrieve all notifications for the current user.
     *
     * @param sessionToken The token used to identify the user.
     * @return List<Object> The list of notifications for the current user.
     * @throws Exception Exception
     */
    @GetMapping("/users/notifications")
    public List<Object> retrieveAllNotifications(
            @CookieValue(value = "JSESSIONID", required = false) String sessionToken
    ) throws Exception {
        //401
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);
        logger.debug("User (Id: {}) received.", currentUser.getId());

        List<Object> notificationPayloads = new ArrayList<>();
        List<MarketCardNotification> marketCardNotifications = marketCardNotificationRepository.findAllByUserId(currentUser.getId());
        List<KeywordNotification> keywordNotifications = new ArrayList<>();
        List<ListingNotification> listingNotifications = listingNotificationRepository.findAllByUsersId(currentUser.getId());
        if (Authorization.isGAAorDGAA(currentUser)) {
            keywordNotifications = keywordNotificationRepository.findAll();
        }

        for (MarketCardNotification marketCardNotification : marketCardNotifications) {
            logger.debug("Market Card Notification (Id: {}) received.", marketCardNotification.getId());
            notificationPayloads.add(marketCardNotification.toMarketCardNotificationPayload());
        }
        for (KeywordNotification keywordNotification : keywordNotifications) {
            logger.debug("Keyword Notification (Id: {}) received.", keywordNotification.getId());
            notificationPayloads.add(keywordNotification.toKeywordNotificationPayload());
        }
        for (ListingNotification listingNotification : listingNotifications) {
            logger.debug("Listing Notification (Id: {}) received.", listingNotification.getId());
            notificationPayloads.add(listingNotification.toListingNotificationPayload());
        }

        return notificationPayloads;
    }

    /**
     * Retrieve all notifications for a business.
     *
     * @param id The ID of the business you'd like to retrieve the notifications for.
     * @param sessionToken The token used to identify the user.
     * @return List<SoldListingNotificationPayloads> The list of sold listing notifications for the business.
     * @throws Exception Exception
     */
    @GetMapping("/businesses/{id}/notifications")
    public List<SoldListingNotificationPayload> retrieveAllBusinessNotifications(
            @CookieValue(value = "JSESSIONID", required = false) String sessionToken, @PathVariable Integer id
    ) throws Exception {
        //401
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);
        logger.debug("User (Id: {}) received.", currentUser.getId());

        Authorization.verifyBusinessExists(id, businessRepository);

        Authorization.verifyBusinessAdmin(currentUser, id);

        List<SoldListingNotification> soldListingNotifications = soldListingNotificationRepository.findAllByBusinessId(id);
        List<SoldListingNotificationPayload> soldListingNotificationPayloads = new ArrayList<>();

        for (SoldListingNotification soldListingNotification : soldListingNotifications) {
            logger.debug("Sold Listing Notification received: {}", soldListingNotification);
            soldListingNotificationPayloads.add(soldListingNotification.toSoldListingNotificationPayload());
        }

        return soldListingNotificationPayloads;
    }

    /**
     * DELETE endpoint for deleting a notification, given the notification id.
     *
     * @param id The ID of the notification you'd like to delete.
     * @param sessionToken The token used to identify the user.
     * @throws Exception Exception
     */
    @DeleteMapping("/users/notifications/{id}")
    @Transactional
    @ResponseStatus(code = HttpStatus.OK, reason = "Keyword Successfully deleted")
    public void deleteNotification(
            @CookieValue(value = "JSESSIONID", required = false) String sessionToken, @PathVariable Integer id
    ) throws Exception {
        //401
        User currentUser = Authorization.getUserVerifySession(sessionToken, userRepository);
        logger.debug("User (Id: {}) received.", currentUser.getId());

        //403
        // Checks user is GAA/DGAA if notification is only for GAA/DGAA
        if (!Authorization.isGAAorDGAA(currentUser)) {
            logger.error("Notification Deletion Error - 403 [FORBIDDEN] - User doesn't have permissions to delete notification");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid permissions to delete notifications");
        }
        // Check user is user for other notifications
        // TODO

        logger.debug("Notification Deletion Update - User has permissions to delete notifications");

        Optional<Notification> notification = notificationRepository.findById(id);

        //406
        if (notification.isEmpty()) {
            logger.error("Notification Deletion Error - 400 [BAD_REQUEST] - Notification at ID {} not found", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification not found");
        }

        logger.debug("Notification Deletion Update - Notification found");

        // TODO: delete Notification


    }
}
