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

package org.apache.doris.stack.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.doris.stack.constant.EnvironmentDefine;
import org.apache.doris.stack.constant.PropertyDefine;
import org.apache.doris.stack.exception.ConfigItemException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description：spring service startup, initializing property system configuration information
 */
@Slf4j
public class CommonPropertyUtil {

    private CommonPropertyUtil() {
        throw new UnsupportedOperationException();
    }

    // When starting the program, read some environment variable values into the properties configuration
    private static final String DB_TYPE = System.getenv(EnvironmentDefine.DB_TYPE_ENV);

    private static final String DB_DBNAME = System.getenv(EnvironmentDefine.DB_DBNAME_ENV);

    private static final String DB_PORT = System.getenv(EnvironmentDefine.DB_PORT_ENV);

    private static final String DB_USER = System.getenv(EnvironmentDefine.DB_USER_ENV);

    private static final String DB_PASS = System.getenv(EnvironmentDefine.DB_PASS_ENV);

    private static final String DB_HOST = System.getenv(EnvironmentDefine.DB_HOST_ENV);

    private static final String STUDIO_IP = System.getenv(EnvironmentDefine.STUDIO_IP_ENV);

    private static final String STUDIO_PORT = System.getenv(EnvironmentDefine.STUDIO_PORT_ENV);

    private static final String ENCRYPT_KEY = System.getenv(EnvironmentDefine.ENCRYPT_KEY_ENV);

    private static final String NGINX_PORT = System.getenv(EnvironmentDefine.NGINX_PORT_ENV);

    private static final String STUDIO_COOKIE_MAX_AGE = System.getenv(EnvironmentDefine.STUDIO_COOKIE_MAX_AGE_ENV);

    private static final String SUPER_PASSWORD_RESER = System.getenv(EnvironmentDefine.SUPER_PASSWORD_RESER_ENV);

    private static final String MAX_LOGIN_FAILED_TIMES = System.getenv(EnvironmentDefine.MAX_LOGIN_FAILED_TIMES_ENV);

    private static final String LOGIN_DELAY_TIME = System.getenv(EnvironmentDefine.LOGIN_DELAY_TIME_ENV);

    private static final String MAX_LOGIN_TIMES_IN_FIVE_MINUTES = System.getenv(EnvironmentDefine.MAX_LOGIN_TIMES_IN_FIVE_MINUTES_ENV);

    private static final String MAX_LOGIN_TIMES = System.getenv(EnvironmentDefine.MAX_LOGIN_TIMES_ENV);

    private static final String PROMETHEUS_HOME = System.getenv(EnvironmentDefine.PROMETHEUS_HOME_ENV);

    private static final String PROMETHEUS_HOST = System.getenv(EnvironmentDefine.PROMETHEUS_HOST_ENV);

    private static final String PROMETHEUS_PORT = System.getenv(EnvironmentDefine.PROMETHEUS_PORT_ENV);

    private static final String DEPLOY_TYPE = System.getenv(EnvironmentDefine.DEPLOY_TYPE_ENV);

    private static final String WEB_ACCEPT_COUNT = System.getenv(EnvironmentDefine.WEB_ACCEPT_COUNT_ENV);

    private static final String WEB_MAX_THREADS = System.getenv(EnvironmentDefine.WEB_MAX_THREADS_ENV);

    private static final String WEB_MIN_SPARE_THREADS = System.getenv(EnvironmentDefine.WEB_MIN_SPARE_THREADS_ENV);

    private static final String WEB_MAX_CONNECTIONS = System.getenv(EnvironmentDefine.WEB_MAX_CONNECTIONS_ENV);

    private static final String DB_MAX_POOL_SIZE = System.getenv(EnvironmentDefine.DB_MAX_POOL_SIZE_ENV);

    private static final String DB_MIN_IDLE = System.getenv(EnvironmentDefine.DB_MIN_IDLE_ENV);

    private static final String H2_FILE_PATH = System.getenv(EnvironmentDefine.H2_FILE_PATH_ENV);

    private static final String LOG_PATH = System.getenv(EnvironmentDefine.LOG_PATH_ENV);

