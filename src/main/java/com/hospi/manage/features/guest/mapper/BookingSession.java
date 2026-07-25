package com.hospi.manage.features.guest.mapper;

import com.hospi.manage.features.guest.dto.BookingDraft;
import jakarta.servlet.http.HttpSession;

import static com.hospi.manage.common.constant.Attributes.BOOKING_DRAFT;

/** Static utility: get or create the session-scoped BookingDraft. */
public final class BookingSession {

    private BookingSession() {}

    /**
     * Return the {@link BookingDraft} associated with the current session, creating one if absent.
     *
     * @param session HTTP session holding the draft attribute
     * @return the existing or newly created booking draft
     */
    public static BookingDraft getDraft(HttpSession session) {
        BookingDraft draft = (BookingDraft) session.getAttribute(BOOKING_DRAFT);
        if (draft == null) {
            draft = new BookingDraft();
            session.setAttribute(BOOKING_DRAFT, draft);
        }
        return draft;
    }
}
