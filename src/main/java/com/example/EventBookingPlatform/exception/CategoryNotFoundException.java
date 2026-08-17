package com.example.EventBookingPlatform.exception;

public class CategoryNotFoundException extends RuntimeException {
  public CategoryNotFoundException(String message) {
    super(message);
  }
}
