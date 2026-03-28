package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.group.request.CreateGroupRequest;
import com.togetherly.demo.data.group.request.JoinGroupRequest;
import com.togetherly.demo.data.group.response.GroupDetailResponse;
import com.togetherly.demo.data.group.response.GroupResponse;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.service.group.GroupService;
import com.togetherly.demo.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Group controller — manages accountability groups.
 *
 * ENDPOINTS (5 total):
 * - POST   /api/groups          — Create a new group
 * - GET    /api/groups          — List user's groups
 * - GET    /api/groups/{id}     — Get group details + members
 * - POST   /api/groups/join     — Join a group via invite code
 * - DELETE /api/groups/{id}     — Leave a group
 *
 * All endpoints require authentication.
 */
@Controller
public class GroupController {

    @Autowired private GroupService groupService;

    // --- CREATE GROUP ---

    @Operation(summary = "create a new accountability group",
            description = "authenticated user becomes the group admin")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "group created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupDetailResponse.class)))
    })
    @RequestMapping(path = "/api/groups", method = RequestMethod.POST)
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
        GroupDetailResponse group = groupService.createGroup(
                userId, request.name(), request.description());
        return ResponseEntity.ok(group);
    }

    // --- LIST MY GROUPS ---

    @Operation(summary = "list all groups the current user belongs to",
            description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "list of groups",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupResponse.class)))
    })
    @RequestMapping(path = "/api/groups", method = RequestMethod.GET)
    public ResponseEntity<?> getMyGroups() {
        UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
        List<GroupResponse> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    // --- GET GROUP DETAIL ---

    @Operation(summary = "get group details including all members",
            description = "requires authentication and group membership")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "group not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "not a member",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "group details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupDetailResponse.class)))
    })
    @RequestMapping(path = "/api/groups/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getGroupDetail(@PathVariable("id") UUID id) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            GroupDetailResponse group = groupService.getGroupDetail(userId, id);
            return ResponseEntity.ok(group);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- JOIN GROUP ---

    @Operation(summary = "join a group using an invite code",
            description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "invalid invite code",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "already a member or group full",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "joined group",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupResponse.class)))
    })
    @RequestMapping(path = "/api/groups/join", method = RequestMethod.POST)
    public ResponseEntity<?> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            GroupResponse group = groupService.joinGroup(userId, request.inviteCode());
            return ResponseEntity.ok(group);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (AlreadyExist | InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- LEAVE GROUP ---

    @Operation(summary = "leave a group",
            description = "requires authentication and group membership")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "group not found or not a member",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "cannot leave (e.g., only admin)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "left group", content = @Content)
    })
    @RequestMapping(path = "/api/groups/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> leaveGroup(@PathVariable("id") UUID id) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            groupService.leaveGroup(userId, id);
            return ResponseEntity.ok().build();
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
