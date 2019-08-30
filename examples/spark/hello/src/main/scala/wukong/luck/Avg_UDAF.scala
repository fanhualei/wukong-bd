package wukong.luck

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, IntegerType, StructField, StructType}

object Avg_UDAF extends UserDefinedAggregateFunction{
  //用户输入参数
  override def inputSchema: StructType = {
    StructType(Array(
      StructField("inputvalue",DoubleType)
    ))
  }

  //缓冲区，存数上一个相加计算的数值
  override def bufferSchema: StructType = {
    StructType(Array(
      StructField("totalValue",DoubleType),
      StructField("totalInt",IntegerType)
    ))
  }

  //返回的类型
  override def dataType: DataType = {
    DoubleType
  }

  //给定多次运行该方法，传入相同的数值，是否返回相同的数值
  override def deterministic: Boolean = {
    //默认返回true
    true
  }

  // 给定缓冲区的初始值
  override def initialize(buffer: MutableAggregationBuffer): Unit = {

    buffer.update(0,0.0)
    buffer.update(1,0)
  }


  //拿出缓存的数值，拿出新的一条记录，两个值进行计算，然后更新缓存
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    val iv=input.getDouble(0)
    val bv=buffer.getDouble(0)
    val bc=buffer.getInt(1)

    buffer.update(0,iv+bv)
    buffer.update(1,bc+1)

  }

  //两个缓存中的数值如何相加
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    val bv1=buffer1.getDouble(0)
    val bv2=buffer2.getDouble(0)

    val bc1=buffer1.getInt(1)
    val bc2=buffer2.getInt(1)

    buffer1.update(0,bv1+bv2)
    buffer1.update(1,bc1+bc2)


  }

  //如何计算
  override def evaluate(buffer: Row): Any = {
    val bv=buffer.getDouble(0)
    val bc=buffer.getInt(1)
    //获得平均值
    bv/bc
  }
}
