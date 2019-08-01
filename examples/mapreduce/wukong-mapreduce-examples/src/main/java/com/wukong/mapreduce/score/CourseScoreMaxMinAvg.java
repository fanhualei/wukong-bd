package com.wukong.mapreduce.score;


import java.io.IOException;

import com.wukong.mapreduce.WordCount;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * 求一个科目成绩的 最大值 最小值 平均值
 * 数据文件在:input/course-score-max-min-avg
 * @author fan
 */
public class CourseScoreMaxMinAvg {

    public static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {



        /**
         *
         * @param key  读取文件的偏移量
         * @param value 读取一行数据. 例如 computer,huangxiaoming,85
         * @param context 要输出的内容
         * @throws IOException  io异常
         * @throws InterruptedException  其他异常
         */
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String[] reads = value.toString().trim().split(",");
            //科目名称
            String course = reads[0];
            //成绩
            String score = reads[2];

            Text outputKey = new Text();
            Text outputValue = new Text();

            outputKey.set(course);
            outputValue.set(score);

            context.write(outputKey, outputValue);
        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text, Text> {


        /**
         *
         * @param key 从map输入的key,实际上是科目的名字
         * @param values 这个科目中所有的成绩
         * @param context  要输出的内容
         * @throws IOException io异常
         * @throws InterruptedException  其他异常
         */
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            int maxScore = 0;
            int minScore = 1000;
            float avgScore;
            int count = 0;
            int sum = 0;
            for (Text text : values) {
                int score = Integer.parseInt(text.toString());
                if (maxScore < score) {
                    maxScore = score;
                }
                if (minScore > score) {
                    minScore = score;
                }
                sum += score;
                count++;
            }
            avgScore = sum / count;
            String renStr = "max=" + maxScore + "\t" + "min=" + minScore + "\t" + "avg=" + avgScore;
            Text outputValue = new Text();
            outputValue.set(renStr);
            context.write(key, outputValue);
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf,"get course score max min avg.");

        job.setJarByClass(CourseScoreMaxMinAvg.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


}
