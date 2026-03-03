package com.togetherly.demo.controller;

import com.togetherly.demo.config.JwtConfig;
import com.togetherly.demo.config.ResetPasswordURL;
import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.controller.constraint.rate.LimitTarget;
import com.togetherly.demo.controller.constraint.rate.RateLimit;
import com.togetherly.demo.data.ErrorMessageResponse;
import com.togetherly.demo.data.auth.AccessTokenSpec;
import com.togetherly.demo.data.auth.TokenPair;
import com.togetherly.demo.data.auth.TokenResponse;
import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.data.auth.VerificationKey;
import com.togetherly.demo.data.auth.VerificationPair;
import com.togetherly.demo.data.auth.request.ChangePasswordRequest;
import com.togetherly.demo.data.auth.request.ForgetPasswordRequest;
import com.togetherly.demo.data.auth.request.IntrospectionRequest;
import com.togetherly.demo.data.auth.request.IssueVerificationCodeRequest;
import com.togetherly.demo.data.auth.request.LoginRequest;
import com.togetherly.demo.data.auth.request.RefreshRequest;
import com.togetherly.demo.data.auth.request.RegisterRequest;
import com.togetherly.demo.data.auth.request.ResetPasswordRequest;
import com.togetherly.demo.data.user.UserProfile;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.InvalidTokenException;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.auth.VerifyToken;
import com.togetherly.demo.service.email.EmailService;
import com.togetherly.demo.service.jwt.JwtService;
import com.togetherly.demo.service.user.auth.LoginService;
import com.togetherly.demo.service.user.auth.PasswordService;
import com.togetherly.demo.service.user.auth.RegistrationService;
import com.togetherly.demo.service.verification.VerificationService;
import com.togetherly.demo.utils.AuthUtil;
import com.togetherly.demo.utils.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Authentication controller — the main entry point for auth operations.
 *
 * ENDPOINTS (10 total):
 * - POST /api/auth/login              — Login, get tokens as JSON
 * - POST /web/api/auth/login          — Login, get tokens as HTTP-only cookies
 * - POST /api/auth/refresh            — Refresh tokens via JSON
 * - POST /web/api/auth/refresh        — Refresh tokens via cookies
 * - POST /api/auth/logout             — Logout (revoke current token)
 * - POST /api/auth/introspect         — Decode/check an access token
 * - POST /api/auth/register           — Register new user
 * - POST /api/auth/issueVerificationCode — Get email verification code
 * - POST /api/auth/changePassword     — Change password (authenticated, rate-limited)
 * - POST /api/auth/forgetPassword     — Request password reset email
 * - POST /api/auth/resetPassword      — Reset password with token
 *
 * WHY TWO LOGIN/REFRESH ENDPOINTS?
 * /api/ endpoints return tokens as JSON (for mobile apps, API clients).
 * /web/api/ endpoints store tokens in HTTP-only cookies (for browser SPAs).
 * HTTP-only cookies can't be read by JavaScript → protects against XSS.
 *
 * KEY CHANGES FROM ORIGINAL:
 * - Record accessors: .username() not .getUsername(), .password() not .getPassword()
 * - TokenResponse.from(tokenPair) factory instead of new TokenResponse(tokenPair)
 * - TokenPair record: .accessToken() not .getAccessToken()
 * - UserProfile.from(user) factory instead of new UserProfile(user)
 *
 * HAND-WRITTEN — you define your API endpoints here.
 */
@Controller
public class AuthController {
    @Autowired private LoginService loginService;
    @Autowired private JwtService jwtService;
    @Autowired private JwtConfig jwtConfig;
    @Autowired private RegistrationService registrationService;
    @Autowired private PasswordService passwordService;
    @Autowired private VerificationService verificationService;
    @Autowired private EmailService emailService;
    @Autowired private ResetPasswordURL resetPasswordURL;

    // --- LOGIN (JSON response) ---

