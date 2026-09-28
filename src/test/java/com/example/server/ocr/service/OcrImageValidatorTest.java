package com.example.server.ocr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.server.global.exception.BusinessException;
import com.example.server.ocr.exception.OcrErrorCode;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class OcrImageValidatorTest {

    private final OcrImageValidator validator = new OcrImageValidator();

    @Mock
    private MultipartFile unreadableImage;

    @Test
    void acceptsJpegSignature() {
        byte[] jpegData = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile image =
                new MockMultipartFile("image", "food.jpg", "image/jpeg", jpegData);

        assertThat(validator.validateAndRead(image)).containsExactly(jpegData);
    }

    @Test
    void acceptsPngSignature() {
        byte[] pngData = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile image =
                new MockMultipartFile("image", "food.png", "image/png", pngData);

        assertThat(validator.validateAndRead(image)).containsExactly(pngData);
    }

    @Test
    void rejectsInvalidContentType() {
        MockMultipartFile image =
                new MockMultipartFile("image", "food.txt", "text/plain", "food".getBytes());

        assertInvalidImage(image);
    }

    @Test
    void rejectsImageLargerThanFiveMegabytes() {
        byte[] oversizedData = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile image =
                new MockMultipartFile("image", "large.jpg", "image/jpeg", oversizedData);

        assertInvalidImage(image);
    }

    @Test
    void rejectsInvalidImageSignature() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "fake.jpg", "image/jpeg", new byte[] {0x00, 0x01, 0x02});

        assertInvalidImage(image);
    }

    @Test
    void convertsIOExceptionToInvalidImageError() throws IOException {
        given(unreadableImage.isEmpty()).willReturn(false);
        given(unreadableImage.getSize()).willReturn(3L);
        given(unreadableImage.getContentType()).willReturn("image/jpeg");
        given(unreadableImage.getBytes()).willThrow(new IOException("read failed"));

        assertInvalidImage(unreadableImage);
    }

    private void assertInvalidImage(MultipartFile image) {
        assertThatThrownBy(() -> validator.validateAndRead(image))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OcrErrorCode.INVALID_OCR_IMAGE);
    }
}
