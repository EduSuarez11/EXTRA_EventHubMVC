package es.daw.eventhubmvc.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConnectionApiRestException.class)
    public String handleConnectApiRestException(ConnectionApiRestException e, Model model){
        model.addAttribute("errorMessage", e.getMessage());
        return "api-error";
    }



}
