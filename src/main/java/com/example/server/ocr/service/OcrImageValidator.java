package com.example.server.ocr.service;

import com.example.server.global.exception.BusinessException;
import com.example.server.ocr.exception.OcrErrorCode;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OcrImageValidator {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png");

    public byte[] validateAndRead(MultipartFile image) {
        if (image == null
                || image.isEmpty()
                || image.getSize() > MAX_IMAGE_SIZE
                || !ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
            throw new BusinessException(OcrErrorCode.INVALID_OCR_IMAGE);
        }

        byte[] imageData = readImage(image);
        if (!hasValidImageSignature(imageData)) {
            throw new BusinessException(OcrErrorCode.INVALID_OCR_IMAGE);
        }
        return imageData;
    }

    private byte[] readImage(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(OcrErrorCode.INVALID_OCR_IMAGE);
        }
    }

    private boolean hasValidImageSignature(byte[] bytes) {
        boolean isJpeg = bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
        boolean isPng = bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
        return isJpeg || isPng;
    }
}
