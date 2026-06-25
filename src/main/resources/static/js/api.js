export async function fetchThemes() {
    const res = await fetch('/themes');
    if (!res.ok) throw new Error('Failed to fetch themes');
    const data = await res.json();
    return data;
}

export async function login(payload) {
    const res = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({
            loginId: payload.loginId,
            password: payload.password
        })
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to login');
    }
    return res.json();
}

export async function signup(payload) {
    const res = await fetch('/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to signup');
    }
    return res.json();
}

export async function fetchMe() {
    const res = await fetch('/auth/me', { credentials: 'same-origin' });
    if (res.status === 401) {
        return null;
    }
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to fetch me');
    }
    return res.json();
}

export async function requireManager() {
    const me = await fetchMe();
    if (!me) {
        window.location.href = `/login?next=${encodeURIComponent(window.location.pathname)}`;
        return null;
    }
    if (me.role !== 'MANAGER') {
        window.location.href = '/';
        return null;
    }
    return me;
}

export async function logout() {
    const res = await fetch('/auth/logout', {
        method: 'POST',
        credentials: 'same-origin'
    });
    if (!res.ok && res.status !== 204) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to logout');
    }
}

export async function fetchRankedThemes(days = 7, limit = 10) {
    const res = await fetch(`/themes/rank`);
    if (!res.ok) throw new Error('Failed to fetch ranked themes');
    const data = await res.json();
    return data;
}

export async function createTheme(payload) {
    const res = await fetch('/admin/themes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to create theme');
    return res.json();
}

export async function deleteTheme(id) {
    const res = await fetch(`/admin/themes/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete theme');
    return res;
}

export async function fetchReservationTimes() {
    const res = await fetch('/admin/times');
    if (!res.ok) throw new Error('Failed to fetch times');
    const data = await res.json();
    return data;
}

export async function createReservationTime(startAt) {
    const res = await fetch('/admin/times', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startAt })
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create time');
    }
    return res.json();
}

export async function deleteReservationTime(id) {
    const res = await fetch(`/admin/times/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete time');
    return res;
}

export async function fetchReservationDates() {
    const res = await fetch('/reservation-dates');
    if (!res.ok) throw new Error('Failed to fetch dates');
    const data = await res.json();
    return data;
}

export async function fetchAdminReservationDates() {
    const res = await fetch('/admin/reservation-dates');
    if (!res.ok) throw new Error('Failed to fetch admin dates');
    const data = await res.json();
    return data;
}

export async function createReservationDate(playDay) {
    const res = await fetch('/admin/reservation-dates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playDay })
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create date');
    }
    return res.json();
}

export async function deleteReservationDate(id) {
    const res = await fetch(`/admin/reservation-dates/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete date');
    return res;
}

export async function fetchReservationSlots(themeId, dateId) {
    const url = (themeId && dateId) ? `/reservation-slots?themeId=${themeId}&dateId=${dateId}` : '/reservation-slots';
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch slots');
    const data = await res.json();
    return data;
}

export async function createReservationSlot(payload) {
    const res = await fetch('/admin/reservation-slots', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create slot');
    }
    return res.json();
}

export async function createReservation(payload) {
    const res = await fetch('/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create reservation');
    }
    return res.json();
}

export async function fetchMyReservations() {
    const res = await fetch('/reservations', { credentials: 'same-origin' });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to fetch my reservations');
    }
    const data = await res.json();
    return data;
}

export async function cancelMyReservation(id) {
    const res = await fetch(`/reservations/${id}`, {
        method: 'DELETE',
        credentials: 'same-origin'
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to cancel reservation');
    }
    return null;
}

export async function fetchAdminReservations() {
    const res = await fetch('/admin/reservations');
    if (!res.ok) throw new Error('Failed to fetch admin reservations');
    const data = await res.json();
    return data;
}

export async function fetchAdminWaitingReservations() {
    const res = await fetch('/admin/waiting-reservations');
    if (!res.ok) throw new Error('Failed to fetch admin waiting reservations');
    return res.json();
}

export async function deleteAdminWaitingReservation(id) {
    const res = await fetch(`/admin/waiting-reservations/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to cancel admin waiting reservation');
    return res;
}

export async function fetchAdminMembers() {
    const res = await fetch('/admin/members');
    if (!res.ok) throw new Error('Failed to fetch members');
    return res.json();
}

export async function createAdminReservation(payload) {
    const res = await fetch('/admin/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create admin reservation');
    }
    return res.json();
}

export async function deleteAdminReservation(id) {
    const res = await fetch(`/admin/reservations/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete admin reservation');
    return res;
}
