package com.wukong.mapreduce.friends;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

/**
 * 求相互粉丝的组合
 * @author fan
 */
public class EachFansMr {

    private static class MyMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        @Override
        protected void map(LongWritable key, Text value,Context context)throws IOException, InterruptedException {
            //A:B,C,D,F,E,O
            String files[] = value.toString().trim().split(":");
            String fans[] = files[1].split(",");
            String user=files[0];
            for(int i=0;i<fans.length;i++){
                int c=user.compareTo(fans[i]);
                String keyStr=user+"-"+fans[i];
                if(c>0) {
                    keyStr = fans[i] + "-" + user;
                }
                context.write(new Text(keyStr),NullWritable.get());
            }
        }
    }


    private static class MyReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context)throws IOException, InterruptedException {
            int count = 0;
            //判断好友对数目

            for(NullWritable value : values){
                count ++;
            }
            if (count >0) {
                context.write(key, NullWritable.get());
            }

        }
    }


    public static void main(String[] args) throws Exception {

        //提前删除输出目录
        File outPutDir = new File(args[1]);
        if (outPutDir.exists()) {
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "求共同好友");

        job.setJarByClass(EachFansMr.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }


}
