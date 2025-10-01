import React from "react";
import ReactECharts from "echarts-for-react";
import "../App.css"; // Assuming App.css will contain general layout styles

const Home: React.FC = () => {
  const getChartOption = () => {
    return {
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "shadow",
        },
      },
      legend: {
        data: ["RSS数量", "平均RSS数"],
      },
      xAxis: {
        type: "category",
        data: [
          "1月3",
          "1月6",
          "1月9",
          "1月12",
          "1月15",
          "1月18",
          "1月21",
          "1月24",
          "1月27",
          "1月30",
        ],
      },
      yAxis: {
        type: "value",
      },
      series: [
        {
          name: "RSS数量",
          type: "bar",
          data: [800, 700, 900, 750, 850, 600, 950, 800, 700, 1000],
        },
        {
          name: "平均RSS数",
          type: "line",
          data: [600, 550, 700, 650, 720, 500, 780, 680, 580, 800],
        },
      ],
    };
  };

  return (
    <div className="home-container">
      <div className="metric-cards-container">
        {/* Metric Cards will go here */}
        <div className="metric-card">
          <h3>导入数据量</h3>
          <p>40,886,200</p>
          <span>50% ↑</span>
        </div>
        <div className="metric-card">
          <h3>RSS数据</h3>
          <p>275,800</p>
          <span>20% ↑</span>
        </div>
        <div className="metric-card">
          <h3>网页导入</h3>
          <p>106,120</p>
          <span>41% ↑</span>
        </div>
        <div className="metric-card">
          <h3>知识库数据量</h3>
          <p>80,600</p>
          <span>30% ↑</span>
        </div>
      </div>

      <div className="chart-and-details-container">
        <div className="order-chart-container">
          <h2>RSS数据</h2>
          <ReactECharts option={getChartOption()} style={{ height: "350px" }} />
        </div>
        <div className="side-details-container">
          <div className="side-detail-card">
            <p>2,346</p>
            <span>RSS数据总数</span>
            <span>40% ↑</span>
          </div>
          <div className="side-detail-card">
            <p>4,422</p>
            <span>最近一个月RSS数据</span>
            <span>60% ↑</span>
          </div>
          <div className="side-detail-card">
            <p>9,180</p>
            <span>最近一个月导入数据量</span>
            <span>22% ↑</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
