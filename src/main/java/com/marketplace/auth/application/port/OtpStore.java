package com.marketplace.auth.application.port;

public interface OtpStore {

    /** Sinh OTP mới cho email, lưu với TTL. Trả về mã OTP. */
    String generate(String email);

    /** Kiểm tra OTP; nếu đúng thì xóa luôn (dùng 1 lần). */
    boolean verify(String email, String otp);

    /** Bắt đầu cooldown resend cho email. Trả về false nếu đang trong thời gian cooldown. */
    boolean tryStartResendCooldown(String email);
}
