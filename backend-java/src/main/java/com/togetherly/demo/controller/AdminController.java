package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.ApiAllowsTo;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.PageList;
import com.togetherly.demo.data.PageOfUserProfile;
import com.togetherly.demo.data.PageRequest;
import com.togetherly.demo.data.admin.request.ChangeUserRoleRequest;
import com.togetherly.demo.data.admin.request.UserIdRequest;
import com.togetherly.demo.data.user.UserProfile;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.service.user.auth.ActivationService;
import com.togetherly.demo.service.user.auth.RoleService;
import com.togetherly.demo.service.user.profile.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Admin-only controller for user management.
 *
 * All endpoints require ADMIN role (enforced by @ApiAllowsTo).
 * Path prefix: /api/admin
 *
 * ENDPOINTS:
 * - POST /api/admin/changeRoleOf    — Change a user's role
 * - POST /api/admin/activateUser    — Re-enable a disabled account
 * - POST /api/admin/deactivateUser  — Disable an account
 * - GET  /api/admin/getUserList     — Paginated user list
 *
 * KEY CHANGES FROM ORIGINAL:
 * - Record accessors: .id() not .getId(), .role() not .getRole()
 * - PageOfUserProfile.from(pageList) factory instead of constructor
 *
 * HAND-WRITTEN.
 */
@Controller
@RequestMapping(path = "/api/admin")
public class AdminController {
    @Autowired private RoleService roleService;
    @Autowired private ActivationService activationService;
    @Autowired private ProfileService profileService;

    @Operation(summary = "change user role", description = "ADMIN only")
    @ApiAllowsTo(roles = Role.ADMIN)
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "user not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200",
                    description = "role changed, user tokens revoked", content = @Content)
    })
    @RequestMapping(path = "/changeRoleOf", method = RequestMethod.POST)
    public ResponseEntity<?> changeRole(@Valid @RequestBody ChangeUserRoleRequest request) {
        try {
            roleService.changeRoleOf(request.id(), Role.valueOf(request.role()));
            return ResponseEntity.ok().build();
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "activate user", description = "ADMIN only")
    @ApiAllowsTo(roles = Role.ADMIN)
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "user not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "user activated", content = @Content)
    })
    @RequestMapping(path = "/activateUser", method = RequestMethod.POST)
    public ResponseEntity<?> activateUser(@Valid @RequestBody UserIdRequest request) {
        try {
            activationService.activateUser(request.id());
            return ResponseEntity.ok().build();
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "deactivate user", description = "ADMIN only")
    @ApiAllowsTo(roles = Role.ADMIN)
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "user not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200",
                    description = "user deactivated, tokens revoked", content = @Content)
    })
    @RequestMapping(path = "/deactivateUser", method = RequestMethod.POST)
    public ResponseEntity<?> deactivateUser(@Valid @RequestBody UserIdRequest request) {
        try {
            activationService.deactivateUser(request.id());
            return ResponseEntity.ok().build();
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "get paginated user list", description = "ADMIN only")
    @ApiAllowsTo(roles = Role.ADMIN)
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "paginated user profiles",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageOfUserProfile.class)))
    })
    @RequestMapping(path = "/getUserList", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUserProfiles(@ParameterObject @Valid PageRequest request) {
        PageList<UserProfile> pageList =
                profileService.getAllUserProfilesWithPage(request.page(), request.size());
        return ResponseEntity.ok(PageOfUserProfile.from(pageList));
    }
}
