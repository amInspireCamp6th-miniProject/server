package com.example.server.ocr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.server.global.exception.BusinessException;
import com.example.server.ingredient.dto.IngredientCreateRequest;
import com.example.server.ingredient.dto.IngredientResponse;
import com.example.server.ingredient.entity.Ingredient;
import com.example.server.ingredient.entity.StorageType;
import com.example.server.ingredient.repository.IngredientRepository;
import com.example.server.ocr.exception.OcrErrorCode;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
class OcrIngredientRegistrationServiceTest {

    @Autowired
    private OcrIngredientRegistrationService registrationService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @AfterEach
    void tearDown() {
        ingredientRepository.deleteAll();
    }

    @Test
    void registersIngredientWithJpegImageAndUserId() {
        IngredientResponse response = registrationService.register(
                7L,
                createRequest(),
                jpegImage());

        assertThat(response.imageUrl())
                .isEqualTo("/api/v1/ingredients/" + response.ingredientId() + "/image");

        Ingredient saved = ingredientRepository.findById(response.ingredientId()).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(7L);
        assertThat(saved.getImageData()).containsExactly(jpegData());
        assertThat(saved.getImageContentType()).isEqualTo("image/jpeg");
        assertThat(saved.getImageFileName()).isEqualTo("tofu.jpg");
    }

    @Test
    void registersIngredientWithPngImage() {
        IngredientResponse response = registrationService.register(
                1L,
                createRequest(),
                pngImage());

        Ingredient saved = ingredientRepository.findById(response.ingredientId()).orElseThrow();
        assertThat(saved.getImageData()).containsExactly(pngData());
        assertThat(saved.getImageContentType()).isEqualTo("image/png");
        assertThat(saved.getImageFileName()).isEqualTo("tofu.png");
    }

    @Test
    void rejectsInvalidContentTypeWithoutSaving() {
        MockMultipartFile textFile =
                new MockMultipartFile("image", "food.txt", "text/plain", "food".getBytes());

        assertInvalidImageDoesNotSave(textFile);
    }

    @Test
    void rejectsInvalidSignatureWithoutSaving() {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "image", "fake.jpg", "image/jpeg", new byte[] {0x00, 0x01, 0x02});

        assertInvalidImageDoesNotSave(invalidImage);
    }

    @Test
    void rejectsOversizedImageWithoutSaving() {
        MockMultipartFile oversizedImage = new MockMultipartFile(
                "image", "large.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1]);

        assertInvalidImageDoesNotSave(oversizedImage);
    }

    @Test
    void convertsImageReadFailureWithoutSaving() throws IOException {
        MultipartFile unreadableImage = mock(MultipartFile.class);
        given(unreadableImage.isEmpty()).willReturn(false);
        given(unreadableImage.getSize()).willReturn(3L);
        given(unreadableImage.getContentType()).willReturn("image/jpeg");
        given(unreadableImage.getBytes()).willThrow(new IOException("read failed"));

        assertInvalidImageDoesNotSave(unreadableImage);
    }

    private void assertInvalidImageDoesNotSave(MultipartFile image) {
        assertThatThrownBy(() -> registrationService.register(1L, createRequest(), image))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OcrErrorCode.INVALID_OCR_IMAGE);
        assertThat(ingredientRepository.count()).isZero();
    }

    private IngredientCreateRequest createRequest() {
        return new IngredientCreateRequest(
                "풀무원 국산콩 두부 300g",
                "두부",
                "가공식품",
                BigDecimal.ONE,
                "모",
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 10, 3),
                StorageType.REFRIGERATED);
    }

    private MockMultipartFile jpegImage() {
        return new MockMultipartFile("image", "tofu.jpg", "image/jpeg", jpegData());
    }

    private MockMultipartFile pngImage() {
        return new MockMultipartFile("image", "tofu.png", "image/png", pngData());
    }

    private byte[] jpegData() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    }

    private byte[] pngData() {
        return new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
    }
}
