package com.jqwik.demo;

public class EmailMasker {

    public static String mask(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            String maskedLocal = local.charAt(0) + "*";
            return maskedLocal + "@" + domain;
        }

        String prefix = local.substring(0, 1);
        String suffix = local.substring(local.length() - 1);
        String maskedLocal = prefix + "*".repeat(local.length() - 2) + suffix;
        return maskedLocal + "@" + domain;
    }
}
