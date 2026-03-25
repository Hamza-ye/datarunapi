package org.nmcpye.datarun.sharedkernal.uidgenerate;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class CodeGenerator {

    public static final String letters = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String ALLOWED_CHARS = "0123456789" + letters;

    public static final int NUMBER_OF_CODEPOINTS = ALLOWED_CHARS.length();

    public static final int CODESIZE = 11;

    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z]{1}[a-zA-Z0-9]{10}$");

    /**
     * @author Hamza 03/06/2025
     */
    public static class ULIDGenerator {

        private static final String ULID_REGEX = "^[0-9A-HJKMNP-TV-Z]{26}$";
        private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-7[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

        /**
         * Generate a ULID (Universally Unique Lexicographically Sortable Identifier).
         * Length: 26 characters, Base32 encoded.
         * Ideal for distributed systems requiring monotonicity.
         */
        public static String nextString() {
            return UlidCreator.getUlid().toString();
            // return ulid.nextValue().toString();
        }

        public static UUID resolveToUuid(String input) {
            if (input == null)
                return null;

            // 1. If it's already a UUID string, parse it directly
            if (input.length() == 36) {
                return UUID.fromString(input);
            }

            // 2. If it's a ULID (26 chars), convert it
            if (input.length() == 26 && input.matches(ULID_REGEX)) {
                return convertUlidToUuid(input);
            }

            throw new IllegalArgumentException("Invalid identifier format");
        }

        private static UUID convertUlidToUuid(String ulid) {
            // manual decoding logic here to get the 128-bit bits
            // and return new UUID(mostSigBits, leastSigBits);
            return Ulid.from(ulid).toUuid();
        }

        /**
         * validate a String is valid ULID or not.
         */
        public static boolean isValidUlid(String ulid) {
            return Ulid.isValid(ulid);
        }
    }

    public static UUID resolveToUuid(String id) {
        return ULIDGenerator.resolveToUuid(id);
    }

    public static String nextUlid() {
        return ULIDGenerator.nextString();
    }

    /**
     * Generates a UID according to the following rules:
     * <ul>
     * <li>Alphanumeric characters only.</li>
     * <li>Exactly 11 characters long.</li>
     * <li>First character is alphabetic.</li>
     * </ul>
     *
     * @return a UID.
     */
    public static String generateUid() {
        return generateCode(CODESIZE);
    }

    /**
     * Generates a pseudo random string with alphanumeric characters.
     *
     * @param codeSize the number of characters in the code.
     * @return the code.
     */
    public static String generateCode(int codeSize) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        char[] randomChars = new char[codeSize];

        // First char should be a letter
        randomChars[0] = letters.charAt(r.nextInt(letters.length()));

        for (int i = 1; i < codeSize; ++i) {
            randomChars[i] = ALLOWED_CHARS.charAt(r.nextInt(NUMBER_OF_CODEPOINTS));
        }

        return new String(randomChars);
    }

    /**
     * Generates a cryptographically strong random token encoded in Base64
     *
     * @param lengthInBytes length in bytes of the token
     * @return a Base64 encoded string of the token
     */
    public static String getRandomSecureToken(int lengthInBytes) {
        SecureRandom sr = new SecureRandom();
        byte[] tokenBytes = new byte[lengthInBytes];
        sr.nextBytes(tokenBytes);

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(tokenBytes);
    }

    /**
     * Tests whether the given code is a valid UID.
     *
     * @param code the code to validate.
     * @return true if the code is valid.
     */
    public static boolean isValidUid(String code) {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }
}
