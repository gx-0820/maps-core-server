package com.example.coreserver.utils;

import com.alibaba.fastjson2.JSONObject;
import io.github.admin4j.http.HttpRequest;
import io.github.admin4j.http.core.Pair;
import io.github.admin4j.http.util.HttpUtil;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OkHttpUtil {

    public static JSONObject get(String url, Map<String, Object> queryParams) throws IOException {
        Response response = HttpUtil.get(url, queryParams);
        return JSONObject.parseObject(response.body().string());
    }
    
    public static JSONObject get(String url, Map<String, Object> queryParams, Map<String, Object> headers) throws IOException {
        HttpRequest httpRequest = HttpRequest.get(url);
        setParams(queryParams, httpRequest);
        Response response = httpRequest.queryParams().headers(headers).execute();
        return JSONObject.parseObject(response.body().string());
    }

    public static JSONObject post(String url, String json) throws IOException {
        Response response = HttpUtil.post(url, json);
        assert response.body() != null;
        return JSONObject.parseObject(response.body().string());
    }

    public static JSONObject postForm(String url, Map<String, Object> formParams) throws IOException {
        Response response = HttpUtil.postForm(url, formParams);
        assert response.body() != null;
        return JSONObject.parseObject(response.body().string());
    }

    public static JSONObject post(String url, String json, Map<String, Object> headers) throws IOException {
        HttpRequest httpRequest = HttpRequest.post(url);
        httpRequest.setBody(json);
        Response response = httpRequest.headers(headers).execute();
        return JSONObject.parseObject(response.body().string());
    }

    private static void setParams(Map<String, Object> queryParams, HttpRequest httpRequest) {
        List<Pair> pairs = new ArrayList<>(queryParams.size());
        queryParams.forEach((x, y) -> pairs.add(Pair.of(x, y)));
        if (pairs.size() > 0) {
            pairs.forEach(httpRequest::queryParams);
        }
    }


}
