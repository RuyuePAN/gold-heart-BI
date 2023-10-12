package com.pan.springbootinit.api;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OpenAIApi
 * @Description TODO
 * @Author Pan
 * @DATE 2023/10/10 17:50
 */
public class OpenAIApi {
    public static void main(String[] args) {
        String url = "";
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", "用户的消息，请帮我分析");
        String json = JSONUtil.toJsonStr(hashMap);
        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer 替换为你自己的key")
                .body(json)
                .execute().body();
    }
}
