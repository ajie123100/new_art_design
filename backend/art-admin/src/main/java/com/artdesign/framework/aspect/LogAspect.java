package com.artdesign.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.enums.BusinessStatus;
import com.artdesign.common.utils.ServletUtils;
import com.artdesign.system.domain.entity.SysOperLog;
import com.artdesign.system.service.SysOperLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LogAspect {
    private static final int MAX_TEXT_LENGTH = 4000;

    private final SysOperLogService operLogService;
    private final ObjectMapper objectMapper;

    public LogAspect(SysOperLogService operLogService, ObjectMapper objectMapper) {
        this.operLogService = operLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.artdesign.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            record(joinPoint, result, error, System.currentTimeMillis() - startTime);
        }
    }

    private void record(ProceedingJoinPoint joinPoint, Object result, Throwable error, long costTime) {
        Log log = getLog(joinPoint);
        if (log == null) {
            return;
        }

        HttpServletRequest request = getRequest();
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle(log.title());
        operLog.setBusinessType(log.businessType().getCode());
        operLog.setMethod(getMethodName(joinPoint));
        operLog.setRequestMethod(request == null ? "" : request.getMethod());
        operLog.setOperName(getLoginName());
        operLog.setOperUrl(request == null ? "" : request.getRequestURI());
        operLog.setOperIp(ServletUtils.getClientIp(request));
        operLog.setOperLocation("内网IP");
        operLog.setOperTime(LocalDateTime.now());
        operLog.setCostTime(costTime);
        operLog.setStatus(error == null ? BusinessStatus.SUCCESS.getCode() : BusinessStatus.FAIL.getCode());

        if (log.saveRequestData()) {
            operLog.setOperParam(toJson(joinPoint.getArgs()));
        }
        if (log.saveResponseData() && error == null) {
            operLog.setJsonResult(toJson(result));
        }
        if (error != null) {
            operLog.setErrorMsg(truncate(error.getMessage()));
        }

        operLogService.record(operLog);
    }

    private Log getLog(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(Log.class);
    }

    private String getMethodName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()";
    }

    private String getLoginName() {
        if (!StpUtil.isLogin()) {
            return "";
        }
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId == null ? "" : String.valueOf(loginId);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String toJson(Object value) {
        try {
            if (value instanceof Object[] args) {
                return truncate(objectMapper.writeValueAsString(Arrays.stream(args)
                        .filter(arg -> !(arg instanceof HttpServletRequest))
                        .toList()));
            }
            return truncate(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH);
    }
}
