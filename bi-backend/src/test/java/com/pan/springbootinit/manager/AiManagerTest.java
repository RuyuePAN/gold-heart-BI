package com.pan.springbootinit.manager;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
// 创建一个JSONObject对象
        JSONObject json = new JSONObject();

// 添加"messages"字段，并设置其值为一个JSON数组
        JSONArray messages = new JSONArray();
        json.put("messages", messages);

// 创建一个JSONObject，表示第一条消息
        JSONObject message1 = new JSONObject();

// 设置"role"字段为"user"
        message1.put("role", "user");

// 设置"content"字段为所需的内容字符串
        String content = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n分析需求:\n{用户数量趋势}\n原始数据:\n{日期,用户数量\n2023.10.10,10\n2023.10.11,10\n2023.10.12,10\n2023.10.13,50\n2023.10.14,60\n2023.10.15,10\n2023.10.16,5\n2023.10.17,1010\n2023.10.18,955\n2023.10.19,536\n}\n请根据这两部分内容，帮我按照以下格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n：【【【\n{将数据绘制成散点图的前端 Echarts V5 的option 配置对象js代码}【【【\n{看图说话及明确的数据分析结论，不少于500字}";
        message1.put("content", content);

// 将message对象添加到messages数组中
        messages.add(message1);

// 输出JSON字符串
        String jsonString = json.toStringPretty();
        System.out.println(jsonString);
        String res = aiManager.doChat(jsonString);
        System.out.println(res);
    }
}