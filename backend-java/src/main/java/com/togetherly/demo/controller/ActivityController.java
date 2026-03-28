package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.activity.request.CreateActivityRequest;
import com.togetherly.demo.data.activity.request.LogActivityRequest;
import com.togetherly.demo.data.activity.response.ActivityLogResponse;
import com.togetherly.demo.data.activity.response.ActivityResponse;
import com.togetherly.demo.data.activity.response.DailyGroupSummary;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.service.activity.ActivityService;
import com.togetherly.demo.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Activity & logging controller.
 *
 * ENDPOINTS (5 total):
 * - POST /api/groups/{groupId}/activities         — Create activity in group
 * - GET  /api/groups/{groupId}/activities          — List group activities
 * - POST /api/logs                                 — Log an activity entry
 * - GET  /api/groups/{groupId}/logs/today          — Today's logs for group
 * - GET  /api/groups/{groupId}/logs/week           — Weekly summary for group
 *
 * All endpoints require authentication.
 */
@Controller
public class ActivityController {

    @Autowired private ActivityService activityService;

    // --- CREATE ACTIVITY ---

    @Operation(summary = "create a new activity within a group",
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
            @ApiResponse(responseCode = "403", description = "not a member or duplicate name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "activity created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActivityResponse.class)))
    })
    @RequestMapping(path = "/api/groups/{groupId}/activities", method = RequestMethod.POST)
    public ResponseEntity<?> createActivity(
            @PathVariable("groupId") UUID groupId,
            @Valid @RequestBody CreateActivityRequest request) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            ActivityResponse activity = activityService.createActivity(
                    userId, groupId, request.name(), request.type());
            return ResponseEntity.ok(activity);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation | AlreadyExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- LIST GROUP ACTIVITIES ---

    @Operation(summary = "list all activities for a group",
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
            @ApiResponse(responseCode = "200", description = "list of activities",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActivityResponse.class)))
    })
    @RequestMapping(path = "/api/groups/{groupId}/activities", method = RequestMethod.GET)
    public ResponseEntity<?> getGroupActivities(@PathVariable("groupId") UUID groupId) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            List<ActivityResponse> activities = activityService.getGroupActivities(userId, groupId);
            return ResponseEntity.ok(activities);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- LOG ACTIVITY ---

    @Operation(summary = "log an activity entry for today",
            description = "requires authentication and membership in the activity's group")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "activity not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "not a member or already logged",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "activity logged",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActivityLogResponse.class)))
    })
    @RequestMapping(path = "/api/logs", method = RequestMethod.POST)
    public ResponseEntity<?> logActivity(@Valid @RequestBody LogActivityRequest request) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            ActivityLogResponse log = activityService.logActivity(
                    userId, request.activityId(), request.durationMins(), request.note());
            return ResponseEntity.ok(log);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation | AlreadyExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- TODAY'S GROUP LOGS ---

    @Operation(summary = "get today's activity logs for all group members",
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
            @ApiResponse(responseCode = "200", description = "today's summary",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DailyGroupSummary.class)))
    })
    @RequestMapping(path = "/api/groups/{groupId}/logs/today", method = RequestMethod.GET)
    public ResponseEntity<?> getGroupLogsToday(@PathVariable("groupId") UUID groupId) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            DailyGroupSummary summary = activityService.getGroupLogsForDate(
                    userId, groupId, LocalDate.now());
            return ResponseEntity.ok(summary);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- WEEKLY GROUP LOGS ---

    @Operation(summary = "get activity logs for a group over a date range",
            description = "requires authentication and group membership. "
                    + "Defaults to the last 7 days if no dates provided.")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "group not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "weekly summary",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DailyGroupSummary.class)))
    })
    @RequestMapping(path = "/api/groups/{groupId}/logs/week", method = RequestMethod.GET)
    public ResponseEntity<?> getGroupLogsWeek(
            @PathVariable("groupId") UUID groupId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());

            // Default to last 7 days
            if (endDate == null) endDate = LocalDate.now();
            if (startDate == null) startDate = endDate.minusDays(6);

            List<DailyGroupSummary> summaries = activityService.getGroupLogsForWeek(
                    userId, groupId, startDate, endDate);
            return ResponseEntity.ok(summaries);
        } catch (NotFound e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
