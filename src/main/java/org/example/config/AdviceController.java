package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;

@ControllerAdvice
public class AdviceController {
    @Autowired
    HttpServletResponse response;
    @ExceptionHandler(NoSuchElementException.class)
    public void notFind(NoSuchElementException ex){
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
