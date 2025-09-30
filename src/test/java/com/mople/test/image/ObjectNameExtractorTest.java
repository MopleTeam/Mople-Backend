package com.mople.test.image;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ObjectNameExtractorTest {

    private final String ns = "ax1e8qojvktg";
    private final String bucket = "DY_BUCKET";

    @Test
    void encodedSlash_isDecoded() {
        String url = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/" + ns
                + "/b/" + bucket + "/o/profile%2Fimg.jpg";
        String objectName = extractObjectName(url);
        assertThat(objectName).isEqualTo("profile/img.jpg");
        assertThat(deleteValid(url, objectName)).isTrue();
    }

    @Test
    void queryString_isIgnored() {
        String url = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/" + ns
                + "/b/" + bucket + "/o/folder%2Fdeep%2Fimg.png?X-Sig=abc&X-Date=123";
        String objectName = extractObjectName(url);
        assertThat(objectName).isEqualTo("folder/deep/img.png");
        assertThat(deleteValid(url, objectName)).isTrue();
    }

    @Test
    void uppercaseExtension_ok() {
        String url = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/" + ns
                + "/b/" + bucket + "/o/p%2FIMG.JPG";
        String objectName = extractObjectName(url);
        assertThat(objectName).isEqualTo("p/IMG.JPG");
        assertThat(deleteValid(url, objectName)).isTrue();
    }

    @Test
    void wrongHost_rejected() {
        String url = "https://cdn.example.com/n/" + ns + "/b/" + bucket + "/o/profile%2Fimg.jpg";
        String objectName = extractObjectName(url);
        assertThat(deleteValid(url, objectName)).isFalse();
    }

    @Test
    void wrongBucket_rejected() {
        String url = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/" + ns
                + "/b/OTHER/o/profile%2Fimg.jpg";
        String objectName = extractObjectName(url);
        assertThat(deleteValid(url, objectName)).isFalse();
    }

    @Test
    void noOsegment_skip() {
        String url = "https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/" + ns
                + "/b/" + bucket + "/profile%2Fimg.jpg";
        assertThat(extractObjectName(url)).isNull();
    }

    // 아래 메서드들은 네 실제 구현과 동일해야 함
    private String extractObjectName(String imageUrl) {
        try {
            var uri = java.net.URI.create(imageUrl);
            var rawPath = uri.getRawPath();
            int idx = rawPath.indexOf("/o/");
            if (idx < 0) return null;
            var encoded = rawPath.substring(idx + 3);
            return java.net.URLDecoder.decode(encoded, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { return null; }
    }

    private boolean deleteValid(String imageUrl, String objectName) {
        try {
            var uri = java.net.URI.create(imageUrl);
            var host = uri.getHost();
            if (host == null || !host.contains("objectstorage") || !host.endsWith("oraclecloud.com")) return false;

            var rawPath = uri.getRawPath();
            var ns = this.ns; var bucket = this.bucket;
            var mustPrefix = "/n/" + ns + "/b/" + bucket + "/o/";
            if (rawPath == null || !rawPath.startsWith(mustPrefix)) return false;

            var lower = objectName.toLowerCase(java.util.Locale.ROOT);
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        } catch (Exception e) { return false; }
    }
}
