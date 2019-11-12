package com.layke.appproxy.http;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangdingshui on 2019/11/12.
 * @version 1.0
 */
@Service
public class HttpManager {
    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .build();


    private static String SCHEMA = "https";


    public static void setSCHEMA(String SCHEMA) {
        HttpManager.SCHEMA = SCHEMA;
    }

    public static void setHOST(String HOST) {
        HttpManager.HOST = HOST;
    }

    private static String HOST = "www.baidu.com";


    private Map<String, Object> cache = new HashMap<>();

    public Object post(String url, byte[] bytes) {
        Request request = new Request.Builder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .post(RequestBody.create(bytes, okhttp3.MediaType.get(MediaType.APPLICATION_JSON_VALUE)))
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String responseString = response.body().string();
            return responseString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response cloneRequest(HttpServletRequest httpServletRequest) throws IOException {
        String method = httpServletRequest.getMethod();
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        String path = httpServletRequest.getServletPath();
        StringBuilder stringBuilder = new StringBuilder();
        parameterMap.entrySet().forEach(e -> {
            String key = e.getKey();
            String[] values = e.getValue();
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(values.length > 0 ? values[0] : "");
            stringBuilder.append("&");
        });
        String url = SCHEMA + "://" + HOST + path + "?" + stringBuilder.toString();
        InputStream inputStream = httpServletRequest.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        Map<String, String> headerMap = getHeaderMap(httpServletRequest);
        headerMap.put("host", HOST);
        String contentType = httpServletRequest.getContentType();
        Cookie[] cookies = httpServletRequest.getCookies();
        String requestURI = httpServletRequest.getRequestURI();
        Builder builder = new Builder()
                .url(HttpUrl.parse(url))
                .headers(Headers.of(headerMap));
        if (HttpMethod.GET.matches(method)) {
            builder.get();
        } else if (HttpMethod.POST.matches(method)) {
            builder.post(RequestBody.create(bytes));
        } else {
            System.err.println("暂不支持此请求方式：" + method);
        }
        Response response = okHttpClient.newCall(builder.build()).execute();
        return response;
    }

    private Map<String, String> getHeaderMap(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headerMap.put(name, request.getHeader(name));
        }
        return headerMap;
    }
}
