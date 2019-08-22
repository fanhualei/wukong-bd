package wukong.spark.nginx

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

/**
 * Nginx日志数据转换类
 */
object NginxLogParser{
  /**
   * 解析正则表达式
   * .r用于指明PARTTERN是一个正则表达式对象
   * 9个值：
   *  客户端访问IP
   *  用户标识clientIdentd
   *  用户userId
   *  访问时间dateTime
   *  请求方式mode
   *  请求状态responseCode
   *  返回文件的大小contentSize
   *  跳转来源referrer
   *  UA信息
   */
  val PATTERN =
    "(\\S+) (\\S+) (\\S+) (\\[.*\\]) (\\\".*\\\") (\\d{3}) (\\d+) (\\\".*?\\\") (\\\".*?\\\")".r

  def parseLog2Line(log: String): AccessLog = {
    def makeWifiLogs(): AccessLog = {
      new AccessLog("", "", "", "", "", 0, 0, "", "")
    }

    if (log.isEmpty) {
      makeWifiLogs()
    }else{
      val logs = PATTERN.findFirstMatchIn(log)
      if (logs.isEmpty) {
        throw new RuntimeException("Cannot parse log line: " + log)
      }

      // 一共有9个匹配
      val m = logs.get
      new AccessLog(m.group(1), m.group(2), m.group(3), m.group(4),m.group(5), m.group(6).toInt,
        m.group(7).toLong, m.group(8), m.group(9))
    }
  }

  def main(args: Array[String]) {
    val line = "183.136.190.62 - - [22/Aug/2019:03:57:14 +0800] \"GET / HTTP/1.1\" 200 208 \"-\" \"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36\""

    val log = NginxLogParser.parseLog2Line(line);
    println(log.ip)
    println(log.clientIdent)
    println(log.userId)
    println(log.timestamp)
    println(log.request)
    println(log.responseCode)
    println(log.contentSize)
    println(log.referrer)
    println(log.ua)
    println(log.getPage)

    println(log.getNewTime)
  }
}
