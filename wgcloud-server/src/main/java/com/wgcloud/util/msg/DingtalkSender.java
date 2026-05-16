package com.wgcloud.util.msg;

import com.wgcloud.entity.DingtalkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DingtalkSender {

    private static final Logger logger = LoggerFactory.getLogger(DingtalkSender.class);

    public static void send(DingtalkConfig config, String title, String content) {
        try {
            String text = "[WGCLOUD] " + title + "\n" + content;
            String body = "{\"msgtype\":\"text\",\"text\":{\"content\":\"" + escapeJson(text) + "\"}}";

            String urlStr = config.getWebhookUrl();
            if (config.getSecret() != null && !config.getSecret().isEmpty()) {
                long timestamp = System.currentTimeMillis();
                String sign = genSign(config.getSecret(), timestamp);
                body = "{\"msgtype\":\"text\",\"text\":{\"content\":\"" + escapeJson(text) + "\"},\"timestamp\":\"" + timestamp + "\",\"sign\":\"" + sign + "\"}";
            }

            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            logger.debug("钉钉告警发送结果: {}", code);
        } catch (Exception e) {
            logger.error("发送钉钉告警失败", e);
        }
    }

    private static String genSign(String secret, long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal();
        return Base64.getEncoder().encodeToString(signData);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