    @Operation(summary = "login and get JWT tokens", description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "auth failure",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "tokens",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)))
    })
    @RequestMapping(path = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserDetail userDetail = loginService.login(request.username(), request.password());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            return ResponseEntity.ok(TokenResponse.from(tokenPair));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage() + " !"), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("unknown error, please try again later !"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- LOGIN (cookie response for browser SPAs) ---

    @Operation(summary = "login and get tokens in HTTP-only cookies",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "auth failure",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "tokens in cookies",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(hidden = true)),
                    headers = {
                            @Header(name = "access_token", description = "HTTP-only cookie"),
                            @Header(name = "refresh_token", description = "HTTP-only cookie")
                    })
    })
    @RequestMapping(path = "/web/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> webLogin(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            UserDetail userDetail = loginService.login(request.username(), request.password());
            TokenPair tokenPair = jwtService.issueTokens(userDetail);
            // Set tokens as HTTP-only cookies (browser can't read via JS → XSS protection)
            response.addCookie(CookieUtils.create(
                    "refresh_token", tokenPair.refreshToken().getToken(),
                    jwtConfig.getCookieDomain(), jwtConfig.getRefreshTokenLifetimeSec(), true));
            response.addCookie(CookieUtils.create(
                    "access_token", tokenPair.accessToken().getToken(),
                    jwtConfig.getCookieDomain(), jwtConfig.getAccessTokenLifetimeSec(), true));
            return new ResponseEntity<>(
                    Collections.singletonMap("access_token", tokenPair.accessToken().getToken()),
                    HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage() + " !"), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("unknown error, please try again later !"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- REFRESH (JSON) ---

    @Operation(summary = "exchange refresh token for new token pair",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "invalid refresh token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "new tokens",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)))
    })
    @RequestMapping(path = "/api/auth/refresh", method = RequestMethod.POST)
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            TokenPair tokenPair = jwtService.refreshTokens(request.token());
            return ResponseEntity.ok(TokenResponse.from(tokenPair));
        } catch (InvalidTokenException | InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- REFRESH (cookie) ---

    @Operation(summary = "refresh tokens via cookie",
            description = "allowed to everyone, protected by CORS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "invalid refresh token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "new tokens in cookies",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(hidden = true)),
                    headers = {
                            @Header(name = "access_token", description = "HTTP-only cookie"),
                            @Header(name = "refresh_token", description = "HTTP-only cookie")
                    })
    })
    @RequestMapping(path = "/web/api/auth/refresh", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> webRefresh(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) {
        Map<String, String> responseBody = new HashMap<>();
        try {
            TokenPair tokenPair = jwtService.refreshTokens(refreshToken);
            responseBody.put("access_token", tokenPair.accessToken().getToken());
            response.addCookie(CookieUtils.create(
                    "refresh_token", tokenPair.refreshToken().getToken(),
                    jwtConfig.getCookieDomain(), jwtConfig.getRefreshTokenLifetimeSec(), true));
            response.addCookie(CookieUtils.create(
                    "access_token", tokenPair.accessToken().getToken(),
                    jwtConfig.getCookieDomain(), jwtConfig.getAccessTokenLifetimeSec(), true));
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (InvalidTokenException | InvalidOperation e) {
            responseBody.put("message", e.getMessage());
            return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
        }
    }

    // --- LOGOUT ---

    @Operation(summary = "logout (revoke current access token)",
            description = "requires authentication")
    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "logged out", content = @Content)
    })
    @RequestMapping(path = "/api/auth/logout", method = RequestMethod.POST)
    public ResponseEntity<?> logout(HttpServletResponse response) {
        jwtService.revokeAccessToken(AuthUtil.currentUserDetail().getCurrentAccessTokenID());
        // Clear cookies
        response.addCookie(
                CookieUtils.removed("access_token", jwtConfig.getCookieDomain(), true));
        response.addCookie(
                CookieUtils.removed("refresh_token", jwtConfig.getCookieDomain(), true));
        return ResponseEntity.ok().build();
    }

    // --- INTROSPECT ---

    @Operation(summary = "decode and validate an access token",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "invalid token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "decoded token claims",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessTokenSpec.class)))
    })
    @RequestMapping(path = "/api/auth/introspect", method = RequestMethod.POST)
    public ResponseEntity<?> introspect(@Valid @RequestBody IntrospectionRequest request) {
        try {
            return new ResponseEntity<>(jwtService.introspect(request.token()), HttpStatus.OK);
        } catch (InvalidTokenException e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // --- REGISTER ---

    @Operation(summary = "register a new user", description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "registration failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200", description = "registered user profile",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfile.class)))
    })
    @RequestMapping(path = "/api/auth/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = registrationService.registerUser(
                    request.username(), request.password(),
                    request.email(), request.verification());
            return ResponseEntity.ok(UserProfile.from(user));
        } catch (AlreadyExist | InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- ISSUE VERIFICATION CODE ---

    @Operation(summary = "send verification code to email",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "verification key",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VerificationKey.class)))
    })
    @RequestMapping(path = "/api/auth/issueVerificationCode", method = RequestMethod.POST)
    public ResponseEntity<?> issueVerificationCode(
            @Valid @RequestBody IssueVerificationCodeRequest request) {
        VerificationPair verificationPair =
                verificationService.issueVerificationCode(request.email());
        return ResponseEntity.ok(new VerificationKey(verificationPair.key()));
    }

    // --- CHANGE PASSWORD ---

    @Operation(summary = "change your password",
            description = "authenticated, rate-limited: 10/hour per user")
    @AuthenticatedApi
    @RateLimit(target = LimitTarget.USER, key = "/api/auth/changePassword",
            limit = 10, period = 3600)
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "change failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200",
                    description = "success — all tokens revoked", content = @Content)
    })
    @RequestMapping(path = "/api/auth/changePassword", method = RequestMethod.POST)
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            passwordService.changePasswordOf(
                    AuthUtil.currentUserDetail().getId(),
                    request.oldPassword(), request.newPassword());
            return ResponseEntity.ok().build();
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (UserDoesNotExist e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("unknown error, please try again later !"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- FORGET PASSWORD ---

    @Operation(summary = "request password reset link via email",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "request failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200",
                    description = "reset link sent to email", content = @Content)
    })
    @RequestMapping(path = "/api/auth/forgetPassword", method = RequestMethod.POST)
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        try {
            VerifyToken verifyToken =
                    passwordService.requestResetPasswordToken(request.email());
            emailService.sendSimpleEmail(
                    request.email(),
                    "Your Reset Password Link",
                    "click the link to reset your password:\n"
                            + resetPasswordURL.getUrlPrefix()
                            + verifyToken.getToken());
            return ResponseEntity.ok().build();
        } catch (UserDoesNotExist | InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    // --- RESET PASSWORD ---

    @Operation(summary = "reset password using token from email",
            description = "allowed to everyone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "reset failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageResponse.class))),
            @ApiResponse(responseCode = "200",
                    description = "success — all tokens revoked", content = @Content)
    })
    @RequestMapping(path = "/api/auth/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok().build();
        } catch (InvalidOperation e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
