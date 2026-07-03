package com.deliveryflow.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class IdentityHeaderRequestWrapper extends HttpServletRequestWrapper {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    private final Map<String, String> overrides = new LinkedHashMap<>();

    public IdentityHeaderRequestWrapper(HttpServletRequest request, String userId, String role) {
        super(request);
        if (userId != null) {
            overrides.put(USER_ID_HEADER, userId);
        }
        if (role != null) {
            overrides.put(USER_ROLE_HEADER, role);
        }
    }

    @Override
    public String getHeader(String name) {
        String override = findOverride(name);
        if (override != null) {
            return override;
        }
        if (isIdentityHeader(name)) {
            return null;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String override = findOverride(name);
        if (override != null) {
            return Collections.enumeration(Collections.singletonList(override));
        }
        if (isIdentityHeader(name)) {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            String name = original.nextElement();
            if (!isIdentityHeader(name)) {
                names.add(name);
            }
        }
        names.addAll(overrides.keySet());
        return Collections.enumeration(names);
    }

    private String findOverride(String name) {
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean isIdentityHeader(String name) {
        return USER_ID_HEADER.equalsIgnoreCase(name) || USER_ROLE_HEADER.equalsIgnoreCase(name);
    }
}
