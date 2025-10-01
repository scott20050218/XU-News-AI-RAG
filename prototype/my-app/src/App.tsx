import React from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Home from "./pages/Home";
import KnowledgeBase from "./pages/KnowledgeBase";
import SemanticQuery from "./pages/SemanticQuery";
import ReportGeneration from "./pages/ReportGeneration";
import "./App.css";

function App() {
  return (
    <Router>
      <div className="App">
        <nav>
          <ul>
            <li>
              <Link to="/">首页</Link>
            </li>
            <li>
              <Link to="/knowledge-base">知识库内容管理</Link>
            </li>
            <li>
              <Link to="/semantic-query">语义查询</Link>
            </li>
            <li>
              <Link to="/report-generation">报告生成</Link>
            </li>
          </ul>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/knowledge-base" element={<KnowledgeBase />} />
          <Route path="/semantic-query" element={<SemanticQuery />} />
          <Route path="/report-generation" element={<ReportGeneration />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
