package de.fred4jupiter.fredbet.image;

import java.util.Base64;

public record BinaryImage(String key, byte[] imageBinary) {

    public String getAsBase64() {
        if (imageBinary == null) {
            return "";
        }

        String mimeType = isPng(imageBinary) ? "image/png" : "image/jpeg";
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBinary);
    }

    private static boolean isPng(byte[] bytes) {
        return bytes.length >= 4
            && bytes[0] == (byte) 0x89
            && bytes[1] == (byte) 0x50  // 'P'
            && bytes[2] == (byte) 0x4E  // 'N'
            && bytes[3] == (byte) 0x47; // 'G'
    }
}
