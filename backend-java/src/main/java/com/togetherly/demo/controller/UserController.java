package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.user.UserProfile;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.service.user.profile.ProfileService;
import com.togetherly.demo.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;

/**
 * User-facing controller for profile operations.
 *
 * Currently has one endpoint: get your own profile.
 * Path prefix: /api/user
 *
 * HAND-WRITTEN.
 */
@Controller
@RequestMapping(path = "/api/user")
public class UserController {
    @Autowired private ProfileService profileService;

    @Operation(summary = "get your profile", description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user profile",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfile.class)))
    })
    @RequestMapping(path = "/profile", method = RequestMethod.GET)
    public ResponseEntity<?> profile() {
        try {
            return ResponseEntity.ok(
                    profileService.getProfile(AuthUtil.currentUserDetail().getId()));
        } catch (UserDoesNotExist ex) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("unknown error, please try again later !"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "update your username", description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @RequestMapping(path = "/profile", method = RequestMethod.PUT)
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        try {
            String userId = AuthUtil.currentUserDetail().getId();
            String newUsername = body.get("username");
            if (newUsername == null || newUsername.isBlank()) {
                return new ResponseEntity<>(
                        new ErrorMessageResponse("username is required"),
                        HttpStatus.BAD_REQUEST);
            }
            profileService.updateUsername(userId, newUsername);
            return ResponseEntity.ok(profileService.getProfile(userId));
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        } catch (AlreadyExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()),
                    HttpStatus.CONFLICT);
        }
    }
}
