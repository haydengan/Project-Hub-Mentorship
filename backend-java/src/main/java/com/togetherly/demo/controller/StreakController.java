package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.activity.response.GroupLeaderboardEntry;
import com.togetherly.demo.data.activity.response.StreakResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.service.activity.StreakService;
import com.togetherly.demo.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Streak controller — streak tracking and leaderboards.
 *
 * ENDPOINTS (2 total):
 * - GET /api/users/me/streaks          — My streaks across all activities
 * - GET /api/groups/{groupId}/streaks  — Group leaderboard by streak
 *
 * All endpoints require authentication.
 */
@Controller
public class StreakController {

    @Autowired private StreakService streakService;

    // --- MY STREAKS ---

    @Operation(summary = "get all my streaks across activities",
            description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "list of streaks",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StreakResponse.class)))
    })
    @RequestMapping(path = "/api/users/me/streaks", method = RequestMethod.GET)
    public ResponseEntity<?> getMyStreaks() {
        UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
        List<StreakResponse> streaks = streakService.getMyStreaks(userId);
        return ResponseEntity.ok(streaks);
    }

    // --- GROUP LEADERBOARD ---

    @Operation(summary = "get streak leaderboard for a group",
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
            @ApiResponse(responseCode = "200", description = "leaderboard entries",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupLeaderboardEntry.class)))
    })
    @RequestMapping(path = "/api/groups/{groupId}/streaks", method = RequestMethod.GET)
    public ResponseEntity<?> getGroupLeaderboard(@PathVariable("groupId") UUID groupId) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            List<GroupLeaderboardEntry> leaderboard =
                    streakService.getGroupLeaderboard(userId, groupId);
            return ResponseEntity.ok(leaderboard);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
