package com.hospi.manage.common.exception;

/** Thrown when a requested resource (entity) cannot be found by its identifier. */
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;

    public ResourceNotFoundException(String resource) {
        super(resource + " not found");
        this.resource = resource;
    }

    public String getResource() { return resource; }
}