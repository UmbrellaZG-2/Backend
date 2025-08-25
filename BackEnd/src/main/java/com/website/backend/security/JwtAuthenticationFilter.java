package com.website.backend.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT认证过滤器 用于从HTTP请求中提取JWT令牌，验证其有效性，并设置用户认证信息到SecurityContext
 * 该过滤器继承自OncePerRequestFilter，确保每个请求只被过滤一次
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter extends OncePerRequestFilter implements Ordered {

	private final JwtTokenProvider tokenProvider;

	private final UserDetailsService userDetailsService;

	/**
	 * 构造函数注入依赖
	 * @param tokenProvider JWT令牌提供器，用于生成和验证JWT令牌
	 * @param userDetailsService 用户详情服务，用于加载用户信息
	 */
	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * 执行过滤器逻辑 从请求头中提取JWT令牌，验证其有效性，如果有效则设置用户认证信息
	 * @param request HTTP请求对象，用于获取请求头中的JWT令牌
	 * @param response HTTP响应对象
	 * @param filterChain 过滤器链，用于继续执行后续过滤器
	 * @throws ServletException 如果发生Servlet相关异常
	 * @throws IOException 如果发生IO异常
	 */
	// 公开路径列表，与SecurityConfig中的配置保持一致
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/articles",
        "/api/articles/*",
        "/api/articles/search",
        "/api/articles/get/*",
        "/api/articles/category/get/*",
        "/api/articles/*/comments/get",
        "/api/articles/*/comments/put",
        "/api/articles/categories/get",
        "/api/home",
        "/api/home/**",
        "/api/auth/get",
        "/api/auth/login",
        "/api/auth/admin/login",
        "/api/auth/guest/login",
        "/api/auth/admin/register",
        "/api/attachments/download",
        "/api/attachments/download/*",
        "/api/attachments/article/get",
        "/api/attachments/article/get/*",
        "/api/images/article/*/cover/get",
        "/api/images/article/*/getAll",
        "/api/tags",
        "/api/tags/*",
        "/api/test/get",
        "/api/test/post"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 检查是否为公开路径
		String requestURI = request.getRequestURI();
		
		log.info("检查请求URI: {}", requestURI);
		boolean isPublicPath = false;
		for (String publicPath : PUBLIC_PATHS) {
			log.info("匹配路径: {} 与 {}", publicPath, requestURI);
			if (pathMatcher.match(publicPath, requestURI)) {
				log.info("匹配成功: {} 与 {}", publicPath, requestURI);
				isPublicPath = true;
				break;
			}
		}
		
		if (isPublicPath) {
			filterChain.doFilter(request, response);
			return;
		}

		// 非公开路径，需要JWT认证
		try {
			String jwt = parseJwt(request);
			log.info("解析JWT令牌: {}", jwt);
			if (jwt != null && tokenProvider.validateJwtToken(jwt)) {
				log.info("JWT令牌验证成功");
				String username = tokenProvider.getUserNameFromJwtToken(jwt);
				log.info("从JWT令牌中提取用户名: {}", username);

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				log.info("加载用户详情: {}", userDetails);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.info("设置认证信息成功");
				filterChain.doFilter(request, response);
			} else {
				log.info("未提供有效的JWT令牌");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Unauthorized: No valid JWT token provided");
			}
		}
		catch (Exception e) {
			log.error("JWT处理异常: {}", e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Unauthorized: Invalid JWT token");
		}
	}

	/**
	 * 从HTTP请求头中提取JWT令牌
	 * @param request HTTP请求对象
	 * @return 提取的JWT令牌，如果请求头中没有有效的JWT令牌则返回null
	 */
	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}

		return null;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}