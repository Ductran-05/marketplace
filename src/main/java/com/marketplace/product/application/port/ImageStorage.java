package com.marketplace.product.application.port;

import java.io.InputStream;

public interface ImageStorage {

    /** Lưu object vào storage với key cho trước. */
    void store(String key, InputStream content, long size, String contentType);

    /** Sinh URL tạm có chữ ký để client tải object trực tiếp từ storage. */
    String presignedUrl(String key);

    void delete(String key);
}
