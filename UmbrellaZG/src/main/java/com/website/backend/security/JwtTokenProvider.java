package com.website.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

	@Value("${app.jwtSecret}")
	private String jwtSecret;

	@Value("${app.jwtExpirationMs}")
	private int jwtExpirationMs;

	public String generateToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		return generateTokenWithExpiration(authentication, jwtExpirationMs);
	}

	/**
	 * 生成具有自定义过期时间的JWT令牌
	 * @param authentication 认证信息
	 * @param expirationMs 过期时间(毫秒)
	 * @return JWT令牌
	 */
	public String generateTokenWithExpiration(Authentication authentication, long expirationMs) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		return Jwts.builder()
			.subject(userPrincipal.getUsername())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationMs))
			.signWith(getSignInKey())
			.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(authToken);
			return true;
		}
		catch (JwtException e) {
			logger.error("JWT无效: {}", e.getMessage());
		}
		catch (IllegalArgumentException e) {
			logger.error("JWT令牌参数非法: {}", e.getMessage());
		}
		return false;
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}