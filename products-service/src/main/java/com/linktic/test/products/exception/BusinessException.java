//archivo: src/main/java/com/linktic/test/products/exception/BusinessException.java
package com.linktic.test.products.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}