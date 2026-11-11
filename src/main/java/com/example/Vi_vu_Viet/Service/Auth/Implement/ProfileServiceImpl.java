package com.example.Vi_vu_Viet.Service.Auth.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Service.Auth.ProfileService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.dto.Request.Auth.UpdateProfileRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.ProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProfileResponse> getProfile(UUID userId) {
        return toResponse(userRepository.findById(userId).orElseThrow(this::userNotFound));
    }

    @Override
    @Transactional
    public BaseResponse<ProfileResponse> updateProfile(UUID userId, UpdateProfileRequest request) {
        if (request.getFullName() == null && request.getEmail() == null && request.getPhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide at least one profile field");
        }
        UserAccount user = userRepository.findByIdForUpdate(userId).orElseThrow(this::userNotFound);
        if (request.getEmail() != null) {
            String email = request.getEmail().toLowerCase(Locale.ROOT);
            if (userRepository.existsByEmailExcludingUser(email, userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
            }
            user.setEmail(email);
        }
        if (request.getPhone() != null) {
            String phone = request.getPhone();
            if (userRepository.existsByPhoneExcludingUser(phone, userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
            }
            if (!Objects.equals(phone, user.getPhone())) {
                user.setPhone_verified_at(null);
            }
            user.setPhone(phone);
        }
        if (request.getFullName() != null) {
            user.setFull_name(request.getFullName());
        }
        // Flush here so uniqueness conflicts are returned by this request.
        return toResponse(userRepository.saveAndFlush(user));
    }

    private BaseResponse<ProfileResponse> toResponse(UserAccount user) {
        ProfileResponse profile = new ProfileResponse();
        profile.setId(user.getId());
        profile.setFullName(user.getFull_name());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setAccountStatus(user.getAccount_status());
        profile.setPhoneVerifiedAt(user.getPhone_verified_at());
        profile.setCreatedAt(user.getCreated_at());
        profile.setUpdatedAt(user.getUpdated_at());
        BaseResponse<ProfileResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(profile);
        return response;
    }

    private ResponseStatusException userNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found");
    }
}
