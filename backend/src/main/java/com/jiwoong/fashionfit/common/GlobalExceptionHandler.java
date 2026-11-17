package com.jiwoong.fashionfit.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.jiwoong.fashionfit.analysis.AnalysisFailedException;

// 컨트롤러에서 터진 예외를 한 곳에서 받아 ErrorResponse 로 바꾼다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검사 실패 → 400 + 입력칸별 메시지
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("입력값을 확인해 주세요", fieldErrors));
    }

    // JSON 형식이 깨졌거나 숫자 자리에 글자가 온 경우 → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("요청 형식이 올바르지 않아요"));
    }

    // 서비스에서 검사한 규칙 위반 → 400
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getFieldErrors()));
    }

    // 없는 데이터 → 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e.getMessage()));
    }

    // 업로드 파일이 application.yml 의 max-file-size(5MB)를 넘음 → 400
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("이미지를 확인해 주세요",
                java.util.Map.of("image", "5MB 이하 이미지만 올릴 수 있어요")));
    }

    // multipart 요청에 image 필드가 없음 → 400
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("이미지를 확인해 주세요",
                java.util.Map.of("image", "스크린샷을 올려 주세요")));
    }

    // 외부 LLM 호출 실패 → 502 Bad Gateway (우리 서버가 아니라 바깥 서비스 문제라는 뜻)
    @ExceptionHandler(AnalysisFailedException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisFailed(AnalysisFailedException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.of(e.getMessage()));
    }
}
