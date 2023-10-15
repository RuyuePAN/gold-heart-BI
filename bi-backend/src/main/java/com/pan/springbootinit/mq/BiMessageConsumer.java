package com.pan.springbootinit.mq;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pan.springbootinit.common.ErrorCode;
import com.pan.springbootinit.exception.BusinessException;
import com.pan.springbootinit.manager.AiManager;
import com.pan.springbootinit.model.dto.chart.BiResponse;
import com.pan.springbootinit.model.entity.Chart;
import com.pan.springbootinit.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName MessageConsumer
 * @Description 指定程序监听的消息队列和确认机制
 * @Author Pan
 * @DATE 2023/10/15 21:27
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;

    @SneakyThrows       // 帮助消除异常
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果为空，则拒绝消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);

        // 先修改图表任务状态为执行中，减少重复执行的风险，同时让用户知道执行的状态
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
        }
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        // 如果图表状态更改失败
        if(!b) {
            handleChartUpdateError(chart.getId(), "更新图表状态执行中失败");
        }

        String chatMessage = aiManager.buildChatMessage(chart.getGoal(), chart.getChartData(), chart.getChartType());

        String res = aiManager.doChat(chatMessage);
        updateChart.setId(chart.getId());
        updateChart.setStatus("succeed");
        System.out.println("返回的结果是：");
        System.out.println(res);
        BiResponse biResponse = null;
        try {
            biResponse = aiManager.getChatResult(res);
            updateChart.setGenChart(biResponse.getGenChart());
            updateChart.setGenResult(biResponse.getGenResult());
        } catch (Exception e) {
            handleChartUpdateError(chart.getId(), "AI响应错误");
        }
        boolean updateResult = chartService.updateById(updateChart);
        if (!updateResult) {
            handleChartUpdateError(chart.getId(), "更新图表状态失败");
        }
        // 消息确认
        channel.basicAck(deliveryTag, false);
    }


    /**
     * 异常回调
     * TODO:重复方法，两个重复方法应该统一放进Service
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
        updateChartResult.setStatus("failed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + ", " + execMessage);
        }
    }
}
