package com.xzp.forum.aspect;

import com.google.common.base.Stopwatch;

import com.xzp.forum.util.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志记录处理
 *
 * @author huangcong
 */
@Aspect
@Component
public class InterfaceAspect {

    @Resource
    private HttpServletRequest request;

    /**
     * 切点拦截controller层所有接口
     */
    @Pointcut("execution(public * com.xzp.forum.controller..*.*(..))")
    public void webLog() {
    }

    /**
     * 日志切面处理逻辑
     */
    @Around("webLog()")
    public Object webLogHandler(ProceedingJoinPoint joinPoint) throws Throwable {

        Map<String, Object> webLog = new HashMap<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object result = joinPoint.proceed();
        long takeTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        webLog.put("FTakeTime", (int)takeTime);
        webLog.put("FCreateTime", LocalDateTime.now(ZoneOffset.UTC).toString());
        webLog.put("FIPv4", IpUtils.getIpAddr(request));
        System.out.println(webLog);
        return result;
    }

}
