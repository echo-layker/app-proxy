package com.layke.appproxy.controller;

import com.layke.appproxy.http.HttpManager;
import kotlin.Pair;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author zhangdingshui on 2019/11/12.
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/")
public class QiyueController {


    @Autowired
    private HttpManager httpManager;

    @RequestMapping(value = "*")
    public void api(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        Response response = httpManager.cloneRequest(request);
        Iterator<Pair<String, String>> iterator = response.headers().iterator();
        while (iterator.hasNext()) {
            Pair<String, String> next = iterator.next();
            httpServletResponse.addHeader(next.getFirst(), next.getSecond());
        }
        httpServletResponse.getOutputStream().write(response.body().bytes());
    }

    @GetMapping(value = "/mg")
    public Object set(@RequestParam String host) {
        HttpUrl httpUrl = HttpUrl.parse(host);
        HttpManager.setHOST(httpUrl.host());
        HttpManager.setSCHEMA(httpUrl.scheme());
        return httpUrl.scheme() + "://" + httpUrl.host();
    }
}
