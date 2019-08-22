package wukong.spark.nginx

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

/**
 * 日志文件对象
 */
case class AccessLog(
                      ip: String, //设备用户的真实ip地址
                      clientIdent: String, //用户标识
                      userId: String, //用户
                      timestamp: String, //访问日期时间
                      request: String, //请求信息，get/post，mac值等
                      responseCode: Int, //请求状态 200成功，304静态加载
                      contentSize: Long, //返回文件的大小
                      referrer: String, //跳转来源
                      ua: String //UA信息
                      //  forward:String //跳转页面

                    ) extends Serializable {

  /**
   * 得到具体的访问页面
   *
   * @return string
   */
  def getPage(): String = {
    val tempPage = request.split(" ")(1)
    val index = tempPage.indexOf("?")
    if (index > 0) {
      tempPage.substring(0, index)
    } else {
      tempPage
    }
  }

  def getNewTime: String ={
    val timeStr= timestamp.substring(1,timestamp.length-1)
    val sdf: SimpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z",Locale.US)
    val date=sdf.parse(timeStr)
    val newSdf: SimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    newSdf.format(date)
  }

  override def toString: String = ip + "," + clientIdent + "," + userId + "," + timestamp + "," + request + "," + responseCode + "," + contentSize + "," + referrer + "," + ua
}

