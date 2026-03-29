package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.group.request.SendMessageRequest;
import com.togetherly.demo.data.group.response.MessageResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.service.group.MessageService;
import com.togetherly.demo.utils.AuthUtil;
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

@Controller
public class MessageController {

    @Autowired private MessageService messageService;

    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @RequestMapping(path = "/api/groups/{groupId}/messages", method = RequestMethod.POST)
    public ResponseEntity<?> sendMessage(
            @PathVariable("groupId") UUID groupId,
            @Valid @RequestBody SendMessageRequest request) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            MessageResponse msg = messageService.sendMessage(userId, groupId, request.content());
            return ResponseEntity.ok(msg);
        } catch (NotFound e) {
            return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @RequestMapping(path = "/api/groups/{groupId}/messages", method = RequestMethod.GET)
    public ResponseEntity<?> getMessages(@PathVariable("groupId") UUID groupId) {
        try {
            UUID userId = UUID.fromString(AuthUtil.currentUserDetail().getId());
            List<MessageResponse> messages = messageService.getMessages(userId, groupId);
            return ResponseEntity.ok(messages);
        } catch (NotFound e) {
            return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
