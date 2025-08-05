package com.jv.events.exception;

import com.jv.events.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCepInvalido(CepInvalidoException ex, WebRequest request) {
        logger.warn("CEP inválido: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "CEP Inválido",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ViaCepApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleViaCepApi(ViaCepApiException ex, WebRequest request) {
        logger.error("Erro na API ViaCEP: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Serviço Indisponível",
                "Erro ao consultar CEP. Tente novamente mais tarde.",
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(EventCreationException.class)
    public ResponseEntity<ErrorResponseDTO> handleEventCreation(EventCreationException ex, WebRequest request) {
        logger.error("Erro ao criar evento: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno",
                "Erro ao salvar evento. Tente novamente.",
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEventNotFound(EventNotFoundException ex, WebRequest request) {
        logger.warn("Evento não encontrado: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Evento Não Encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EventNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEventNameAlreadyExists(EventNameAlreadyExistsException ex,
            WebRequest request) {
        logger.warn("Nome de evento duplicado: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Nome de Evento Duplicado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Erro de validação: {}", ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos");

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Dados Inválidos",
                message,
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex, WebRequest request) {
        logger.error("Erro não tratado: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno",
                "Erro interno do servidor. Contate o suporte.",
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
