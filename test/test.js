// parseTime.test.js
// import { parseTime } from "./parseTime";
const { parseTime } = require("./parseTime");

/**
 * parseTime 函数测试套件
 * 测试各种时间格式的解析和格式化
 */
describe("parseTime 时间格式化函数测试", () => {
  // 测试用例使用的参考时间（本地时间）
  const referenceTimestamp = 1706279400000; // 2024-01-26 15:30:00 (正确的时间戳)
  const referenceDate = new Date(referenceTimestamp);

  // 1. 测试空值和边界情况
  describe("边界情况和空值处理", () => {
    test("没有参数时返回 null", () => {
      expect(parseTime()).toBe(null);
    });

    test("传入 null 返回 null", () => {
      expect(parseTime(null)).toBe(null);
    });

    test("传入 undefined 返回 null", () => {
      expect(parseTime(undefined)).toBe(null);
    });

    test("传入空字符串返回 null", () => {
      expect(parseTime("")).toBe(null);
    });

    test("传入 0 应该正常处理", () => {
      expect(parseTime(0)).toBe("1970-01-01 01:00:00"); // CET时区 (+1小时)
    });
  });

  // 2. 测试 Date 对象输入
  describe("Date 对象输入", () => {
    test("传入 Date 对象", () => {
      const result = parseTime(referenceDate);
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("Date 对象使用自定义格式", () => {
      const result = parseTime(referenceDate, "{y}年{m}月{d}日");
      expect(result).toBe("2024年01月26日");
    });
  });

  // 3. 测试时间戳输入
  describe("时间戳输入", () => {
    test("13位时间戳（毫秒）", () => {
      const result = parseTime(1706279400000);
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("10位时间戳（秒）- 自动转换为毫秒", () => {
      const result = parseTime(1706279400); // 10位
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("10位时间戳字符串 - 自动转换", () => {
      const result = parseTime("1706279400");
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("13位时间戳字符串", () => {
      const result = parseTime("1706279400000");
      expect(result).toBe("2024-01-26 15:30:00");
    });
  });

  // 4. 测试日期字符串输入
  describe("日期字符串输入", () => {
    test("ISO 格式字符串", () => {
      const result1 = parseTime("2025-09-27T21:09:39.747314");
      expect(result1).toMatch(/2025-09-27 \d{2}:09:39/);

      const result = parseTime("2024-01-26T15:30:00.123Z");
      // ISO格式带Z表示UTC时间，会转换为本地时区，预期可能不同
      expect(result).toMatch(/2024-01-26 \d{2}:30:00/);
    });

    test("标准日期字符串", () => {
      const result = parseTime("2024-01-26 15:30:00");
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("带毫秒的日期字符串", () => {
      const result = parseTime("2024-01-26 15:30:00.456");
      expect(result).toBe("2024-01-26 15:30:00");
    });

    test("斜杠分隔的日期字符串", () => {
      const result = parseTime("2024/01/26 15:30:00");
      expect(result).toBe("2024-01-26 15:30:00");
    });
  });

  // 5. 测试自定义格式
  describe("自定义格式输出", () => {
    const testTime = referenceTimestamp;

    test("只显示日期", () => {
      const result = parseTime(testTime, "{y}-{m}-{d}");
      expect(result).toBe("2024-01-26");
    });

    test("只显示时间", () => {
      const result = parseTime(testTime, "{h}:{i}:{s}");
      expect(result).toBe("15:30:00");
    });

    test("中文日期格式", () => {
      const result = parseTime(testTime, "{y}年{m}月{d}日 {h}时{i}分{s}秒");
      expect(result).toBe("2024年01月26日 15时30分00秒");
    });

    test("星期显示", () => {
      // 2024-01-26 是星期五
      const result = parseTime(testTime, "星期{a}");
      expect(result).toBe("星期五");
    });

    test("完整中文格式", () => {
      const result = parseTime(testTime, "{y}年{m}月{d}日 {h}:{i}:{s} 星期{a}");
      expect(result).toBe("2024年01月26日 15:30:00 星期五");
    });

    test("美国日期格式", () => {
      const result = parseTime(testTime, "{m}/{d}/{y} {h}:{i}:{s}");
      expect(result).toBe("01/26/2024 15:30:00");
    });
  });

  // 6. 测试特殊日期
  describe("特殊日期测试", () => {
    test("闰年日期", () => {
      const leapYear = new Date(2024, 1, 29); // 2024-02-29
      const result = parseTime(leapYear);
      expect(result).toBe("2024-02-29 00:00:00");
    });

    test("月末日期", () => {
      const endOfMonth = new Date(2024, 0, 31); // 2024-01-31
      const result = parseTime(endOfMonth);
      expect(result).toBe("2024-01-31 00:00:00");
    });

    test("时间边界 - 00:00:00", () => {
      const midnight = new Date(2024, 0, 26, 0, 0, 0);
      const result = parseTime(midnight);
      expect(result).toBe("2024-01-26 00:00:00");
    });

    test("时间边界 - 23:59:59", () => {
      const endOfDay = new Date(2024, 0, 26, 23, 59, 59);
      const result = parseTime(endOfDay);
      expect(result).toBe("2024-01-26 23:59:59");
    });
  });

  // 7. 测试错误输入处理
  describe("错误输入处理", () => {
    test("无效日期字符串", () => {
      const result = parseTime("invalid-date-string");
      // 应该返回一个日期字符串，可能是 "Invalid Date" 或具体的错误格式
      expect(typeof result).toBe("string");
    });

    test("非常大的数字", () => {
      const result = parseTime(9999999999999);
      expect(result).toMatch(/\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/);
    });

    test("负时间戳", () => {
      const result = parseTime(-1000000);
      expect(result).toMatch(/\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/);
    });
  });
});
