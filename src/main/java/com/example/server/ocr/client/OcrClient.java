package com.example.server.ocr.client;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** 외부 OCR 업체가 바뀌어도 Service를 수정하지 않도록 만든 공통 호출 규격이다. */
public interface OcrClient {

    List<OcrTextBlock> extractText(MultipartFile image);
}
