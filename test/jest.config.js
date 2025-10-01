module.exports = {
  testEnvironment: "node",
  collectCoverageFrom: [
    "**/parseTime.js",
    "!**/node_modules/**",
    "!**/vendor/**",
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 90,
      lines: 90,
      statements: 90,
    },
  },
};
