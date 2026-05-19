package com.tony.demo.modules.notification.api.dto;

public record NotificationTemplateDTO(
    String code,
    String titleEn,
    String titleVi,
    String contentEn,
    String contentVi
) {}
