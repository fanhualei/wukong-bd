package com.wukong.mapreduce.score;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 统计学生的平均成绩
 * @author fan
 */
public class ScoreAverage {

    private static class MyMap extends Mapper<Object,Text,Text,Text>{

        @Override
        protected void map(Object inputkey,Text inputValue,Context outPutContext)
                throws IOException, InterruptedException {
            //将学生名字作为key 将学生成绩作为value
            String[] fields=inputValue.toString().split(",");
            outPutContext.write(new Text(fields[1]),
                    new Text(fields[2])
                    );
        }
    }


    private static class MyReduce extends Reducer<Text,Text,Text,Text>{

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {

            int count=0;
            int total=0;
            float avg;
            for(Text value:values){
                count++;
                total=total+Integer.parseInt(value.toString());
            }
            avg=total/count;

            context.write(key,
                    new Text("考试了"+ count+"门科目,平均成绩:"+avg)
                    );
        }
    }


    public static  void main(String[] args) throws Exception{
        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"统计学生的平均成绩");

        job.setJarByClass(ScoreAverage.class);
        job.setMapperClass(MyMap.class);
        job.setReducerClass(MyReduce.class);


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        System.exit(job.waitForCompletion(true)?0:1);
    }


}
