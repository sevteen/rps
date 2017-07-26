package com.example.rps;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Beka Tsotsoria
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

}
