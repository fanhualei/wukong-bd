package com.wukong.mapreduce.scorePro;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 求每个课程的参考人数与平均成绩
 * @author fan
 */
public class CourseScoreAveragePro {

    private static class MyMap extends Mapper<Object,Text,Text,FloatWritable>{

        @Override
        public void map(Object inKey,Text inValue,Context context) throws IOException,InterruptedException{
            // 输入数据格式:计算机,黄晓明,85,86,41,75,93,42,85
            String[] fields=inValue.toString().split(",");
            int count=0,sum=0;
            float avg;
            for(int i=2;i<fields.length;i++){
                count++;
                sum=sum+Integer.parseInt(fields[i]);
            }
            avg=sum/count;
            context.write(new Text(fields[0]),
                    new FloatWritable(avg)
                    );
        }
    }


    private  static  class  MyReduce extends Reducer<Text,FloatWritable,Text,Text>{

        @Override
        public void reduce(Text inKey,Iterable<FloatWritable> inValues,Context context)
            throws IOException,InterruptedException{

            int count=0;
            float avg=0f;
            float sum=0f;

            for(FloatWritable value:inValues){
                sum=sum+value.get();
                count++;
            }
            avg=sum/count;
            context.write(inKey,
                    new Text(" "+count+" "+ avg)
                    );
        }
    }

    public static void main(String[] args)throws Exception{
        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"求每课的平均成绩");

        job.setJarByClass(CourseScoreAveragePro.class);
        job.setMapperClass(MyMap.class);
        job.setReducerClass(MyReduce.class);


        //如果map使用了非Text类型,那么就要设置output参数
        job.setMapOutputValueClass(FloatWritable.class);

        //这个必须要设置
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);


        FileInputFormat.setInputPaths(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));

        System.exit(job.waitForCompletion(true)?0:1);


    }


}
