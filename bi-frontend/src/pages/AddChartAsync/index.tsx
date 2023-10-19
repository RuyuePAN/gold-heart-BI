import React, {useEffect, useState} from 'react';
import {UploadOutlined} from '@ant-design/icons';
import {
  Button, Card,
  Form, Input, message,
  Select,
  Space,
  Upload,
} from 'antd';
import TextArea from "antd/es/input/TextArea";
import {
  genChartByAiAsyncMQUsingPOST,
  genChartByAiAsyncUsingPOST,
  genChartByAiUsingPOST
} from '@/services/mybi/chartController';
import {useForm} from "antd/lib/form/Form";

/**
 * 添加图表页面
 * @constructor
 */
const AddChartAsync: React.FC = () => {
  // 创建一个响应式变量
  const [form] = useForm();
  // 默认为false
  const [submitting, setSubmitting] = useState<boolean>(false)
  /**
   * 提交
   * @param values
   */
  const onFinish = async (values: any) => {
    // 避免重复提交
    if (submitting) {
      return;     // 直接返回
    }
    setSubmitting(true);        // 开始时设置为 true
    // 对接后端，上传数据
    const params = {
      ...values,
      file: undefined
    };
    try {
      // const res = await genChartByAiAsyncUsingPOST(params, {}, values.file.file.originFileObj)
      const res = await genChartByAiAsyncMQUsingPOST(params, {}, values.file.file.originFileObj)
      console.log(res);
      if (!res.data) {
        message.error("分析失败")
      } else {
        message.success("分析任务提交成功，请在我的图表页面查看任务执行状况");
        form.resetFields();
      }
    } catch (e: any) {
      message.error("分析失败," + e)
    }
    setSubmitting(false);       // 结束后设置为 false
  };

  return (
    // 给页面指定类名
    <div className="addChart">

      <Card title="智能分析">
        <Form form={form}
          labelAlign="left"
          labelCol={{span: 4}}
          wrapperCol={{span: 16}}
          name="add_chart"
          onFinish={onFinish}
          initialValues={{}}
        >

          <Form.Item
            name="goal"
            label="分析目标"
            rules={[{required: true, message: '请输入分析目标！'}]}
          >
            <TextArea placeholder="请输入你的分析需求：比如，分析网站的用户数量趋势"/>
          </Form.Item>
          <Form.Item
            name="name"
            label="图表名称"
            rules={[{required: true, message: '请输入图表名称！'}]}
          >
            <Input placeholder="请输入图表名称"/>
          </Form.Item>

          <Form.Item
            name="chartType"
            label="图表类型"
            rules={[{required: true, message: '图表类型必须选择！'}]}
          >
            <Select
              options={[
                {
                  value: '折线图',
                  label: '折线图',
                },
                {
                  value: '柱状图',
                  label: '柱状图',
                },
                {
                  value: '堆叠图',
                  label: '堆叠图',
                },
                {
                  value: '雷达图',
                  label: '雷达图',
                },
                {
                  value: '饼图',
                  label: '饼图',
                }
              ]}
            />
          </Form.Item>
          <Form.Item
            name="file"
            label="原始数据"
            //extra="请上传excel文件(*^▽^*)"
            rules={[{required: true, message: '请上传待分析的数据！'}]}
          >
            <Upload name="file" maxCount={1}>
              <Button icon={<UploadOutlined/>}>上传 CSV 文件</Button>
            </Upload>
          </Form.Item>

          <Form.Item wrapperCol={{span: 16, offset: 4}}>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting} disabled={submitting}>
                提交
              </Button>
              <Button htmlType="reset">重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>

  );
};

export default AddChartAsync;
