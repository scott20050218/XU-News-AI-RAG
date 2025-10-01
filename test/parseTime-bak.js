function parseTime(time, pattern) {
  if (
    arguments.length === 0 ||
    time === null ||
    time === undefined ||
    time === ""
  ) {
    return null;
  }
  const format = pattern || "{y}-{m}-{d} {h}:{i}:{s}";
  let date;
  if (typeof time === "object") {
    date = time;
  } else {
    if (typeof time === "string" && /^[0-9]+$/.test(time)) {
      time = parseInt(time);
    } else if (typeof time === "string") {
      // 如果是ISO 8601格式的字符串，直接使用，避免时区问题
      if (time.includes("T")) {
        // 对于包含T的ISO格式，保持原样或者去掉微秒部分
        time = time.replace(/\.\d+$/, ""); // 去掉微秒部分
      } else {
        // 对于其他格式，进行原有的转换
        time = time
          .replace(new RegExp(/-/gm), "/")
          .replace("T", " ")
          .replace(new RegExp(/\.[\d]{3}/gm), "");
      }
    }
    if (typeof time === "number" && time.toString().length === 10) {
      time = time * 1000;
    }
    date = new Date(time);
  }

  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  };
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key];
    // Note: getDay() returns 0 on Sunday
    if (key === "a") {
      return ["日", "一", "二", "三", "四", "五", "六"][value];
    }
    if (result.length > 0 && value < 10) {
      value = "0" + value;
    }
    return value || 0;
  });

  return time_str;
}

module.exports = { parseTime };
