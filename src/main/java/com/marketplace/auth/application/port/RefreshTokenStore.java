package com.marketplace.auth.application.port;

import com.marketplace.auth.domain.model.UserId;

public interface RefreshTokenStore {

    /** Lưu refresh token hiện hành của user (ghi đè token cũ → mỗi user 1 token). */
    void save(UserId userId, String token);

    /** Token có khớp với token đang lưu của user không. */
    boolean matches(UserId userId, String token);

    void revoke(UserId userId);
}
