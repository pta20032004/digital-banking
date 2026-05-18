package com.tony.demo.modules.user.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;
import com.tony.demo.modules.user.domain.UserStatus;
import com.tony.demo.modules.user.domain.UserKycDocument;
import com.tony.demo.modules.user.domain.UserKycDocumentRepository;
import com.tony.demo.modules.user.dto.PendingUserDto;
import com.tony.demo.modules.user.dto.PendingUsersResponse;
import com.tony.demo.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserKycDocumentRepository userKycDocumentRepository;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket.kyc}")
    private String kycBucketName;

    @Override
    public PendingUsersResponse getAllPendingUsers() {
        long totalPending = userRepository.countByKycStatus(UserStatus.PENDING);
        List<User> pendingUsers = userRepository.findAllByKycStatus(UserStatus.PENDING);

        List<PendingUserDto> userDtos = pendingUsers.stream().map(user -> {
            UserKycDocument kycDoc = userKycDocumentRepository.findByUser(user).orElse(null);
            
            return PendingUserDto.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .identityNumber(user.getIdentityNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .idCardFrontUrl(kycDoc != null ? kycDoc.getIdCardFrontUrl() : null)
                    .idCardBackUrl(kycDoc != null ? kycDoc.getIdCardBackUrl() : null)
                    .selfieUrl(kycDoc != null ? kycDoc.getSelfieUrl() : null)
                    .build();
        }).collect(Collectors.toList());

        return new PendingUsersResponse(totalPending, userDtos);
    }

    @Override
    public byte[] getKycDocumentImage(String fileName) {
        log.info("Downloading file {} from bucket {}", fileName, kycBucketName);
        return s3Service.downloadFile(kycBucketName, fileName);
    }
}
