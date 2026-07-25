package com.hospi.manage.common.interfaces;

/** An email with a subject and plain-text body. */
public record EmailTemplate(String subject, String content) {
}
