import React, {useEffect, useState} from 'react';
import {DeleteOutlined, SearchOutlined, UploadOutlined} from '@ant-design/icons';
import {
  Button, Card, Col, Divider,
  Form, Input, message,
  Select,
  Space,
  Upload,
  List,
  Avatar, Result, Modal
} from 'antd';
import {deleteChartUsingPOST, genChartByAiUsingPOST, listMyChartByPageUsingPOST} from '@/services/mybi/chartController';
import ReactECharts from 'echarts-for-react';
import Row from 'antd/lib/row';
import Spin from 'antd/lib/spin';
import {getInitialState} from "@/app";
import {useModel} from "@umijs/max";
import Search from "antd/es/input/Search";
import {values} from "lodash";
import Tooltip from 'antd/lib/tooltip';

/**
 * 我的图表页面
 * @constructor
 */
const MyChartPage: React.FC = () => {
  const initSearchParams = {
    current: 1,
    pageSize: 10,
    sortField: 'createTime',
    sortOrder: 'desc'
  }
  const {initialState} = useModel('@@initialState');
  // 是否显示对话框
  const [isModalOpen, setIsModalOpen] = useState(false);
  // 从initialState中取currentUser
  const {currentUser} = initialState ?? {};
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({...initSearchParams})
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [loading, setLoading] = useState<boolean>(false)
  // 默认情况下为 0
  const [total, setTotal] = useState<number>(0)

  // 对话框函数——显示对话框
  const showModal = () => {
    setIsModalOpen(true);
  };
  // 对话框函数——处理确认事件
  const handleOk = () => {
    setIsModalOpen(false);
  };
  // 对话框函数——处理取消事件
  const handleCancel = () => {
    setIsModalOpen(false);
  };
  // 定义一个获取数据的函数
  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listMyChartByPageUsingPOST(searchParams);
      if (res.data) {
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0);
        // 把 title 抹掉
        if (res.data.records) {
          res.data.records.forEach(data => {
            if (data.status === 'succeed') {
              const chartOption = JSON.parse(data.genChart ?? "{}");
              chartOption.title = undefined;
              data.genChart = JSON.stringify(chartOption);
            }
          })
        }
      } else {
        message.error("获取图表失败");
      }
    } catch (e: any) {
      message.error("获取我的图表失败，" + e.message)
    }
    setLoading(false)
  }
  // 这个钩子函数用于触发加载数据
  // 在react页面首次渲染时，数组中的一些变量发生变化时触发
  // 这里就是搜索条件发生改变就会自动触发搜索
  useEffect(() => {
    loadData();
  }, [searchParams]);

  return (
    // 给页面指定类名
    <div className="my-chart-page">
      <div>
        <Search placeholder="请输入图表名称" loading={loading} enterButton onSearch={(value) => {
          setSearchParams({
            // 设置搜索条件
            ...initSearchParams,    // 当用户点击搜索按钮时，将新设置的条件变回初始化，重新展示第一页
            name: value
          })
        }}></Search>
      </div>
      <div className="margin-16"/>
      <List
        grid={{gutter: 16, xs: 1, sm: 1, md: 1, lg: 2, xl: 2, xxl: 2}}
        pagination={{
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize: pageSize
            })
          },
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total
        }}
        loading={loading}
        dataSource={chartList}
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card style={{ width: '100%' }}>
              <List.Item.Meta
                avatar={<Avatar src={currentUser && currentUser.userAvatar} />}
                title={item.name}
                description={item.chartType ? '图表类型：' + item.chartType : undefined}
              />
              <>
                {
                  item.status === 'wait' && <>
                    <Result
                      status="warning"
                      title="待生成"
                      subTitle={item.execMessage ?? '当前图表生成队列繁忙，请耐心等候'}
                    />
                  </>
                }
                {
                  item.status === 'running' && <>
                    <Result
                      status="info"
                      title="图表生成中"
                      subTitle={item.execMessage}
                    />
                  </>
                }
                {
                  item.status === 'succeed' && <>
                    <div style={{ marginBottom: 16 }} />
                    <p>{'分析目标：' + item.goal}</p>
                    <p>{'分析结论：' + item.genResult}</p>
                    <div style={{ marginBottom: 16 }} />
                    <ReactECharts option={item.genChart && JSON.parse(item.genChart)} />
                  </>
                }
                {
                  item.status === 'failed' && <>
                    <Result
                      status="error"
                      title="图表生成失败"
                      subTitle={item.execMessage}
                    />
                  </>
                }

                <Modal title="提示" open={isModalOpen} onOk={handleOk} onCancel={handleCancel}>
                  <p>是否删除这张图表？</p>
                </Modal>

                <Row justify="end">
                  <Col span={4}></Col>
                  <Col span={4}></Col>
                  <Col span={4}></Col>
                  <Col span={4}>
                    <Tooltip title="删除该图表">
                      <Button
                        shape="circle"
                        danger icon={<DeleteOutlined />}
                        onClick={() => {
                            // 弹出确认对话框
                            showModal();
                            // 删除图片
                            deleteChartUsingPOST({id: item.id})
                            // 刷新页面
                            loadData();
                          }
                        }
                      />
                    </Tooltip>
                  </Col>
                </Row>
              </>

            </Card>
          </List.Item>

        )}
      />
    </div>
  );
};

export default MyChartPage;
