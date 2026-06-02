package com.hospi.manage.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String resource;

    public ResourceNotFoundException(String resource) {
        super(resource + " not found");
        this.resource = resource;
    }

    public String getResource() { return resource; }
}