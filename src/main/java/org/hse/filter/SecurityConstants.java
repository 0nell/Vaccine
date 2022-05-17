package org.hse.filter;

public final class SecurityConstants {
    // JWT token defaults
    public static final String SECRET = "ShVmYp3s6v9y$B&E)H@McQfTjWnZr4t7w!z%C*F-JaNdRgUkXp2s5v8x/A?D(G+K";
    public static final long EXPIRATION_TIME = 1_800; // 30 mins
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String COOKIE_NAME = "JWT";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/signup";
}