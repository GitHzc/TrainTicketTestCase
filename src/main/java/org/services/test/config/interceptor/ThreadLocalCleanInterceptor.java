package org.services.test.config.interceptor;

import org.services.test.cache.ThreadLocalCache;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ThreadLocalCleanInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable
            Exception ex) throws Exception {
        ThreadLocalCache.threadLocalClean();
    }
}
