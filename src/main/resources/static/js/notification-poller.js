function startNotificationPolling(intervalMs = 30000) {
    async function fetchCount() {
        try {
            const res = await fetch('/api/notifications/unread-count', {
                credentials: 'same-origin'
            });
            const { count } = await res.json();
            updateNotificationBadge(count);
        } catch (e) {
            // silent fail
        }
    }
    fetchCount(); // initial
    return setInterval(fetchCount, intervalMs);
}

function updateNotificationBadge(count) {
    const badges = document.querySelectorAll('[data-notification-badge]');
    badges.forEach(b => {
        if (count > 0) {
            b.textContent = count;
            b.classList.remove('hidden');
            b.classList.add('pill-warning');
        } else {
            b.classList.add('hidden');
        }
    });
}

function markAllRead() {
    fetch('/api/notifications/mark-all-read', {
        method: 'POST',
        credentials: 'same-origin'
    }).then(() => updateNotificationBadge(0));
}