// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.stack.rest;

import org.apache.doris.stack.exception.AuthorizationException;
import org.apache.doris.stack.exception.BadRequestException;
import org.apache.doris.stack.exception.HdfsUnknownHostException;
import org.apache.doris.stack.exception.HdfsUrlException;
import org.apache.doris.stack.exception.NoPermissionException;
import lombok.extern.slf4j.Slf4j;

import org.apache.doris.stack.exception.UserNoSelectClusterException;
import org.apache.doris.stack.exception.UsernameDuplicateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description：Unified exception handling
 */
@ControllerAdvice
@Slf4j
public class RestApiExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(AuthorizationException.class)
    @ResponseBody
    public Object unauthorizedHandler(HttpServletRequest request, AuthorizationException e) {
        log.error("authorized exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.unauthorized(msg);
    }

    @ExceptionHandler(NoPermissionException.class)
    @ResponseBody
    public Object noPermissionHandler(HttpServletRequest request, NoPermissionException e) {
        log.error("no permission exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.noPermission(msg);
    }

    @ExceptionHandler(UserNoSelectClusterException.class)
    @ResponseBody
    public Object noSpaceHandler(HttpServletRequest request, UserNoSelectClusterException e) {
        log.error("no space exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.noSpace(msg);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public Object badRequestExceptionHandler(HttpServletRequest request, BadRequestException e) {
        log.error("bad request exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.badRequest(msg);
    }

    @ExceptionHandler(HdfsUnknownHostException.class)
    @ResponseBody
    public Object hdfsHostUnknownExceptionHandler(HttpServletRequest request, HdfsUnknownHostException e) {
        log.error("palo data import hdfs host unknown exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.okWithCommonError(msg, RestApiStatusCode.HDFS_HOST_ERROR);
    }

    @ExceptionHandler(HdfsUrlException.class)
    @ResponseBody
    public Object hdfsUrlErrorExceptionHandler(HttpServletRequest request, HdfsUrlException e) {
        log.error("palo data import hdfs url error exception", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.okWithCommonError(msg, RestApiStatusCode.HDFS_URL_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object unexpectedExceptionHandler(HttpServletRequest request, Exception e) {
        log.error("common exception:{}", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.okWithCommonError(msg);
    }

    @ExceptionHandler(UsernameDuplicateException.class)
    @ResponseBody
    public Object usernameDuplicateExceptionHandler(HttpServletRequest request, UsernameDuplicateException e) {
        log.error("Username Duplicate Exception:{}", e);
        String msg = getMessage(e.getMessage(), request);
        return ResponseEntityBuilder.usernameDuplicateRequest(msg);
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    @ResponseBody
    public Object noSuchElementExceptionHandler(HttpServletRequest request, Exception e) {
        log.error("common exception:{}", e);
        String msg = "AccessDataNotFound";
        return ResponseEntityBuilder.okWithCommonError(msg, RestApiStatusCode.NOT_FOUND);
    }

    // get exception msg
    private String getMessage(String key, HttpServletRequest request) {
        Locale locale =  currentLocale(request);
        String msg;
        try {
            msg  = messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            msg = key;
        }
        if (locale.equals(Locale.CHINA)) {
            msg = "错误：" + msg;
        } else {
            msg = "ERROR:" + msg;
        }
        return msg;
    }

    // get locale language
    private Locale currentLocale(HttpServletRequest request) {
        String locale = request.getParameter("lang");
        if ("en".equalsIgnoreCase(locale)) {
            return Locale.ENGLISH;
        } else {
            return Locale.CHINA;
        }
    }
}
