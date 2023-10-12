package com.pan.springbootinit.api;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.pan.springbootinit.common.ErrorCode;
import com.pan.springbootinit.exception.BusinessException;
import com.pan.springbootinit.model.dto.chart.BceAccessTokenResponse;
import com.pan.springbootinit.model.dto.chart.BceResponse;
import com.pan.springbootinit.utils.AppProperties;
import com.squareup.okhttp.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.cert.CertPathBuilderException;


/**
 * @ClassName BceClient
 * @Description 获取AccessToken（有效期为30天，生产环境注意刷新）
 * @Author Pan
 * @DATE 2023/10/11 11:54
 */
@Service
public class BceClient {
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    @Resource
    private AppProperties appProperties;

    private String accessToken = getAccessToken();

    private final String api_key = "";
    private final String secret_key = "";

    public String getAccessToken() {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        StringBuilder url = new StringBuilder();
        url.append("https://aip.baidubce.com/oauth/2.0/token?client_id=");
        //url.append(appProperties.getBceProvider().getApiKey());
        url.append(api_key);
        url.append("&client_secret=");
        //url.append(appProperties.getBceProvider().getSecretKey());
        url.append(secret_key);
        url.append("&grant_type=client_credentials");
        Request request = new Request.Builder()
                .url(url.toString())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = null;
        try {
            response = HTTP_CLIENT.newCall(request).execute();

            BceAccessTokenResponse bceAccessTokenResponse = JSONUtil.toBean(response.body().string(), BceAccessTokenResponse.class);
            if (bceAccessTokenResponse == null || StringUtils.isBlank(bceAccessTokenResponse.getAccess_token())) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取AI服务失败");
            }
            return bceAccessTokenResponse.getAccess_token();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AI 对话
     * @param message
     * @throws IOException
     */
    public String onChat(String message) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, message);
        StringBuilder url = new StringBuilder();
        url.append("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=");
        url.append(accessToken);
        Request request = new Request.Builder()
                .url(url.toString())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        System.setProperty("sun.net.client.defaultConnectTimeout", String
                .valueOf(100000));// （单位：毫秒）
        try {
            response = HTTP_CLIENT.newCall(request).execute();
            BceResponse bceResponse = JSONUtil.toBean(response.body().string(), BceResponse.class);
            // 如果过期就重新刷accessToken
            if (bceResponse.getError_code() != null && bceResponse.getError_code().equals("110") || bceResponse.getError_code().equals("119")) {
                accessToken = getAccessToken();
            } else if (bceResponse.getError_code() != null && bceResponse.getError_code().equals("336002") || bceResponse.getError_code().equals("336003")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入的JSON有误");
            }

            return bceResponse.getResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
