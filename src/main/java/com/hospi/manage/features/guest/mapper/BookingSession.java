package com.hospi.manage.features.guest.mapper;

import com.hospi.manage.features.guest.dto.BookingDraft;
import jakarta.servlet.http.HttpSession;

import static com.hospi.manage.common.constant.Attributes.BOOKING_DRAFT;

public final class BookingSession {

    private BookingSession() {}

    public static BookingDraft getDraft(HttpSession session) {
        BookingDraft draft = (BookingDraft) session.getAttribute(BOOKING_DRAFT);
        if (draft == null) {
            draft = new BookingDraft();
            session.setAttribute(BOOKING_DRAFT, draft);
        }
        return draft;
    }
}
