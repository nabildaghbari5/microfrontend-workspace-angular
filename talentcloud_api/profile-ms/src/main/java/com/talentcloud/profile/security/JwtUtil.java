//package com.talentcloud.profile.security;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.interfaces.DecodedJWT;
//
//public class JwtUtil {
//
//    public static String getClaim(String token, String claim) {
//        try {
//            DecodedJWT jwt = JWT.decode(token.replace("Bearer ", ""));
//            return jwt.getClaim(claim).asString();
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public static String getUserId(String token) {
//        return getClaim(token, "sub");
//    }
//
//    public static String getUsername(String token) {
//        return getClaim(token, "preferred_username");
//    }
//
//    public static String getEmail(String token) {
//        return getClaim(token, "email");
//    }
//}