    public static Map<String, Object> getProperties() throws ConfigItemException {
        Map<String, Object> properties = new HashMap<>();
        // log path configuration

        if (LOG_PATH != null && !LOG_PATH.isEmpty()) {
            properties.put("logging.path", LOG_PATH);
        }

        // Service port configuration
        if (STUDIO_PORT == null || STUDIO_PORT.isEmpty()) {
            properties.put(PropertyDefine.SERVER_PORT_PROPERTY, 8080);
        } else {
            properties.put(PropertyDefine.SERVER_PORT_PROPERTY, STUDIO_PORT);
        }

        if (STUDIO_IP == null || STUDIO_IP.isEmpty()) {
            log.debug("STUDIO_IP is empty,Manger IP will be used automatically");
        } else {
            AddressVerification addressVerification = new AddressVerification();
            if (addressVerification.IpVerification(STUDIO_IP)) {
                properties.put(PropertyDefine.SERVER_IP_PROPERTY, STUDIO_IP);
            } else {
                throw new ConfigItemException("config item [STUDIO_IP] is invalid");
            }
        }

        if (ENCRYPT_KEY == null || ENCRYPT_KEY.isEmpty()) {
            log.error("config item [ENCRYPT_KEY] is not set");
            throw new ConfigItemException("config item [ENCRYPT_KEY] is not set");
        } else if (ENCRYPT_KEY.length() != CredsUtil.getAesKeyStrLen()) {
            log.error("encrypt key {} string length is not {}", ENCRYPT_KEY, CredsUtil.getAesKeyStrLen());
            throw new ConfigItemException("config item [ENCRYPT_KEY] is not correct");
        } else {
            log.debug("set encrypt key: " + ENCRYPT_KEY);
            CredsUtil.setEncryptKey(ENCRYPT_KEY);
        }

        // Nginx service port configuration
        if (NGINX_PORT == null || NGINX_PORT.isEmpty()) {
            properties.put(PropertyDefine.NGINX_PORT_PROPERTY, 8090);
        } else {
            properties.put(PropertyDefine.NGINX_PORT_PROPERTY, NGINX_PORT);
        }
        properties.put("spring.messages.basename", "locale.exception_message");
        // Database JPA configuration
        properties.put("spring.jpa.properties.hibernate.jdbc.time_zone", "Asia/Shanghai");
        properties.put("spring.jpa.show-sql", true);
        properties.put("spring.jpa.hibernate.ddl-auto", "update");
        properties.put("spring.jpa.hibernate.naming-strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        properties.put("spring.data.jpa.repositories.enabled", true);
        properties.put("spring.jpa.hibernate.use-new-id-generator-mappings", false);

        properties.put("spring.cache.type", "ehcache");
        properties.put("spring.cache.ehcache.config", "classpath:cache/ehcache.xml");

        // database connection pool configuration
        if (DB_MAX_POOL_SIZE != null) {
            properties.put(PropertyDefine.MAX_POOL_SIZE_PROPERTY, DB_MAX_POOL_SIZE);
        } else {
            properties.put("spring.datasource.maximum-pool-size", 20);
        }

        if (DB_MIN_IDLE != null) {
            properties.put(PropertyDefine.MIN_IDLE_PROPERTY, DB_MIN_IDLE);
        } else {
            properties.put("spring.datasource.minimum-idle", 10);
        }

        //default mysql
        if (StringUtils.isEmpty(DB_TYPE) || DB_TYPE.equals(PropertyDefine.JPA_DATABASE_MYSQL)) {
            // Configure MySQL database access
            properties.put(PropertyDefine.JPA_DATABASE_PROPERTY, PropertyDefine.JPA_DATABASE_MYSQL);
            properties.put("spring.datasource.driverClassName", "com.mysql.cj.jdbc.Driver");
            StringBuffer url = new StringBuffer();
            url.append("jdbc:mysql://");
            if (StringUtils.isEmpty(DB_HOST)) {
                url.append("127.0.0.1");
                properties.put(PropertyDefine.MYSQL_HOST_PROPERTY, "127.0.0.1");
            } else {
                url.append(DB_HOST);
                properties.put(PropertyDefine.MYSQL_HOST_PROPERTY, DB_HOST);
            }
            url.append(":");
            if (StringUtils.isEmpty(DB_PORT)) {
                url.append("3306");
                properties.put(PropertyDefine.MYSQL_PORT_PROPERTY, 3306);
            } else {
                url.append(DB_PORT);
                properties.put(PropertyDefine.MYSQL_PORT_PROPERTY, DB_PORT);
            }
            url.append("/");
            if (StringUtils.isEmpty(DB_DBNAME)) {
                url.append("test1");
            } else {
                url.append(DB_DBNAME);
            }
            url.append("?useUnicode=true&characterEncoding=UTF-8");

            properties.put("spring.datasource.url", url.toString());

            if (StringUtils.isEmpty(DB_USER)) {
                properties.put("spring.datasource.username", "root");
            } else {
                properties.put("spring.datasource.username", DB_USER);
            }

            if (StringUtils.isEmpty(DB_PASS)) {
                properties.put("spring.datasource.password", "testPass");
            } else {
                properties.put("spring.datasource.password", DB_PASS);
            }
            properties.put("spring.jpa.properties.hibernate.dialect", "org.apache.doris.stack.util.DorisMySQL5InnoDBDialect");
        } else if (DB_TYPE.equals(PropertyDefine.JPA_DATABASE_H2)) {
            // Configure H2 database access
            // Relative path saved in local file
            properties.put(PropertyDefine.JPA_DATABASE_PROPERTY, PropertyDefine.JPA_DATABASE_H2);
            // properties.put("spring.datasource.url", "jdbc:h2:mem:h2test");// Save only in memory
            String h2FilePath = "jdbc:h2:file:./studio";
            if (!StringUtils.isEmpty(H2_FILE_PATH)) {
                h2FilePath = "jdbc:h2:file:" + H2_FILE_PATH;
            }
            properties.put("spring.datasource.url", h2FilePath);
            properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
            properties.put("spring.datasource.username", "studio");
            properties.put("spring.datasource.password", "app#studio@123");

            // Initializing table creation
            properties.put("spring.h2.console.settings.web-allow-others", true);
            properties.put("spring.h2.console.path", "/h2-console/studio");
            properties.put("spring.h2.console.enabled", true);

            properties.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        } else {
            // Configure PostgreSQL database access
            properties.put(PropertyDefine.JPA_DATABASE_PROPERTY, PropertyDefine.JPA_DATABASE_POSTGRESQL);
            properties.put("spring.datasource.driverClassName", "org.postgresql.Driver");

            StringBuffer url = new StringBuffer();
            url.append("jdbc:postgresql://");
            if (StringUtils.isEmpty(DB_HOST)) {
                url.append("127.0.0.1");
                properties.put(PropertyDefine.POSTGRESQL_HOST_PROPERTY, "127.0.0.1");
            } else {
                url.append(DB_HOST);
                properties.put(PropertyDefine.POSTGRESQL_HOST_PROPERTY, DB_HOST);
            }
            url.append(":");
            if (StringUtils.isEmpty(DB_PORT)) {
                url.append("8432");
                properties.put(PropertyDefine.POSTGRESQL_PORT_PROPERTY, 8432);
            } else {
                url.append(DB_PORT);
                properties.put(PropertyDefine.POSTGRESQL_PORT_PROPERTY, DB_PORT);
            }
            url.append("/");
            if (StringUtils.isEmpty(DB_DBNAME)) {
                url.append("test");
            } else {
                url.append(DB_DBNAME);
            }

            properties.put("spring.datasource.url", url.toString());

            if (StringUtils.isEmpty(DB_USER)) {
                properties.put("spring.datasource.username", "root");
            } else {
                properties.put("spring.datasource.username", DB_USER);
            }

            if (StringUtils.isEmpty(DB_PASS)) {
                properties.put("spring.datasource.password", "testPass");
            } else {
                properties.put("spring.datasource.password", DB_PASS);
            }
            properties.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        }

        // Log configuration
        properties.put("spring.profiles.active", "dev");
        properties.put("logging.level.org.apache.doris.stack", "debug");

        // Configure cookie validity (minutes)
        if (STUDIO_COOKIE_MAX_AGE == null) {
            // The default is 14 days
            properties.put(PropertyDefine.MAX_SESSION_AGE_PROPERTY, "20160");
        } else {
            properties.put(PropertyDefine.MAX_SESSION_AGE_PROPERTY, STUDIO_COOKIE_MAX_AGE);
        }

        // Configure whether the super administrator user password is reset
        if (SUPER_PASSWORD_RESER == null) {
            // The default is false
            properties.put(PropertyDefine.SUPER_USER_PASS_RESET_PROPERTY, false);
        } else {
            properties.put(PropertyDefine.SUPER_USER_PASS_RESET_PROPERTY, SUPER_PASSWORD_RESER);
        }

        // Configure maximum failed logins
        if (MAX_LOGIN_FAILED_TIMES == null) {
            // The default is 5 times
            properties.put(PropertyDefine.MAX_LOGIN_FAILED_TIMES_PROPERTY, 5);
        } else {
            properties.put(PropertyDefine.MAX_LOGIN_FAILED_TIMES_PROPERTY, MAX_LOGIN_FAILED_TIMES);
        }

        // Failed to configure login account disabling time
        if (LOGIN_DELAY_TIME == null) {
            // The default is 5 minutes
            properties.put(PropertyDefine.LOGIN_DELAY_TIME_PROPERTY, 300000);
        } else {
            properties.put(PropertyDefine.LOGIN_DELAY_TIME_PROPERTY, LOGIN_DELAY_TIME);
        }

        //Configure the number of people online within five minutes
        if (MAX_LOGIN_TIMES_IN_FIVE_MINUTES == null) {
            // The default is 1000
            properties.put(PropertyDefine.MAX_LOGIN_TIMES_IN_FIVE_MINUTES_PROPERTY, 1000);
        } else {
            properties.put(PropertyDefine.MAX_LOGIN_TIMES_IN_FIVE_MINUTES_PROPERTY, MAX_LOGIN_TIMES_IN_FIVE_MINUTES);
        }

        // Configure the number of people online at the same time
        if (MAX_LOGIN_TIMES == null) {
            // The default is 5000
            properties.put(PropertyDefine.MAX_LOGIN_TIMES_PROPERTY, 5000);
        } else {
            properties.put(PropertyDefine.MAX_LOGIN_TIMES_PROPERTY, MAX_LOGIN_TIMES);
        }

        if (PROMETHEUS_HOME == null) {
            // It is possible to start manager through IDE during development.
            log.warn("The environment variable 'PROMETHEUS_DIR' is not found, and prometheus will not start.");
        }
        properties.put(PropertyDefine.PROMETHEUS_HOME_PROPERTY, PROMETHEUS_HOME);

        if (PROMETHEUS_HOST == null) {
            properties.put(PropertyDefine.PROMETHEUS_HOST_PROPERTY, "localhost");
        } else {
            properties.put(PropertyDefine.PROMETHEUS_HOST_PROPERTY, PROMETHEUS_HOST);
        }

        if (PROMETHEUS_PORT == null) {
            properties.put(PropertyDefine.PROMETHEUS_PORT_PROPERTY, 9090);
        } else {
            properties.put(PropertyDefine.PROMETHEUS_PORT_PROPERTY, PROMETHEUS_PORT);
        }

        if (DEPLOY_TYPE == null) {
            properties.put(PropertyDefine.DEPLOY_TYPE_PROPERTY, DeployType.studio.getName());
        } else {
            properties.put(PropertyDefine.DEPLOY_TYPE_PROPERTY, DEPLOY_TYPE);
        }

        if (WEB_ACCEPT_COUNT != null) {
            properties.put(PropertyDefine.SERVER_ACCEPT_COUNT_PROPERTY, WEB_ACCEPT_COUNT);
        }

        if (WEB_MAX_THREADS != null) {
            properties.put(PropertyDefine.SERVER_MAX_THREADS_PROPERTY, WEB_MAX_THREADS);
        }

        if (WEB_MIN_SPARE_THREADS != null) {
            properties.put(PropertyDefine.SERVER_MIN_SPARE_THREADS_PROPERTY, WEB_MIN_SPARE_THREADS);
        }

        if (WEB_MAX_CONNECTIONS != null) {
            properties.put(PropertyDefine.SERVER_MAX_CONNECTIONS_PROPERTY, WEB_MAX_CONNECTIONS);
        }

        return properties;
    }
}
