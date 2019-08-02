package com.wukong.mapreduce.score;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 统计科目中分数相同的人有多少,每个人的人名
 *
 * @author fan
 */
public class CourseScoreGrouping {

    private static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {

        /**
         * @param key     读取文件的偏移量
         * @param value   读取一行数据. 例如 computer,huangxiaoming,85
         * @param context 要输出的内容
         * @throws IOException          io异常
         * @throws InterruptedException 其他异常
         */
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            //将科目与分数作为key , 将姓名作为value . 例如:计算机,huangjiaju,88
            String[] fieldsValues= value.toString().split(",");
            context.write(new Text(fieldsValues[0]+"\t"+fieldsValues[2])
                    ,new Text(fieldsValues[1]));


        }

    }

    private static class MyReduce extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            int count=0;
            StringBuffer studentNames=new StringBuffer();
            for(Text value:values){
                studentNames.append(value).append(" ");
                count++;
            }
            if(count>1){
                context.write(key,
                        new Text(" "+ count +" "+ studentNames.toString())
                        );
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "统计分数重复的学生名单");

        job.setJarByClass(CourseScoreGrouping.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }


}
